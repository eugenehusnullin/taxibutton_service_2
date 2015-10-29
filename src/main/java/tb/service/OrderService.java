package tb.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import tb.admin.model.AlacrityModel;
import tb.admin.model.OrderModel;
import tb.admin.model.OrderStatusModel;
import tb.car.dao.CarDao;
import tb.car.domain.Car;
import tb.car.domain.Car4Request;
import tb.car.domain.GeoData;
import tb.dao.IDeviceDao;
import tb.dao.IOfferDao;
import tb.dao.IOrderAssignRequestDao;
import tb.dao.IOrderCancelDao;
import tb.dao.IOrderDao;
import tb.dao.IOrderStatusDao;
import tb.dao.IPartnerDao;
import tb.domain.Device;
import tb.domain.Partner;
import tb.domain.order.AssignRequest;
import tb.domain.order.Feedback;
import tb.domain.order.Offer;
import tb.domain.order.Order;
import tb.domain.order.OrderCancel;
import tb.domain.order.OrderCancelType;
import tb.domain.order.OrderStatus;
import tb.domain.order.OrderStatusType;
import tb.orderprocessing.SheduledProcessing;
import tb.service.exceptions.DeviceNotFoundException;
import tb.service.exceptions.NotValidOrderStatusException;
import tb.service.exceptions.OrderNotFoundException;
import tb.service.exceptions.ParseOrderException;
import tb.service.exceptions.PartnerNotFoundException;
import tb.service.exceptions.WrongData;
import tb.service.serialize.OrderJsonParser;
import tb.service.serialize.YandexOrderSerializer;
import tb.tariffdefinition.TariffDefinitionHelper;
import tb.utils.DatetimeUtils;
import tb.utils.HttpUtils;

@Service
public class OrderService {

	private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

	@Autowired
	private IOrderDao orderDao;
	@Autowired
	private IOrderStatusDao orderStatusDao;
	@Autowired
	private IDeviceDao deviceDao;
	@Autowired
	private IPartnerDao partnerDao;
	@Autowired
	private IOfferDao offerDao;
	@Autowired
	private IOrderCancelDao orderCancelDao;
	@Autowired
	private IOrderAssignRequestDao assignRequestDao;
	@Autowired
	private IOrderAssignRequestDao orderAlacrityDao;
	@Autowired
	private CarDao carDao;
	@Autowired
	private TariffDefinitionHelper tariffDefinitionHelper;
	@Autowired
	private SheduledProcessing processing;

	@Value("#{mainSettings['createorder.limit']}")
	private Integer createOrderLimit = 60000;

	@Value("#{mainSettings['offerorder.notlaterminutes']}")
	private int notlaterMinutes;

	@Transactional
	public void saveFeedback(JSONObject feedbackJson) throws OrderNotFoundException, WrongData {
		String apiId = feedbackJson.optString("apiId");
		String orderUuid = feedbackJson.optString("orderId");
		Order order = orderDao.get(orderUuid);
		if (order == null || !order.getDevice().getApiId().equals(apiId)) {
			throw new OrderNotFoundException(orderUuid);
		}

		int rating = feedbackJson.optInt("rating", -1);
		String text = feedbackJson.optString("feedback");
		if (rating == -1 && text == null) {
			throw new WrongData();
		}

		Feedback feedback = new Feedback();
		feedback.setDate(new Date());
		feedback.setOrder(order);
		feedback.setRating(rating);
		feedback.setText(text);

		orderDao.saveFeedback(feedback);
		return;
	}

	@Transactional
	public void informEvent(JSONObject informJson) throws OrderNotFoundException, WrongData {
		String apiId = informJson.optString("apiId");
		String orderUuid = informJson.optString("orderId");
		Order order = orderDao.get(orderUuid);
		if (order == null || !order.getDevice().getApiId().equals(apiId)) {
			throw new OrderNotFoundException(orderUuid);
		}

		OrderStatusType lastStatusType = orderStatusDao.getLast(order).getStatus();
		if (lastStatusType != OrderStatusType.Driving && lastStatusType != OrderStatusType.Waiting) {
			throw new WrongData();
		}

		Partner partner = order.getPartner();
		String url = partner.getApiurl() + "/1.x/inform";
		try {
			HttpUtils.postRawData(informJson.toString(), url, "UTF-8", "application/json");
		} catch (Exception e) {
			logger.error("INFORM - " + order.getUuid() + ".", e);
		}

		return;
	}

