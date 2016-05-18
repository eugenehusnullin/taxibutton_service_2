package tb.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import tb.admin.model.BrandModel;
import tb.service.BrandingService;

@RequestMapping("/brand")
@Controller
public class BrandController {
	@Autowired
	private BrandingService brandingService;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String list(Model model) {
		List<BrandModel> list = brandingService.getBrandModels();
		model.addAttribute("brands", list);
		return "brand/list";
	}
}
