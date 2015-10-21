package tb.orderprocessing;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tb.dao.IOrderDao;
import tb.domain.order.Order;

@Service
public class SheduledProcessing {
	private static final Logger logger = LoggerFactory.getLogger(SheduledProcessing.class);

	@Autowired
	private ExecutorService executorService;
	@Autowired
	private IOrderDao orderDao;
	@Autowired
	private Processing processing;

	@Scheduled(fixedDelay = 30000)
	@Transactional
	public void process() {
		List<Order> orders = orderDao.getUnfinishedOrders();
		logger.debug("process orders count=" + orders.size());

		for (Order order : orders) {
			processOrderAsync(order);
		}
	}

	public void processOrderAsync(Order order) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				processing.processOrder(order.getId());
			}
		});
	}
	
	@PreDestroy
	public void stoping() {
		executorService.shutdownNow();
	}

}
