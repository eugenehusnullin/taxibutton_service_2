package tb.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import tb.dao.IOrderAssignRequestDao;
import tb.domain.Partner;
import tb.domain.order.AssignRequest;
import tb.domain.order.Order;

@Repository("OrderAssignRequestDao")
public class OrderAssignRequestDao implements IOrderAssignRequestDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public AssignRequest get(Order order, Partner partner, String uuid) {
		return (AssignRequest) sessionFactory.getCurrentSession().createCriteria(AssignRequest.class)
				.add(Restrictions.eq("order", order)).add(Restrictions.eq("partner", partner))
				.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}

	@Override
	public void save(AssignRequest alacrity) {
		sessionFactory.getCurrentSession().save(alacrity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AssignRequest> getAll(Order order) {
		return sessionFactory.getCurrentSession().createCriteria(AssignRequest.class)
				.add(Restrictions.eq("order", order)).list();
	}

	@Override
	public AssignRequest getBestAssignRequest(Order order) {
		AssignRequest winnerAlacrity = (AssignRequest) sessionFactory.getCurrentSession()
				.createCriteria(AssignRequest.class)
				.add(Restrictions.eq("order", order))
				.add(
						Restrictions.or(
								Restrictions.eq("fail", false),
								Restrictions.isNull("fail")))
				.addOrder(org.hibernate.criterion.Order.asc("date"))
				.setMaxResults(1)
				.uniqueResult();

		return winnerAlacrity;
	}
}