	@Transactional
	public void getOrders(String apiId, String apiKey) throws PartnerNotFoundException {
		Partner partner = partnerDao.getByApiId(apiId, apiKey);
		if (partner == null) {
			throw new PartnerNotFoundException(apiId);
		}

	}

	@Transactional
	public Order initOrder(JSONObject createOrderObject) throws DeviceNotFoundException, ParseOrderException {
		String deviceApiid = createOrderObject.optString("apiId");
		Device device = deviceDao.get(deviceApiid);
		if (device == null) {
			throw new DeviceNotFoundException(deviceApiid);
		}

		Order order = OrderJsonParser.Json2Order(createOrderObject.getJSONObject("order"), device.getPhone(),
				partnerDao,
				notlaterMinutes);
		order.setDevice(device);
		return order;
	}

	@Transactional
	public void create(Order order) throws ParseOrderException {
		// check booking date
		if (DatetimeUtils.checkTimeout(order.getBookingDate(), createOrderLimit, new Date())) {
			throw new ParseOrderException("bookingdate is out");
		}

		order.setUuid(UUID.randomUUID().toString().replace("-", ""));
		order.setCreatedDate(new Date());
		order.setStartProcessing(new Date());
		orderDao.save(order);

		// create new order status (Created)
		OrderStatus orderStatus = new OrderStatus();
		orderStatus.setDate(new Date());
		orderStatus.setOrder(order);
		orderStatus.setStatus(OrderStatusType.Created);
		orderStatusDao.save(orderStatus);

		if (order.getNotlater()) {
			processing.processOrderAsync(order);
		}
	}

	@Transactional
	public void cancel(JSONObject cancelOrderJson)
			throws DeviceNotFoundException, OrderNotFoundException, NotValidOrderStatusException {

		String apiId = cancelOrderJson.getString("apiId");
		String orderUuid = cancelOrderJson.getString("orderId");
		String reason = cancelOrderJson.getString("reason");

		Device device = deviceDao.get(apiId);
		if (device == null) {
			throw new DeviceNotFoundException(apiId);
		}

		Order order = orderDao.get(orderUuid);
		if (order == null) {
			throw new OrderNotFoundException(orderUuid);
		}

		if (!order.getDevice().getApiId().equals(apiId)) {
			throw new OrderNotFoundException(orderUuid);
		}

		cancelAction(order, reason, device);
	}

	private void cancelAction(Order order, String reason, Device device) throws NotValidOrderStatusException {

		OrderCancel orderCancel = new OrderCancel();
		orderCancel.setOrder(order);
		orderCancel.setReason(reason);
		orderCancelDao.save(orderCancel);

		OrderStatus newStatus = new OrderStatus();
		newStatus.setOrder(order);
		newStatus.setStatus(OrderStatusType.Cancelled);
		newStatus.setDate(new Date());
		orderStatusDao.save(newStatus);

		cancelOrder(order, OrderCancelType.User);
	}

	@Transactional
	public JSONObject getStatus(JSONObject getStatusObject) throws DeviceNotFoundException, OrderNotFoundException {
		String apiId = getStatusObject.getString("apiId");
		Device device = deviceDao.get(apiId);
		if (device == null) {
			throw new DeviceNotFoundException(apiId);
		}

		String orderUuid = getStatusObject.getString("orderId");
		Order order = orderDao.get(orderUuid);
		if (order == null) {
			throw new OrderNotFoundException(orderUuid);
		}

		if (!order.getDevice().getApiId().equals(apiId)) {
			throw new OrderNotFoundException(orderUuid);
		}

		OrderStatus status = orderStatusDao.getLast(order);
		JSONObject statusJson = new JSONObject();
		statusJson.put("orderId", orderUuid);
		statusJson.put("status", status.getStatus().toString());
		statusJson.put("date", status.getDate());
		statusJson.put("description", status.getStatusDescription());

		if (order.getPartner() != null) {
			statusJson.put("executor", order.getPartner().getName());
			Car car = carDao.getCar(order.getPartner().getId(), order.getCarUuid());
			statusJson.put("driver_name", car.getDriverDisplayName());
			statusJson.put("driver_phone", car.getDriverPhone());
			statusJson.put("car_color", car.getCarColor());
			statusJson.put("car_mark", car.getCarModel());
			statusJson.put("car_model", car.getCarModel());
			statusJson.put("car_number", car.getCarNumber());
		}
		return statusJson;
	}

