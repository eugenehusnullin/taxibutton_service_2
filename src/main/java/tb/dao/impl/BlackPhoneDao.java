package tb.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import tb.dao.IBlackPhoneDao;
import tb.domain.Partner;
import tb.domain.phone.BlackPhone;

@Repository("BlackPhoneDao")
public class BlackPhoneDao implements IBlackPhoneDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public BlackPhone get(Long id) {
		return (BlackPhone) sessionFactory.getCurrentSession().get(
				BlackPhone.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BlackPhone> get(Partner partner) {
		return sessionFactory.getCurrentSession()
				.createCriteria(BlackPhone.class)
				.add(Restrictions.eq("partner", partner)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BlackPhone> getAll() {
		return sessionFactory.getCurrentSession()
				.createCriteria(BlackPhone.class).list();
	}

	@Override
	public void save(BlackPhone phone) {
		sessionFactory.getCurrentSession().save(phone);
	}

	@Override
	public void saveOrUpdate(BlackPhone phone) {
		sessionFactory.getCurrentSession().saveOrUpdate(phone);
	}

}
