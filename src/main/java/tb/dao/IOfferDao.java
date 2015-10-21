package tb.dao;

import java.util.List;

import tb.domain.order.Offer;
import tb.domain.order.Order;

public interface IOfferDao {
	void save(Offer offeredOrderPartner);
	List<Offer> get(Order order);
	Long getCount(Order order);
}
