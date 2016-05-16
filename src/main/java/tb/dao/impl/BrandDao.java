package tb.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import tb.dao.IBrandDao;
import tb.domain.Brand;
import tb.domain.BrandService;
import tb.domain.Partner;

@Repository("BrandDao")
public class BrandDao implements IBrandDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Transactional
	@Override
	public Brand get(String codeName) {
		return (Brand) sessionFactory.getCurrentSession()
				.createCriteria(Brand.class)
				.add(Restrictions.eq("codeName", codeName))
				.uniqueResult();
	}

	@Transactional
	@SuppressWarnings("unchecked")
	public List<BrandService> getBrandServices(Partner partner) {
		return sessionFactory.getCurrentSession()
				.createCriteria(BrandService.class)
				.add(Restrictions.eq("partner", partner))
				.list();
	}

}
