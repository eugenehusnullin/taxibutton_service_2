package tb.dao;

import java.util.List;

import tb.domain.Brand;
import tb.domain.BrandService;
import tb.domain.Partner;

public interface IBrandDao {
	Brand get(String codeName);

	List<BrandService> getBrandServices(Partner partner);
}
