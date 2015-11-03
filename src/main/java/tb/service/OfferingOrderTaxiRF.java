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
import tb.dao.IOrderStatusDao;
import tb.dao.IPartnerDao;
import tb.domain.Partner;
import tb.domain.TariffDefinition;
import tb.domain.order.Offer;
import tb.domain.order.Order;
import tb.domain.order.OrderStatus;
import tb.domain.order.OrderStatusType;
import tb.service.serialize.YandexOrderSerializer;
import tb.tariffdefinition.TariffDefinitionHelper;
import tb.utils.DatetimeUtils;
import tb.utils.HttpUtils;

@Service
public class OfferingOrderTaxiRF {
	private static final Logger logger = LoggerFactory.getLogger(OfferingOrderTaxiRF.class);

	@Autowired
	private IOrderStatusDao orderStatusDao;
	@Autowired
	private CarDao carDao;
	@Autowired
	private IPartnerDao partnerDao;
	// @Autowired
	// private ITariffDao tariffDao;
	@Autowired
	private IOfferDao offerDao;
	@Autowired
	private PartnerService partnerService;
	@Autowired
	private TariffDefinitionHelper tariffDefinitionHelper;

	@Value("#{mainSettings['offerorder.coordcoef']}")
	private double COORDINATES_COEF;
	@Value("#{mainSettings['offerorder.speed']}")
	private int SPEED;
	private final static int MINUTE_IN_HOUR = 60;

	@SuppressWarnings("unused")
	@Transactional
	public Boolean offer(Order order) {
		// common check (order state and e.t.c.)
		if (!checkOrderValid4Offer(order)) {
			logger.info("Order - " + order.getUuid() + ", has bad state for offering.");
			return null;
		}

		TariffDefinition tariffDefinition = tariffDefinitionHelper.getTariffDefinition(order.getSource().getLat(),
				order.getSource().getLon(), order.getOrderVehicleClass());
		if (tariffDefinition == null) {
			logger.info("Order - " + order.getUuid() + ", tariff definition not found for order.");
			return null;
		}

		String tariffIdName = tariffDefinition.getIdName();

		Map<Long, Document> messages4Send = null;
		if (true /* true потому что заказы подаются прямо перед подачей за deltaMinutes param from settings file */) {
			logger.info("Order - " + order.getUuid() + ", try offer NOT LATER order.");

			List<CarState> carStates = carDao.getNearCarStates(order.getSource().getLat(), order.getSource().getLon(),
					COORDINATES_COEF);

			if (carStates.size() == 0) {
				logger.info("Order - " + order.getUuid()
						+ ", NOT OFFER - not found car normal state or(and) nornmal distance.");
				return false;
			}

			if (order.getOfferPartners() != null && order.getOfferPartners().size() > 0) {
				final List<Long> limitPartnerIds = order.getOfferPartners().stream().map(m -> m.getId())
						.collect(Collectors.toList());
				carStates = carStates.stream()
						.filter(p -> limitPartnerIds.contains(p.getPartnerId()))
						.collect(Collectors.toList());

				if (carStates.size() == 0) {
					logger.info("Order - " + order.getUuid()
							+ ", NOT OFFER - not found car normal state or(and) nornmal distance of choosed partner.");
				}
			}
			List<Long> partnerIdsList = carStates.stream().map(p -> p.getPartnerId()).distinct()
					.collect(Collectors.toList());

			carStates = carDao.getCarStatesByRequirements(carStates, order.getRequirements(), tariffIdName);
			if (carStates.size() == 0) {
				logger.info("Order - " + order.getUuid()
						+ ", NOT OFFER - not found car with selected additional services.");
			}
			messages4Send = createNotlaterOffer(order, partnerIdsList, carStates, tariffIdName);
		} else {
			logger.info("Order - " + order.getUuid() + ", try offer EXACT order.");

			List<Partner> partners = partnerService.getPartnersByMapAreas(order.getSource().getLat(),
					order.getSource().getLon());

			if (partners.size() == 0) {
				logger.info("Order - " + order.getUuid()
						+ ", NOT OFFER - not found partners with needed mapareas.");
			}

			if (order.getOfferPartners() != null && order.getOfferPartners().size() > 0) {
				final List<Long> limitPartnerIds = order.getOfferPartners().stream().map(m -> m.getId())
						.collect(Collectors.toList());
				partners = partners.stream()
						.filter(p -> limitPartnerIds.contains(p.getId()))
						.collect(Collectors.toList());

				if (partners.size() == 0) {
					logger.info("Order - " + order.getUuid()
							+ ", NOT OFFER - not found choosed partner in mapareas.");
				}
			}
			messages4Send = createExactOffers(order, partners.stream().map(p -> p.getId()).collect(Collectors.toList()),
					tariffIdName);
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
					Offer offeredOrderPartner = new Offer();
					offeredOrderPartner.setOrder(order);
					offeredOrderPartner.setPartner(partner);
					offeredOrderPartner.setTimestamp(new Date());
					offerDao.save(offeredOrderPartner);
				}
			} catch (IOException | TransformerException | TransformerFactoryConfigurationError e) {
				logger.error("MAKE OFFER ORDER - " + order.getUuid() + ".", e);
			}
		}
		return result;
	}

	private Map<Long, Document> createExactOffers(Order order, List<Long> partnerIdsList, String tariffIdName) {
		Map<Long, Document> messagesMap = new HashMap<Long, Document>();
		for (Long partnerId : partnerIdsList) {
			Partner partner = partnerDao.get(partnerId);
			Document doc = createExactOffer(order, partner, tariffIdName);
			messagesMap.put(partnerId, doc);
		}
		return messagesMap;
	}

	@Transactional
	public Document createExactOffer(Order order, Partner partner, String tariffIdName) {
		Date bookDate = DatetimeUtils.offsetTimeZone(order.getBookingDate(), "UTC", partner.getTimezoneId());

		List<String> tariffIds = new ArrayList<String>();
		tariffIds.add(tariffIdName);
		Document doc = YandexOrderSerializer.orderToRequestXml(order, bookDate, tariffIds, null);

		return doc;
	}

	private Map<Long, Document> createNotlaterOffer(Order order, List<Long> partnerIdsList, List<CarState> carStates,
			String tariffIdName) {
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
				car4Request.setTariff(tariffIdName);

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
