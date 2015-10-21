package tb.admin;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import tb.service.TariffService;

@RequestMapping("/tariff")
@Controller
public class TariffController {

	@Autowired
	private TariffService tariffService;

	@RequestMapping(value = "/tariff", method = RequestMethod.GET)
	public String tariff(@RequestParam("id") Long partnerId, Model model) {

		String tariff = tariffService.getTariff(partnerId);
		model.addAttribute("tariff", tariff);
		model.addAttribute("partnerId", partnerId);
		return "tariff/tariff";
	}

	@RequestMapping(value = "/tariff", method = RequestMethod.POST)
	public String tariff(@RequestParam("tariff") String tariff, @RequestParam("partnerId") Long partnerId, HttpServletResponse response) {

		return "redirect:../partner/list";
	}
}
