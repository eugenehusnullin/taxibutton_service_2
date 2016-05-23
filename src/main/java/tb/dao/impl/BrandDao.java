package tb.dao.impl;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
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

	@Transactional
	@SuppressWarnings("unchecked")
	@Override
	public List<Brand> getAll() {
		return (List<Brand>) sessionFactory.getCurrentSession()
				.createCriteria(Brand.class)
				.list();
	}

	@Transactional
	@Override
	public Brand get(Long brandId) {
		return (Brand) sessionFactory.getCurrentSession().get(Brand.class, brandId);
	}

	@Override
	public void saveBrand(Brand brand) {
		sessionFactory.getCurrentSession().saveOrUpdate(brand);
	}

	@Override
	public void deleteBrandServices(Brand brand) {
		Session s = sessionFactory.getCurrentSession();
		Iterator<BrandService> iterator = brand.getServices().iterator();
		while (iterator.hasNext()) {
			BrandService bs = iterator.next();
			iterator.remove();
			s.delete(bs);
		}
	}
}
