package tb.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import tb.admin.model.MapAreaModel;
import tb.car.CarSynch;
import tb.car.dao.CarDao;
import tb.dao.IMapAreaDao;
import tb.domain.Partner;
import tb.domain.maparea.MapArea;
import tb.service.PartnerService;
import tb.service.Starter;

@RequestMapping("/partner")
@Controller
public class PartnerController {

	@Autowired
	private PartnerService partnerService;
	// @Autowired
	// private TariffService tariffService;
	@Autowired
	private Starter starter;
	@Autowired
	private CarSynch carSynch;
	@Autowired
	private IMapAreaDao mapAreaDao;
	@Autowired
	private CarDao carDao;

	@RequestMapping(value = "/carsynch", method = RequestMethod.GET)
	public String carSynch(Model model) {
		Date d = new Date(new Date().getTime() + 3000);
		starter.schedule(carSynch::synch, d);

		model.addAttribute("result", "Car synch started, wait some seconds. And you can see result in log files.");
		return "result";
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String list(Model model) {
		List<Partner> list = partnerService.getAll();
		model.addAttribute("partners", list);
		return "partner/list";
	}

	@RequestMapping(value = "/cars", method = RequestMethod.GET)
	public String cars(@RequestParam("id") Long partnerId, Model model) {
		List<?> list = carDao.getCarsWithCarStates(partnerId);
		model.addAttribute("cars", list);
		return "partner/cars";
	}

	@RequestMapping(value = "/create", method = RequestMethod.GET)
	public String create(Model model) {
		List<MapAreaModel> mapAreaModels = new ArrayList<MapAreaModel>();
		List<MapArea> mapAreas = mapAreaDao.getAll();
		for (MapArea mapArea : mapAreas) {
			MapAreaModel mapAreaModel = new MapAreaModel();
			mapAreaModel.setId(mapArea.getId());
			mapAreaModel.setName(mapArea.getName());
			mapAreaModels.add(mapAreaModel);
		}
		model.addAttribute("mapareas", mapAreaModels);

		return "partner/create";
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String create(@RequestParam("name") String name,
			@RequestParam("apiurl") String apiurl,
			@RequestParam("apiId") String apiId,
			@RequestParam("apiKey") String apiKey,
			@RequestParam("timezoneId") String timezoneId,
			@RequestParam("mapareas") String[] mapAreaIds,
			Model model) {

		Set<MapArea> mapAreasSet = new HashSet<MapArea>();
		for (String mapAreaId : mapAreaIds) {
			MapArea mapArea = new MapArea();
			mapArea.setId(Long.parseLong(mapAreaId));
			mapAreasSet.add(mapArea);
		}

		Partner partner = new Partner();
		partner.setName(name);
		partner.setApiurl(apiurl);
		partner.setApiId(apiId);
		partner.setApiKey(apiKey);
		partner.setTimezoneId(timezoneId);
		partner.setMapAreas(mapAreasSet);
		partnerService.add(partner);

		return "redirect:list";
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public String delete(@RequestParam("id") Long partnerId) {
		partnerService.delete(partnerId);
		return "redirect:list";
	}

	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public String edit(@RequestParam("id") Long partnerId, Model model) {

		Partner partner = partnerService.get(partnerId);

		model.addAttribute("partnerId", partnerId);
		model.addAttribute("apiId", partner.getApiId());
		model.addAttribute("apiKey", partner.getApiKey());
		model.addAttribute("name", partner.getName());
		model.addAttribute("apiUrl", partner.getApiurl());
		model.addAttribute("timezoneId", partner.getTimezoneId());

		List<MapAreaModel> mapAreaModels = new ArrayList<MapAreaModel>();
		List<MapArea> mapAreas = mapAreaDao.getAll();
		for (MapArea mapArea : mapAreas) {
			MapAreaModel mapAreaModel = new MapAreaModel();
			mapAreaModel.setId(mapArea.getId());
			mapAreaModel.setName(mapArea.getName());
			mapAreaModels.add(mapAreaModel);
		}
		model.addAttribute("mapareas", mapAreaModels);

		List<Long> mapAreasIds = new ArrayList<Long>();
		partner.getMapAreas().stream().forEach(a -> mapAreasIds.add(a.getId()));
		model.addAttribute("mapareasids", mapAreasIds);

		return "partner/edit";
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String edit(@RequestParam("partnerId") Long partnerId,
			@RequestParam("apiId") String apiId,
			@RequestParam("apiKey") String apiKey,
			@RequestParam("name") String name,
			@RequestParam("apiUrl") String apiUrl,
			@RequestParam("timezoneId") String timezoneId,
			@RequestParam("mapareas") String[] mapAreaIds) {

		Set<MapArea> mapAreasSet = new HashSet<MapArea>();
		for (String mapAreaId : mapAreaIds) {
			MapArea mapArea = new MapArea();
			mapArea.setId(Long.parseLong(mapAreaId));
			mapAreasSet.add(mapArea);
		}

		partnerService.update(partnerId, apiId, apiKey, name, apiUrl, timezoneId, mapAreasSet);
		return "redirect:list";
	}
}
