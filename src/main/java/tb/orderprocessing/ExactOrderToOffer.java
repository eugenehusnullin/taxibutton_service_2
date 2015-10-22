package tb.orderprocessing;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tb.dao.IOrderDao;
import tb.domain.order.Order;

@Service
@EnableScheduling
public class ExactOrderToOffer {

	private static final Logger logger = LoggerFactory.getLogger(ExactOrderToOffer.class);
	@Autowired
	private IOrderDao orderDao;

	@Value("#{mainSettings['offerorder.notlaterminutes']}")
	private int notlaterMinutes;

	@Transactional
	@Scheduled(cron = "0 */2 * * * *")
	// every two minutes
	public void toOffer() {
		List<Order> orders = orderDao.getExactOrdersNeedOffering(notlaterMinutes);
		for (Order order : orders) {
			try {
				order.setNotlater(true);
				order.setStartProcessing(new Date());
				orderDao.save(order);

				logger.info("Set notlater to exact order id=" + order.getId().toString() + ".");
			} catch (Exception e) {
				logger.error("Set notlater to exact order error: ", e);
			}
		}

	}
}
