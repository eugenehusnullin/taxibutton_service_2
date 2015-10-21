package tb.dao;

import java.util.List;

import tb.domain.Partner;
import tb.domain.order.Order;
import tb.domain.order.AssignRequest;

public interface IOrderAssignRequestDao {

	AssignRequest get(Order order, Partner partner, String uuid);

	List<AssignRequest> getAll(Order order);

	void save(AssignRequest alacrity);

	AssignRequest getBestAssignRequest(Order order);
}
