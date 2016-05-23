package tb.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import tb.admin.model.BrandModel;
import tb.admin.model.BrandServiceModel;
import tb.dao.IBrandDao;
import tb.domain.Brand;
import tb.domain.BrandService;
import tb.domain.Partner;
import tb.service.BrandingService;
import tb.service.PartnerService;

@RequestMapping("/brand")
@Controller
public class BrandController {
	@Autowired
	private BrandingService brandingService;
	@Autowired
	private PartnerService partnerService;
	@Autowired
	private IBrandDao brandDao;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String list(Model model) {
		List<BrandModel> list = brandingService.getBrandModels();
		model.addAttribute("brands", list);
		return "brand/list";
	}

	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public String edit(@RequestParam(name = "id", required = false, defaultValue = "0") Long brandId, Model model) {

		List<BrandServiceModel> bsms = prepareBsm();
		Brand brand;
		if (brandId == 0) {
			brand = new Brand();
			brand.setId(0L);

		} else {
			brand = brandDao.get(brandId);

			for (BrandService brandService : brand.getServices()) {
				Optional<BrandServiceModel> option = bsms.stream()
						.filter(p -> p.getPartnerId() == brandService.getPartner().getId())
						.findFirst();
				if (option.isPresent()) {
					BrandServiceModel brandServiceModel = option.get();
					brandServiceModel.setPriority(brandService.getPriority());
					brandServiceModel.setMajor(brandService.getMajor());
				}
			}
		}

		model.addAttribute("brand", brand);
		model.addAttribute("services", bsms);

		return "brand/edit";
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String edit(HttpServletRequest request, Model model) {

		Long majorPartnerId = Long.parseLong(request.getParameter("major"));

		Long brandId = Long.parseLong(request.getParameter("brandId"));
		String brandName = request.getParameter("name");

		List<BrandServiceModel> brandServiceModels = new ArrayList<>();
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			String strPartnerid = request.getParameter("service_" + i + "_partnerid");
			if (strPartnerid == null) {
				break;
			}

			Long partnerId = Long.parseLong(strPartnerid);

			String strPriority = request.getParameter("service_" + i + "_priority");
			if (strPriority == null || strPriority.isEmpty()) {
				continue;
			}
			int priority = Integer.parseInt(strPriority);
			if (priority < 1) {
				continue;
			}

			BrandServiceModel bsm = new BrandServiceModel();
			bsm.setPartnerId(partnerId);
			bsm.setPriority(priority);
			bsm.setMajor(partnerId == majorPartnerId);

			brandServiceModels.add(bsm);
		}

		brandingService.editBrand(brandId, brandName, brandServiceModels);

		return "redirect:list";
	}

	private List<BrandServiceModel> prepareBsm() {
		List<Partner> partners = partnerService.getAll();
		List<BrandServiceModel> bsms = new ArrayList<>();
		for (Partner partner : partners) {
			BrandServiceModel brandServiceModel = new BrandServiceModel();
			bsms.add(brandServiceModel);

			brandServiceModel.setPartnerId(partner.getId());
			brandServiceModel.setPartnerName(partner.getName());
			brandServiceModel.setPriority(0);
			brandServiceModel.setMajor(false);
		}
		return bsms;
	}
}