	@Transactional
	public JSONObject getGeodata(JSONObject getGeodataJsonObject)
			throws DeviceNotFoundException, OrderNotFoundException, ParseException {
		String apiId = getGeodataJsonObject.optString("apiId");
		Device device = deviceDao.get(apiId);
		if (device == null) {
			throw new DeviceNotFoundException(apiId);
		}

		String orderUuid = getGeodataJsonObject.optString("orderId");
		Order order = orderDao.get(orderUuid);
		if (order == null) {
			throw new OrderNotFoundException(orderUuid);
		}

		if (!order.getDevice().getApiId().equals(apiId)) {
			throw new OrderNotFoundException(orderUuid);
		}

		List<GeoData> geoDataList = new ArrayList<GeoData>();
		OrderStatus lastStatus = orderStatusDao.getLast(order);
		if (OrderStatusType.IsValidForUserGeodata(lastStatus.getStatus())) {
			String lastDateString = getGeodataJsonObject.optString("lastDate");
			Date date = null;
			if (!lastDateString.isEmpty()) {
				date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(lastDateString);
			}
			OrderStatus firstUserGeodataStatus = order.getStatuses().stream()
					.filter(p -> OrderStatusType.IsValidForUserGeodata(p.getStatus()))
					.sorted((e1, e2) -> e1.getDate().compareTo(e2.getDate()))
					.findFirst()
					.get();
			geoDataList = carDao.getGeoData(order.getPartner().getId(), order.getCarUuid(),
					date == null ? firstUserGeodataStatus.getDate() : date);
		}

		JSONObject geoDataJson = new JSONObject();
		geoDataJson.put("orderId", order.getUuid());

		JSONArray geoPointsArrayJson = new JSONArray();
		for (GeoData currentPoint : geoDataList) {

			JSONObject currentPointJson = new JSONObject();
			currentPointJson.put("lat", currentPoint.getLat());
			currentPointJson.put("lon", currentPoint.getLon());
			currentPointJson.put("direction", 1);
			currentPointJson.put("speed", 45);
			currentPointJson.put("category", "n");
			currentPointJson.put("date", currentPoint.getDate());
			geoPointsArrayJson.put(currentPointJson);
		}
		geoDataJson.put("points", geoPointsArrayJson);

		return geoDataJson;
	}

	@Transactional
	public void assignRequest(String partnerApiId, String partnerApiKey, String orderUuid, String uuid)
			throws PartnerNotFoundException, OrderNotFoundException {

		Partner partner = partnerDao.getByApiId(partnerApiId);
		if (partner == null) {
			throw new PartnerNotFoundException(partnerApiId);
		}

		if (!partner.getApiKey().equals(partnerApiKey)) {
			throw new PartnerNotFoundException(partnerApiId);
		}

		Order order = orderDao.get(orderUuid);
		if (order == null) {
			throw new OrderNotFoundException(orderUuid);
		}

		if (assignRequestDao.get(order, partner, uuid) != null) {
			return;
		}

		AssignRequest alacrity = new AssignRequest();
		alacrity.setPartner(partner);
		alacrity.setOrder(order);
		alacrity.setUuid(uuid);
		alacrity.setDate(new Date());
		assignRequestDao.save(alacrity);
	}

