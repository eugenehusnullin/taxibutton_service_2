package tb.orderprocessing;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tb.dao.IBrandDao;
import tb.dao.IOfferDao;
import tb.dao.IOrderAssignRequestDao;
import tb.dao.IOrderDao;
import tb.dao.IOrderStatusDao;
import tb.domain.Brand;
import tb.domain.order.AssignRequest;
import tb.domain.order.Order;
import tb.domain.order.OrderCancelType;
import tb.domain.order.OrderStatus;
import tb.domain.order.OrderStatusType;
import tb.service.OfferingOrder;
import tb.service.OrderService;

@Service
@EnableScheduling
public class Processing {
	private static final Logger logger = LoggerFactory.getLogger(Processing.class);

	@Autowired
	private IOrderDao orderDao;
	@Autowired
	private IOrderStatusDao orderStatusDao;
	@Autowired
	private IOfferDao offerDao;
	@Autowired
	private OrderService orderService;
	@Autowired
	private OfferingOrder offeringOrder;
	@Autowired
	private IOrderAssignRequestDao orderAssignRequestDao;
	@Autowired
	private IBrandDao brandDao;

	@Value("#{mainSettings['processing.offer.expired.timeout']}")
	private Integer offerExpiredTimeout;
	@Value("#{mainSettings['processing.assign.expired.timeout']}")
	private Integer assignExpiredTimeout;

	private Map<Long, Integer> offerCounts = new HashMap<>();

	enum ProcessingState {
		Offer, Assign
	};

	@Transactional
	public void processOrder(Long orderId) {
		Order order = orderDao.get(orderId);

		if (order.getStartProcessing() == null) {
			order.setStartProcessing(new Date());
			orderDao.save(order);
		}

		ProcessingState state = defineProcessingState(order);
		logger.debug("process order id=" + orderId + " ProcessingState=" + state.toString());

		boolean expired = checkExpired(order, state);
		if (expired) {
			switch (state) {
			case Offer:
				orderDao.addOrderProcessing(order.getId(),
						"Заказ отменен сервером. Т.к. в течении " + offerExpiredTimeout
								+ " минут, он не мог быть предложен ни одному партнеру.");
				break;
			case Assign:
				orderDao.addOrderProcessing(order.getId(),
						"Заказ отменен сервером. Т.к. в течении " + assignExpiredTimeout
								+ " минут, его не взяли ни один партнер.");
				break;
			default:
				break;
			}

			logger.debug("process order id=" + orderId + " is expired.");
			makeExpiredWork(order);

		} else {
			Brand brand = brandDao.get(order.getDevice().getTaxi());
			if (offerDao.getCount(order) < brand.getServices().size()) {
				makeOfferWork(order);
			}

			List<AssignRequest> assigns = orderAssignRequestDao.getAll(order);
			if (assigns.size() > 0) {
				makeAssignWork(order);
			}

			// switch (state) {
			// case Offer:
			// logger.debug("process order id=" + orderId + " make offer.");
			// makeOfferWork(order);
			// return;
			//
			// case Assign:
			// logger.debug("process order id=" + orderId + " make assign.");
			// makeAssignWork(order);
			// return;
			//
			// default:
			// break;
			// }
		}
	}

	private void makeExpiredWork(Order order) {
		OrderStatus failedStatus = new OrderStatus();
		failedStatus.setDate(new Date());
		failedStatus.setOrder(order);
		failedStatus.setStatus(OrderStatusType.Failed);
		orderStatusDao.save(failedStatus);

		orderService.cancelOrder(order, OrderCancelType.Timeout);
	}

	private void makeOfferWork(Order order) {
		Integer count = offerCounts.get(order.getId());
		if (count == null) {
			count = 0;
		}
		count++;
		offerCounts.put(order.getId(), count);

		offeringOrder.offer(order, count);
	}

	private void makeAssignWork(Order order) {
		boolean assigned = orderService.assign(order);
		if (assigned) {
			offerCounts.remove(order.getId());
		}
	}

	private ProcessingState defineProcessingState(Order order) {
		return offerDao.getCount(order) > 0 ? ProcessingState.Assign : ProcessingState.Offer;
	}

	private boolean checkExpired(Order order, ProcessingState state) {
		switch (state) {
		case Offer:
			return isNowAfter(order.getStartProcessing(), offerExpiredTimeout);

		case Assign:
			return isNowAfter(order.getStartProcessing(), assignExpiredTimeout);

		default:
			break;
		}

		return false;
	}

	private boolean isNowAfter(Date date, Integer plusMinutes) {
		LocalDateTime createdDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		return LocalDateTime.now().isAfter(createdDateTime.plusMinutes(offerExpiredTimeout));
	}
}
