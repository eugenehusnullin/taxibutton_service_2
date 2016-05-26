package tb.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
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
import tb.car.domain.LastGeoData;
import tb.dao.IDeviceDao;
import tb.dao.IOfferDao;
import tb.dao.IOrderAssignRequestDao;
import tb.dao.IOrderCancelDao;
import tb.dao.IOrderDao;
import tb.dao.IOrderStatusDao;
import tb.dao.IPartnerDao;
import tb.domain.Device;
import tb.domain.Partner;
import tb.domain.order.AddressPoint;
import tb.domain.order.AssignRequest;
import tb.domain.order.Feedback;
import tb.domain.order.Offer;
import tb.domain.order.Order;
import tb.domain.order.OrderCancel;
import tb.domain.order.OrderCancelType;
import tb.domain.order.OrderProcessing;
import tb.domain.order.OrderStatus;
import tb.domain.order.OrderStatusType;
import tb.orderprocessing.SheduledProcessing;
import tb.service.exceptions.CarNotFoundException;
import tb.service.exceptions.DeviceNotFoundException;
import tb.service.exceptions.NotValidOrderStatusException;
import tb.service.exceptions.OrderNotFoundException;
import tb.service.exceptions.ParseOrderException;
import tb.service.exceptions.PartnerNotFoundException;
import tb.service.exceptions.WrongData;
import tb.service.serialize.OrderJsonParser;
import tb.service.serialize.YandexOrderSerializer;
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
	private PartnerService partnerService;
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
	private SheduledProcessing processing;
	@Autowired
	private BrandingService brandingService;

	@Value("#{mainSettings['createorder.deltaminutes']}")
	private Integer createOrderDeltaMinutes = 20;

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

		// OrderStatusType lastStatusType = orderStatusDao.getLast(order).getStatus();
		// if (lastStatusType != OrderStatusType.Driving && lastStatusType != OrderStatusType.Waiting) {
		// throw new WrongData();
		// }

		Partner partner = order.getPartner();
		String url = partner.getApiurl() + "/1.x/inform";
		try {
			String informStr = informJson.toString();
			HttpUtils.postRawData(informStr, url, "UTF-8", "application/json");

			orderDao.addOrderProcessing(order.getId(), "�������� ��������� INFORM");
			logger.info("OrderId = " + order.getId() + "�������� ��������� INFORM: " + informStr);
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
	public Order initOrder(JSONObject createOrderObject)
			throws DeviceNotFoundException, ParseOrderException, JSONException, IOException {
		String deviceApiid = createOrderObject.optString("apiId");
		Device device = deviceDao.get(deviceApiid);
		if (device == null) {
			throw new DeviceNotFoundException(deviceApiid);
		}

		Order order = OrderJsonParser.Json2Order(createOrderObject.getJSONObject("order"), device,
				partnerDao, partnerService, brandingService);
		order.setDevice(device);
		return order;
	}

	@Transactional
	public void create(Order order) throws ParseOrderException {
		order.setCreatedDate(new Date());

		// check booking date
		if (DatetimeUtils.checkTimeout(order.getBookingDate(), createOrderDeltaMinutes * 60 * 1000,
				order.getCreatedDate())) {
			throw new ParseOrderException("bookingdate is out");
		}

		order.setUuid(UUID.randomUUID().toString().replace("-", ""));
		orderDao.save(order);

		// create new order status (Created)
		OrderStatus orderStatus = new OrderStatus();
		orderStatus.setDate(new Date());
		orderStatus.setOrder(order);
		orderStatus.setStatus(OrderStatusType.Created);
		orderStatusDao.save(orderStatus);

		// create order processing info
		orderDao.addOrderProcessing(order.getId(),
				"������ �����. ������� " + order.getDevice().getPhone() + ". ����� " + order.getDevice().getTaxi());

		processing.processOrderAsync(order);
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

		OrderStatus status = orderStatusDao.getLast(order);
		if (status.getStatus() == OrderStatusType.Completed
				|| status.getStatus() == OrderStatusType.Cancelled
				|| status.getStatus() == OrderStatusType.Failed) {
			throw new NotValidOrderStatusException(status);
		}

		// create order processing info
		orderDao.addOrderProcessing(order.getId(), "����� ������� �������������.");

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
			statusJson.put("car_model", car.getCarModel());
			statusJson.put("car_number", car.getCarNumber());
		}
		return statusJson;
	}

	@Transactional
	public JSONObject getGeodata(JSONObject jsonRequest)
			throws DeviceNotFoundException, OrderNotFoundException, ParseException {
		String apiId = jsonRequest.optString("apiId");
		Device device = deviceDao.get(apiId);
		if (device == null) {
			throw new DeviceNotFoundException(apiId);
		}

		String orderUuid = jsonRequest.optString("orderId");
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
			String lastDateString = jsonRequest.optString("lastDate");
			OrderStatus firstUserGeodataStatus = order.getStatuses().stream()
					.filter(p -> OrderStatusType.IsValidForUserGeodata(p.getStatus()))
					.sorted((e1, e2) -> e1.getDate().compareTo(e2.getDate()))
					.findFirst()
					.get();

			Date date = firstUserGeodataStatus.getDate();

			if (!lastDateString.isEmpty()) {
				Date userDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(lastDateString);
				if (userDate.after(date)) {
					date = userDate;
				}
			}

			geoDataList = carDao.getGeoData(order.getPartner().getId(), order.getCarUuid(), date);
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

		AddressPoint source = order.getDestinations().first();
		LastGeoData lastGeoData = carDao.getLastGeoData(partner.getId(), uuid);
		double farIndex = Math.abs(lastGeoData.getLat() - source.getLat())
				+ Math.abs(lastGeoData.getLon() - source.getLon());

		AssignRequest alacrity = new AssignRequest();
		alacrity.setPartner(partner);
		alacrity.setOrder(order);
		alacrity.setUuid(uuid);
		alacrity.setDate(new Date());
		alacrity.setFarIndex(farIndex);
		assignRequestDao.save(alacrity);

		orderDao.addOrderProcessing(order.getId(),
				"������� " + partner.getName() + " ������ ��������� �����. �������� " + uuid);
	}

	@Transactional
	public void setNewcar(String partnerApiId, String partnerApiKey, String orderUuid, String newcar)
			throws PartnerNotFoundException, OrderNotFoundException, CarNotFoundException {
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

		Car car = carDao.getCar(partner.getId(), newcar);
		if (car == null) {
			throw new CarNotFoundException(partner.getId(), newcar);
		}

		order.setCarUuid(newcar);
		orderDao.saveOrUpdate(order);

		orderDao.addOrderProcessing(order.getId(),
				"������� " + partner.getName() + " ������ ��������. ����� �������� " + newcar);
	}

	@Transactional
	public void setStatus(String partnerApiId, String partnerApiKey, String orderUuid, String newStatus,
			String statusParams) throws PartnerNotFoundException, OrderNotFoundException {
		OrderStatusType newStatusType = defineOrderStatusType(newStatus);
		if (newStatusType == null) {
			return;
		}

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
		status.setStatus(newStatusType);
		status.setStatusDescription(statusParams);
		orderStatusDao.save(status);

		orderDao.addOrderProcessing(order.getId(),
				"������� " + partner.getName() +
						", �������� " + order.getCarUuid() +
						", ������� ������ ������. " + newStatus +
						" " + (statusParams == null ? "" : statusParams));
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

			model.setRequirements(
					order.getRequirements().stream()
							.map(p -> p.getType() + " = " + p.getOptions())
							.collect(Collectors.joining(", ")));
			model.setVehicleClass(order.getCarClass());
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
	public OrderModel getOrderModel(Long orderId) {
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
	public Order getOrder(Long orderId) {
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
		Car car = carDao.getCar(assignRequest.getPartner().getId(), assignRequest.getUuid());
		Car4Request car4Request = new Car4Request();
		car4Request.setUuid(car.getUuid());
		car4Request.setCarClass(order.getCarClass());

		Date bookDate = DatetimeUtils.offsetTimeZone(order.getBookingDate(), "UTC",
				assignRequest.getPartner().getTimezoneId());

		Document doc = YandexOrderSerializer.orderToSetcarXml(order, bookDate, car4Request, order.getPhone(),
				order.getDevice().getUserName());
		String url = assignRequest.getPartner().getApiurl() + "/1.x/setcar";

		int responseCode = 0;
		try {
			HttpURLConnection con = HttpUtils.postDocumentOverHttp(doc, url, logger);
			responseCode = con.getResponseCode();
			if (responseCode != 200) {
				try {
					logger.error(IOUtils.toString(con.getErrorStream()));
				} catch (Exception e) {
				}
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
	public boolean assign(Order order) {
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

				orderDao.addOrderProcessing(order.getId(),
						"����� ������� ����� �� ���������� �������� " + assignRequest.getPartner().getName()
								+ ", �������� " + assignRequest.getUuid());

				return true;
			} else {
				if (responseCode != 0) {
					logger.info("assign for order id=" + order.getId() + ", fail give to partner.");
					assignRequest.setFail(true);
					assignRequest.setFailHttpCode(responseCode);
					assignRequestDao.save(assignRequest);

					orderDao.addOrderProcessing(order.getId(),
							"����� �� ���� ���� ������� �� ���������� �������� " + assignRequest.getPartner().getName()
									+ " ������ " + responseCode);
				} else {
					orderDao.addOrderProcessing(order.getId(),
							"����� �� ���� ���� ������� �� ���������� �������� " + assignRequest.getPartner().getName()
									+ " ������ ���������� � ���������.");
				}
			}
		}

		return false;
	}

	@Transactional
	public List<OrderProcessing> getOrderProcessing(Long orderId) {
		return orderDao.getOrderProcessing(orderId);
	}

}
