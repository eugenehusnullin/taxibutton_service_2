package tb.dao;

import java.util.List;

import tb.domain.order.Feedback;
import tb.domain.order.Order;
import tb.domain.order.OrderProcessing;

public interface IOrderDao {

	Order get(Long id);

	Order get(String uuid);

	List<Order> getPagination(String orderField, String orderDirection, int start, int count);

	void save(Order order);

	void saveOrUpdate(Order order);

	Long getAllOrdersCount();

	void saveFeedback(Feedback feedback);

	List<Order> getUnfinishedOrders();

	void addOrderProcessing(Long orderId, String note);

	List<OrderProcessing> getOrderProcessing(Long orderId);
}
