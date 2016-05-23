package tb.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tb.admin.model.BrandModel;
import tb.admin.model.BrandServiceModel;
import tb.dao.IBrandDao;
import tb.domain.Brand;
import tb.domain.BrandService;
import tb.domain.Partner;

@Service
public class BrandingService {

	private static final Logger logger = LoggerFactory.getLogger(BrandingService.class);

	@Autowired
	private IBrandDao brandDao;
	@Autowired
	private PartnerService partnerService;

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

	@Transactional
	public List<BrandModel> getBrandModels() {
		List<Brand> brands = brandDao.getAll();

		List<BrandModel> brandModels = new ArrayList<>(brands.size());
		for (Brand brand : brands) {
			String partners = brand.getServices().stream()
					.map(p -> (p.getMajor() ? "$_" : "") + p.getPartner().getName() + " - " + p.getPriority())
					.collect(Collectors.joining("; "));

			BrandModel brandModel = new BrandModel(brand);
			brandModel.setPartners(partners);

			brandModels.add(brandModel);
		}

		return brandModels;
	}

	@Transactional
	public void editBrand(Long brandId, String brandName, List<BrandServiceModel> brandServiceModels) {
		Brand brand;
		if (brandId == 0) {
			brand = new Brand();
			brand.setServices(new HashSet<>());
		} else {
			brand = brandDao.get(brandId);
			brandDao.deleteBrandServices(brand);
		}
		brand.setCodeName(brandName);

		for (BrandServiceModel bsm : brandServiceModels) {
			Partner partner = partnerService.get(bsm.getPartnerId());

			BrandService bs = new BrandService();
			bs.setBrand(brand);
			bs.setPartner(partner);
			bs.setPriority(bsm.getPriority());
			bs.setMajor(bsm.isMajor());

			brand.getServices().add(bs);
		}

		brandDao.saveBrand(brand);
	}

}
