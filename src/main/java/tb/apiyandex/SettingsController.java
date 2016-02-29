package tb.apiyandex;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import tb.dao.IPartnerDao;
import tb.domain.Partner;
import tb.domain.PartnerSettings;
import tb.service.PartnerService;
import tb.service.exceptions.PartnerNotFoundException;

@Controller("apiyandexSettings")
@RequestMapping("/settings")
public class SettingsController {
	@Autowired
	private PartnerService partnerService;
	@Autowired
	private IPartnerDao partnerDao;

	@RequestMapping(value = "")
	public void index(@RequestBody String str, HttpServletResponse response, @RequestParam("clid") String clid,
			@RequestParam("apikey") String apikey) {

		try {
			Partner partner = partnerDao.getByApiId(clid);
			if (partner == null) {
				throw new PartnerNotFoundException(clid);
			}

			if (!partner.getApiKey().equals(apikey)) {
				throw new PartnerNotFoundException(clid);
			}

			PartnerSettings settings = new PartnerSettings();
			settings.setPartner(partner);
			settings.setSettings(str);
			partnerService.savePartnerCarOptions(settings);

		} catch (PartnerNotFoundException e) {
			response.setStatus(403);
		}
	}
}
