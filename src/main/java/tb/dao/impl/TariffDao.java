package tb.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import tb.dao.ITariffDao;
import tb.domain.Partner;
import tb.domain.Tariff;

@Repository("TariffDao")
public class TariffDao implements ITariffDao {

	@Autowired
	private SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	@Override
	public List<Tariff> getAll() {
		return sessionFactory.getCurrentSession().createCriteria(Tariff.class).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Tariff> get(Partner partner) {
		return sessionFactory.getCurrentSession().createCriteria(Tariff.class)
				.add(Restrictions.eq("partner", partner))
				.list();
	}

	@Override
	public void saveOrUpdate(Tariff tariff) {
		sessionFactory.getCurrentSession().saveOrUpdate(tariff);
	}

	@Override
	public void delete(Partner partner) {
		String hqlDelete = "delete Tariff where partner = :partner";
		sessionFactory.getCurrentSession().createQuery(hqlDelete)
				.setEntity("partner", partner)
				.executeUpdate();
	}
}
