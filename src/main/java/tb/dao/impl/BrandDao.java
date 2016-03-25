package tb.dao.impl;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import tb.dao.IBrandDao;
import tb.domain.Brand;

@Repository("BrandDao")
public class BrandDao implements IBrandDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public Brand get(String codeName) {
		return (Brand) sessionFactory.getCurrentSession()
				.createCriteria(Brand.class)
				.add(Restrictions.eq("codeName", codeName))
				.uniqueResult();
	}

}
