package tb.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tb.dao.IBrandDao;
import tb.domain.Brand;
import tb.domain.BrandService;
import tb.domain.Partner;

@Service
public class BrandingService {

	private static final Logger logger = LoggerFactory.getLogger(BrandingService.class);

	@Autowired
	private IBrandDao brandDao;

	@Transactional
	public Partner getMajorPartner(String brandName) {
		Brand brand = brandDao.get(brandName);
		if (brand != null) {
			Optional<BrandService> optional = brand.getServices().stream()
					.filter(p -> p.getMajor() != null && p.getMajor() == true)
					.findFirst();

			if (optional.isPresent()) {
				return optional.get().getPartner();
			}
		}

		logger.warn("Brand: " + brandName + " - not exists.");
		return null;

	}

}
