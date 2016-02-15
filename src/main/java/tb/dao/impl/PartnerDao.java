package tb.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import tb.dao.IPartnerDao;
import tb.domain.Partner;
import tb.domain.PartnerSettings;

@Repository("PartnerDao")
public class PartnerDao implements IPartnerDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public Partner get(Long id) {
		return (Partner) sessionFactory.getCurrentSession().get(Partner.class, id);
	}

	@Override
	public Partner get(String uuid) {
		return (Partner) sessionFactory.getCurrentSession()
				.createCriteria(Partner.class)
				.add(Restrictions.eq("uuid", uuid))
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Partner> getAll() {
		return sessionFactory.getCurrentSession().createCriteria(Partner.class).list();
	}

	@Override
	public void save(Partner partner) {
		sessionFactory.getCurrentSession().save(partner);
	}

	@Transactional
	@Override
	public Partner getByApiId(String id) {
		return (Partner) sessionFactory.getCurrentSession().createCriteria(Partner.class)
				.add(Restrictions.eq("apiId", id)).uniqueResult();
	}

	@Override
	public Partner getByApiId(String apiId, String apiKey) {
		return (Partner) sessionFactory.getCurrentSession().createCriteria(Partner.class)
				.add(Restrictions.eq("apiId", apiId))
				.add(Restrictions.eq("apiKey", apiKey)).uniqueResult();
	}

	@Override
	public void delete(Partner partner) {
		sessionFactory.getCurrentSession().delete(partner);
	}

	@Override
	public void saveOrUpdate(Partner partner) {
		sessionFactory.getCurrentSession().saveOrUpdate(partner);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<Partner> getActive() {
		return sessionFactory.getCurrentSession().createCriteria(Partner.class).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Partner> getPartnersNeedMapareaSynch() {
		return sessionFactory.getCurrentSession().createCriteria(Partner.class)
				.add(Restrictions.isNotNull("mapareaUrl"))
				.add(Restrictions.isNotEmpty("mapareaUrl"))
				.list();
	}

	@Transactional
	@Override
	public void saveSettings(PartnerSettings settings) {
		sessionFactory.getCurrentSession().saveOrUpdate(settings);		
	}

	@Override
	public Partner getByCodeName(String codeName) {
		return (Partner) sessionFactory.getCurrentSession()
				.createCriteria(Partner.class)
				.add(Restrictions.eq("codeName", codeName))
				.uniqueResult();
	}
	
	@Override
	public PartnerSettings getPartnerSettings(Partner partner) {
		return (PartnerSettings) sessionFactory.getCurrentSession()
				.createCriteria(PartnerSettings.class)
				.add(Restrictions.eq("partner", partner))
				.uniqueResult();
	}
}