	@Transactional
	public void setNewcar(String partnerApiId, String partnerApiKey, String orderUuid, String newcar)
			throws PartnerNotFoundException, OrderNotFoundException {
		Partner partner = partnerDao.getByApiId(partnerApiId);
		if (partner == null) {
			throw new PartnerNotFoundException(partnerApiId);
		}

		if (!partner.getApiKey().equals(partnerApiKey)) {
			throw new PartnerNotFoundException(partnerApiId);
		}

		Order order = orderDao.get(orderUuid);
		if (order == null) {
			throw new OrderNotFoundException(orderUuid);
		}

		if (!order.getPartner().getId().equals(partner.getId())) {
			throw new OrderNotFoundException(orderUuid);
		}

		order.setCarUuid(newcar);
		orderDao.saveOrUpdate(order);
	}

	@Transactional
	public void setStatus(String partnerApiId, String partnerApiKey, String orderUuid, String newStatus,
			String statusParams) throws PartnerNotFoundException, OrderNotFoundException {
		Partner partner = partnerDao.getByApiId(partnerApiId);
		if (partner == null) {
			throw new PartnerNotFoundException(partnerApiId);
		}

		if (!partner.getApiKey().equals(partnerApiKey)) {
			throw new PartnerNotFoundException(partnerApiId);
		}

		Order order = orderDao.get(orderUuid);
		if (order == null) {
			throw new OrderNotFoundException(orderUuid);
		}

		if (order.getPartner() == null || !order.getPartner().getId().equals(partner.getId())) {
			throw new OrderNotFoundException(orderUuid);
		}

		OrderStatus status = new OrderStatus();
		status.setOrder(order);
		status.setDate(new Date());
		status.setStatus(defineOrderStatusType(newStatus));
		status.setStatusDescription(statusParams);
		orderStatusDao.save(status);
	}

	private OrderStatusType defineOrderStatusType(String status) {
		OrderStatusType orderStatusType;
		switch (status) {
		case "driving":
			orderStatusType = OrderStatusType.Driving;
			break;

		case "waiting":
			orderStatusType = OrderStatusType.Waiting;
			break;

		case "transporting":
			orderStatusType = OrderStatusType.Transporting;
			break;

		case "complete":
			orderStatusType = OrderStatusType.Completed;
			break;

		case "cancelled":
			orderStatusType = OrderStatusType.Cancelled;
			break;

		case "failed":
			orderStatusType = OrderStatusType.Failed;
			break;

		default:
			orderStatusType = null;
			break;
		}

		return orderStatusType;
	}

	@Transactional
	public List<OrderModel> listByPage(String orderField, String orderDirection, int start, int count) {
		List<Order> orders = orderDao.getPagination(orderField, orderDirection, start, count);

		List<OrderModel> models = new ArrayList<>();
		for (Order order : orders) {
			OrderStatus lastStatus = orderStatusDao.getLast(order);
			OrderModel model = new OrderModel();
			models.add(model);
			model.setId(order.getId());
			model.setBookingDate(order.getBookingDate());
			model.setLastStatus(lastStatus.getStatus().toString());
			model.setPhone(order.getPhone());
			model.setSourceShortAddress(order.getSource().getShortAddress());
			model.setUrgent(order.getNotlater());
			model.setUuid(order.getUuid());
			if (order.getPartner() != null) {
				model.setPartnerName(order.getPartner().getName());
			}
		}

		return models;
	}

	@Transactional
	public Long getAllCount() {
		return orderDao.getAllOrdersCount();
	}

	@Transactional
	public List<AlacrityModel> getAlacrities(Long orderId) {
		Order order = orderDao.get(orderId);
		List<AssignRequest> listAlacrity = orderAlacrityDao.getAll(order);

		List<AlacrityModel> models = new ArrayList<>();
		for (AssignRequest orderAcceptAlacrity : listAlacrity) {
			AlacrityModel model = new AlacrityModel();
			models.add(model);

			model.setId(orderAcceptAlacrity.getOrder().getId());
			model.setDate(orderAcceptAlacrity.getDate());
		}
		return models;
	}

	@Transactional
	public OrderModel getOrder(Long orderId) {
		Order order = orderDao.get(orderId);
		OrderStatus lastStatus = orderStatusDao.getLast(order);
		OrderModel model = new OrderModel();
		model.setId(order.getId());
		model.setBookingDate(order.getBookingDate());
		model.setLastStatus(lastStatus.getStatus().toString());
		model.setPhone(order.getPhone());
		model.setSourceShortAddress(order.getSource().getShortAddress());
		model.setUrgent(order.getNotlater());
		model.setUuid(order.getUuid());
		if (order.getPartner() != null) {
			model.setPartnerName(order.getPartner().getName());
		}
		return model;
	}

