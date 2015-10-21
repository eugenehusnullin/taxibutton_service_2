package tb.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import tb.dao.ITariffDefinitionMapAreaDao;
import tb.domain.TariffDefinitionMapArea;

@Repository("TariffDefinitionMapAreaDao")
public class TariffDefinitionMapAreaDao implements ITariffDefinitionMapAreaDao {
	@Autowired
	private SessionFactory sessionFactory;

	@Transactional
	@Override
	public void add(TariffDefinitionMapArea tariffDefinitionMapArea) {
		sessionFactory.getCurrentSession().save(tariffDefinitionMapArea);
	}

	@Transactional
	@SuppressWarnings("unchecked")
	@Override
	public List<TariffDefinitionMapArea> getAll() {
		return sessionFactory.getCurrentSession().createCriteria(TariffDefinitionMapArea.class).list();
	}

	@Transactional
	@Override
	public void delete(String name) {
		String delete = "delete TariffDefinitionMapArea where name = :name";
		sessionFactory.getCurrentSession().createQuery(delete)
				.setString("name", name)
				.executeUpdate();
	}

	@Transactional
	@Override
	public TariffDefinitionMapArea get(String name) {
		return (TariffDefinitionMapArea) sessionFactory.getCurrentSession()
				.createCriteria(TariffDefinitionMapArea.class)
				.add(Restrictions.idEq(name))
				.uniqueResult();
	}

	@Transactional
	@Override
	public void update(TariffDefinitionMapArea tariffDefinitionMapArea) {
		sessionFactory.getCurrentSession().update(tariffDefinitionMapArea);
	}
}
