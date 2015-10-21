package tb.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import tb.dao.IOfferDao;
import tb.domain.order.Offer;
import tb.domain.order.Order;

@Repository("OfferDao")
public class OfferDao implements IOfferDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public void save(Offer offeredOrderPartner) {
		sessionFactory.getCurrentSession()
				.saveOrUpdate(offeredOrderPartner);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Offer> get(Order order) {
		return sessionFactory.getCurrentSession().createCriteria(Offer.class)
				.add(Restrictions.eq("order", order))
				.list();
	}

	@Override
	public Long getCount(Order order) {
		return (Long) sessionFactory.getCurrentSession().createCriteria(Offer.class)
				.add(Restrictions.eq("order", order))
				.setProjection(Projections.rowCount())
				.uniqueResult();
	}

}