	@Transactional
	public Order getTrueOrder(Long orderId) {
		return orderDao.get(orderId);
	}

	@Transactional
	public List<OrderStatusModel> getStatuses(Long orderId) {
		Order order = orderDao.get(orderId);
		List<OrderStatus> statusList = orderStatusDao.get(order);

		List<OrderStatusModel> models = new ArrayList<>();
		for (OrderStatus orderStatus : statusList) {
			OrderStatusModel model = new OrderStatusModel();
			models.add(model);

			model.setId(orderStatus.getId());
			model.setDate(orderStatus.getDate());
			model.setStatus(orderStatus.getStatus().toString());
		}

		return models;
	}

	@Transactional
	public OrderStatus getOrderLastStatus(Order order) {
		return orderStatusDao.getLast(order);
	}

	private int giveOrder(Order order, AssignRequest assignRequest) {
		String tariffIdName = tariffDefinitionHelper.getTariffIdName(order.getSource().getLat(),
				order.getSource().getLon(), order.getOrderVehicleClass());

		Car car = carDao.getCar(assignRequest.getPartner().getId(), assignRequest.getUuid());
		Car4Request car4Request = new Car4Request();
		car4Request.setUuid(car.getUuid());
		car4Request.setTariff(tariffIdName);

		Date bookDate = DatetimeUtils.offsetTimeZone(order.getBookingDate(), "UTC",
				assignRequest.getPartner().getTimezoneId());

		Document doc = YandexOrderSerializer.orderToSetcarXml(order, bookDate, car4Request, order.getPhone());
		String url = assignRequest.getPartner().getApiurl() + "/1.x/setcar";

		int responseCode = 0;
		try {
			// TODO: DELETE AFTER DEVICE TESTING COMPLETED
			if (order.getDevice().getPhone().startsWith("+++")) {
				responseCode = 200;
			} else {
				responseCode = HttpUtils.postDocumentOverHttp(doc, url, logger).getResponseCode();
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
		return responseCode;
	}

	@Transactional
	public void cancelOrder(Order order, OrderCancelType cancelType) {
		order.setProcessingFinished(true);
		orderDao.save(order);

		List<Offer> offeredPartners = offerDao.get(order);
		String reason = cancelType.toString();
		String params = "orderid=" + order.getUuid() + "&reason=" + reason;

		for (Offer currentOffer : offeredPartners) {
			if (cancelType == OrderCancelType.Assigned
					&& order.getPartner().getId().equals(currentOffer.getPartner().getId())) {
				continue;
			}
			String url = currentOffer.getPartner().getApiurl() + "/1.x/cancelrequest";
			HttpUtils.sendHttpGet(url, params);
		}
	}

	@Transactional
	public void assign(Order order) {
		AssignRequest assignRequest = assignRequestDao.getBestAssignRequest(order);

		if (assignRequest != null) {
			logger.info(
					"assign for order id=" + order.getId() + ", found best id=" + assignRequest.getPartner().getId());
			int responseCode = giveOrder(order, assignRequest);

			if (responseCode == 200) {
				logger.info("assign for order id=" + order.getId() + ", success gived to partner.");

				order.setPartner(assignRequest.getPartner());
				order.setCarUuid(assignRequest.getUuid());
				orderDao.saveOrUpdate(order);

				OrderStatus orderStatus = new OrderStatus();
				orderStatus.setDate(new Date());
				orderStatus.setOrder(order);
				orderStatus.setStatus(OrderStatusType.Taked);
				orderStatusDao.save(orderStatus);

				cancelOrder(order, OrderCancelType.Assigned);
			} else {
				if (responseCode != 0) {
					logger.info("assign for order id=" + order.getId() + ", fail give to partner.");
					assignRequest.setFail(true);
					assignRequest.setFailHttpCode(responseCode);
					assignRequestDao.save(assignRequest);
				}
			}
		}
	}

}
