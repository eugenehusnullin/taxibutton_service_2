package tb.orderprocessing;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tb.dao.IOfferDao;
import tb.dao.IOrderDao;
import tb.dao.IOrderStatusDao;
import tb.domain.order.Order;
import tb.domain.order.OrderCancelType;
import tb.domain.order.OrderStatus;
import tb.domain.order.OrderStatusType;
import tb.service.OfferingOrderTaxiRF;
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
	private OfferingOrderTaxiRF offeringOrder;

	@Value("#{mainSettings['processing.offer.expired.timeout']}")
	private Integer offerExpiredTimeout;
	@Value("#{mainSettings['processing.assign.expired.timeout']}")
	private Integer assignExpiredTimeout;

	enum ProcessingState {
		Offer, Assign
	};

	@Transactional
	public void processOrder(Long orderId) {
		Order order = orderDao.get(orderId);
		ProcessingState state = defineProcessingState(order);
		logger.debug("process order id=" + orderId + " ProcessingState=" + state.toString());

		boolean expired = checkExpired(order, state);
		if (expired) {
			logger.debug("process order id=" + orderId + " is expired.");
			makeExpiredWork(order);
			return;
		}

		switch (state) {
		case Offer:
			logger.debug("process order id=" + orderId + " make offer.");
			makeOfferWork(order);
			return;

		case Assign:
			logger.debug("process order id=" + orderId + " make assign.");
			makeAssignWork(order);
			return;

		default:
			break;
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
		offeringOrder.offer(order);
	}

	private void makeAssignWork(Order order) {
		orderService.assign(order);
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
