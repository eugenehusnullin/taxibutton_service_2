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
import tb.car.domain.LastGeoData;
import tb.dao.IOfferDao;
import tb.dao.IOrderDao;
import tb.dao.IPartnerDao;
import tb.domain.Partner;
import tb.domain.order.Offer;
import tb.domain.order.Order;
import tb.service.serialize.YandexOrderSerializer;
import tb.utils.DatetimeUtils;
import tb.utils.HttpUtils;

@Service
public class OfferingOrder {
	private static final Logger logger = LoggerFactory.getLogger(OfferingOrder.class);

	@Autowired
	private CarDao carDao;
	@Autowired
	private IPartnerDao partnerDao;
	@Autowired
	private IOfferDao offerDao;
	@Autowired
	private IOrderDao orderDao;
	@Autowired
	private PartnerService partnerService;

	@Value("#{mainSettings['offerorder.coordcoef']}")
	private double COORDINATES_COEF;
	@Value("#{mainSettings['offerorder.speed']}")
	private int SPEED;
	private final static int MINUTE_IN_HOUR = 60;

	@Transactional
	public Boolean offer(Order order) {

		// !select partners by device codeName(taxi)
		List<Partner> partners = null;
		String taxi = order.getDevice().getTaxi();
		if (taxi != null && !taxi.isEmpty()) {
			partners = partnerService.getByCodeName(taxi);
		}

		//
		// !select partners by map area
		partners = partnerService.getPartnersByMapAreas(partners,
				order.getSource().getLat(), order.getSource().getLon());

		if (partners == null || partners.size() == 0) {
			orderDao.addOrderProcessing(order.getId(), "Не найдены партнеры в геозоне действия заказа.");
			logger.info("Order - " + order.getUuid() + ", partners not found.");
			return null;
		}

		//
		// !select by car state
		List<Object[]> comb = carDao.getNearCarStates(partners, order.getSource().getLat(),
				order.getSource().getLon(),
				COORDINATES_COEF);
		List<LastGeoData> lastGeoDatas = comb.stream()
				.map(p -> (LastGeoData) p[1])
				.collect(Collectors.toList());
		if (lastGeoDatas.size() == 0) {
			orderDao.addOrderProcessing(order.getId(),
					"Не найдено ни одной машины такси вблизи заказа, с допустимым состоянием.");
			logger.info("Order - " + order.getUuid()
					+ ", NOT OFFER - not found car normal state or(and) nornmal distance.");
			return false;
		}

		//
		// !select by partnerId in order
		if (order.getOfferPartners() != null && order.getOfferPartners().size() > 0) {
			final List<Long> limitPartnerIds = order.getOfferPartners().stream().map(m -> m.getId())
					.collect(Collectors.toList());
			lastGeoDatas = lastGeoDatas.stream()
					.filter(p -> limitPartnerIds.contains(p.getPartnerId()))
					.collect(Collectors.toList());

			if (lastGeoDatas.size() == 0) {
				orderDao.addOrderProcessing(order.getId(),
						"Не найдено ни одной машины такси вблизи заказа, для указаного в заказе партнера.");
				logger.info("Order - " + order.getUuid()
						+ ", NOT OFFER - not found car normal state or(and) nornmal distance of choosed partner.");
				return false;
			}
		}

		//
		// !select by requirements and car class
		lastGeoDatas = carDao.getCarStatesByRequirements(lastGeoDatas, order.getRequirements(),
				order.getCarClass(), order.getCarBasket());
		if (lastGeoDatas.size() == 0) {
			orderDao.addOrderProcessing(order.getId(), "Не найдено ни одной машины такси с выбраными опциями.");
			logger.info("Order - " + order.getUuid()
					+ ", NOT OFFER - not found car with selected additional services.");
			return false;
		}

		//
		// !log for admin
		String scars = lastGeoDatas
				.stream()
				.map(p -> p.getPartnerId().toString() + "->" + p.getUuid())
				.collect(Collectors.joining(", "));
		orderDao.addOrderProcessing(order.getId(), "Подходящие машины такси: " + scars);

		List<Long> partnerIdsList = lastGeoDatas.stream()
				.map(p -> p.getPartnerId())
				.distinct()
				.collect(Collectors.toList());
		Map<Long, Document> messages4Send = createNotlaterOffer(order, partnerIdsList, lastGeoDatas);
		return makeOffer(messages4Send, order);
	}

	private Map<Long, Document> createNotlaterOffer(Order order, List<Long> partnerIdsList,
			List<LastGeoData> lastGeoDatas) {
		double lat = order.getSource().getLat();
		double lon = order.getSource().getLon();
		Map<Long, Document> messagesMap = new HashMap<Long, Document>();
		for (Long partnerId : partnerIdsList) {
			Partner partner = partnerDao.get(partnerId);
			Date bookDate = DatetimeUtils.offsetTimeZone(order.getBookingDate(), "UTC", partner.getTimezoneId());

			List<LastGeoData> filteredLastGeoDatas = lastGeoDatas.stream()
					.filter(p -> p.getPartnerId() == partnerId)
					.collect(Collectors.toList());

			List<Car4Request> car4RequestList = new ArrayList<Car4Request>();
			for (LastGeoData lastGeoData : filteredLastGeoDatas) {
				Car4Request car4Request = new Car4Request();
				car4Request.setUuid(lastGeoData.getUuid());
				int dist = (int) calcDistance(lat, lon, lastGeoData.getLat(), lastGeoData.getLon());
				car4Request.setDist(dist);
				car4Request.setTime((dist * MINUTE_IN_HOUR) / SPEED);
				car4Request.setCarClass(order.getCarClass());

				car4RequestList.add(car4Request);
			}

			Document doc = YandexOrderSerializer.orderToRequestXml(order, bookDate, car4RequestList);
			messagesMap.put(partnerId, doc);
		}

		return messagesMap;
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
				int response = HttpUtils.postDocumentOverHttp(doc, url, logger).getResponseCode();
				boolean posted = response == 200;
				result |= posted;
				if (posted) {
					Offer offeredOrderPartner = new Offer();
					offeredOrderPartner.setOrder(order);
					offeredOrderPartner.setPartner(partner);
					offeredOrderPartner.setTimestamp(new Date());
					offerDao.save(offeredOrderPartner);
					orderDao.addOrderProcessing(order.getId(), "Заказ успешно предложен партнеру " + partner.getName());

				} else {
					orderDao.addOrderProcessing(order.getId(),
							"Заказ не смог быть предложенным партнеру " + partner.getName()
									+ ", код ошибки - " + response);
				}
			} catch (IOException | TransformerException | TransformerFactoryConfigurationError e) {
				logger.error("MAKE OFFER ORDER - " + order.getUuid() + ".", e);
				orderDao.addOrderProcessing(order.getId(),
						"Заказ не смог быть предложенным партнеру " + partner.getName()
								+ ", ошибка - " + e.getMessage());
			}
		}
		return result;
	}

	private long calcDistance(double lat1, double lon1, double lat2, double lon2) {
		return Math.round(
				Math.sqrt(
						Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2)) * 111);
		// 111 - km in one degree
	}
}
