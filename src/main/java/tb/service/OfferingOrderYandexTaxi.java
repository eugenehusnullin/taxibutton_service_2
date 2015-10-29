package tb.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import tb.car.dao.CarDao;
import tb.car.domain.Car4Request;
import tb.car.domain.CarState;
import tb.dao.IOfferDao;
import tb.dao.IOrderDao;
import tb.dao.IOrderStatusDao;
import tb.dao.IPartnerDao;
import tb.dao.ITariffDao;
import tb.domain.Partner;
import tb.domain.Tariff;
import tb.domain.order.Offer;
import tb.domain.order.Order;
import tb.domain.order.OrderStatus;
import tb.domain.order.OrderStatusType;
import tb.service.serialize.YandexOrderSerializer;
import tb.utils.DatetimeUtils;
import tb.utils.HttpUtils;

@Service
public class OfferingOrderYandexTaxi {
	private static final Logger logger = LoggerFactory.getLogger(OfferingOrderYandexTaxi.class);

	@Autowired
	private IOrderDao orderDao;
	@Autowired
	private IOrderStatusDao orderStatusDao;
	@Autowired
	private CarDao carDao;
	@Autowired
	private IPartnerDao partnerDao;
	@Autowired
	private ITariffDao tariffDao;
	@Autowired
	private IOfferDao offerDao;
	@Autowired
	private PartnerService partnerService;

	@Value("#{mainSettings['offerorder.coordcoef']}")
	private double COORDINATES_COEF;
	@Value("#{mainSettings['offerorder.speed']}")
	private int SPEED;
	private final static int MINUTE_IN_HOUR = 60;

	@Transactional
	public Boolean offer(Long orderId) {
		Order order = orderDao.get(orderId);

		// common check (order state and e.t.c.)
		if (!checkOrderValid4Offer(order)) {
			return null;
		}

		Map<Long, Document> messages4Send = null;
		if (order.getNotlater()) {
			List<CarState> carStates = carDao.getNearCarStates(order.getSource().getLat(), order.getSource().getLon(),
					COORDINATES_COEF);
			if (order.getOfferPartners() != null && order.getOfferPartners().size() > 0) {
				final List<Long> limitPartnerIds = order.getOfferPartners().stream().map(m -> m.getId())
						.collect(Collectors.toList());
				carStates = carStates.stream()
						.filter(p -> limitPartnerIds.contains(p.getPartnerId()))
						.collect(Collectors.toList());
			}
			List<Long> partnerIdsList = carStates.stream().map(p -> p.getPartnerId()).distinct()
					.collect(Collectors.toList());

			carStates = carDao.getCarStatesByRequirements(carStates, order.getRequirements(), null);
			messages4Send = createNotlaterOffer(order, partnerIdsList, carStates);
		} else {
			List<Partner> partners = partnerService.getPartnersByMapAreas(order.getSource().getLat(),
					order.getSource().getLon());
			if (order.getOfferPartners() != null && order.getOfferPartners().size() > 0) {
				final List<Long> limitPartnerIds = order.getOfferPartners().stream().map(m -> m.getId())
						.collect(Collectors.toList());
				partners = partners.stream()
						.filter(p -> limitPartnerIds.contains(p.getId()))
						.collect(Collectors.toList());
			}
			messages4Send = createExactOffers(order,
					partners.stream().map(p -> p.getId()).collect(Collectors.toList()));
		}

		return makeOffer(messages4Send, order);
	}

	private boolean makeOffer(Map<Long, Document> messages4Send, Order order) {
		boolean result = false;
		List<Offer> offeredOrderPartnerList = offerDao.get(order);
		List<Long> excludePartners = offeredOrderPartnerList
				.stream()
				.map(m -> m.getPartner().getId())
				.collect(Collectors.toList());

		for (Map.Entry<Long, Document> entry : messages4Send.entrySet()) {
			Long partnerId = entry.getKey();
			Document doc = entry.getValue();

			if (excludePartners.contains(partnerId)) {
				continue;
			}

			Partner partner = partnerDao.get(partnerId);
			String url = partner.getApiurl() + "/1.x/requestcar";
			try {
				boolean posted = HttpUtils.postDocumentOverHttp(doc, url, logger).getResponseCode() == 200;
				result |= posted;
				if (posted) {
					Offer offer = new Offer();
					offer.setOrder(order);
					offer.setPartner(partner);
					offer.setTimestamp(new Date());
					offerDao.save(offer);
				}
			} catch (IOException | TransformerException | TransformerFactoryConfigurationError e) {
				logger.error("MAKE OFFER.", e);
			}
		}
		return result;
	}

	private Map<Long, Document> createExactOffers(Order order, List<Long> partnerIdsList) {
		Map<Long, Document> messagesMap = new HashMap<Long, Document>();
		for (Long partnerId : partnerIdsList) {
			Partner partner = partnerDao.get(partnerId);
			Document doc = createExactOffer(order, partner);
			messagesMap.put(partnerId, doc);
		}
		return messagesMap;
	}

	@Transactional
	public Document createExactOffer(Order order, Partner partner) {
		Date bookDate = DatetimeUtils.offsetTimeZone(order.getBookingDate(), "UTC", partner.getTimezoneId());

		List<Tariff> tariffs = tariffDao.get(partner);
		List<String> tariffIds = tariffs.stream().map(p -> p.getTariffId()).collect(Collectors.toList());
		Document doc = YandexOrderSerializer.orderToRequestXml(order, bookDate, tariffIds, null);

		return doc;
	}

	private Map<Long, Document> createNotlaterOffer(Order order, List<Long> partnerIdsList, List<CarState> carStates) {
		double lat = order.getSource().getLat();
		double lon = order.getSource().getLon();
		Map<Long, Document> messagesMap = new HashMap<Long, Document>();
		for (Long partnerId : partnerIdsList) {
			Partner partner = partnerDao.get(partnerId);
			Date bookDate = DatetimeUtils.offsetTimeZone(order.getBookingDate(), "UTC", partner.getTimezoneId());

			List<CarState> filteredCarStates = carStates.stream()
					.filter(p -> p.getPartnerId() == partnerId)
					.collect(Collectors.toList());

			List<Car4Request> car4RequestList = new ArrayList<Car4Request>();
			for (CarState carState : filteredCarStates) {
				Car4Request car4Request = new Car4Request();
				car4Request.setUuid(carState.getUuid());
				int dist = (int) calcDistance(lat, lon, carState.getLatitude(), carState.getLongitude());
				car4Request.setDist(dist);
				car4Request.setTime((dist * MINUTE_IN_HOUR) / SPEED);
				car4Request.setTariff(carDao.getFirstTariff(carState.getPartnerId(), carState.getUuid()));

				car4RequestList.add(car4Request);
			}

			List<String> tariffsList = car4RequestList.stream()
					.map(p -> p.getTariff())
					.distinct()
					.collect(Collectors.toList());

			Document doc = YandexOrderSerializer.orderToRequestXml(order, bookDate, tariffsList,
					car4RequestList);
			messagesMap.put(partnerId, doc);
		}

		return messagesMap;
	}

	private long calcDistance(double lat1, double lon1, double lat2, double lon2) {
		return Math.round(
				Math.sqrt(
						Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2)) * 111);
		// 111 - km in one degree
	}

	private boolean checkOrderValid4Offer(Order order) {
		OrderStatus orderStatus = orderStatusDao.getLast(order);
		return OrderStatusType.IsValidForOffer(orderStatus.getStatus());
	}
}
