package tb.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import tb.admin.model.CarModel;
import tb.admin.model.MapAreaModel;
import tb.car.CarSynch;
import tb.car.dao.CarDao;
import tb.car.domain.Car;
import tb.car.domain.CarState;
import tb.car.domain.LastGeoData;
import tb.dao.IMapAreaDao;
import tb.domain.Partner;
import tb.domain.maparea.MapArea;
import tb.domain.order.VehicleClass;
import tb.service.PartnerService;
import tb.service.Starter;

@RequestMapping("/partner")
@Controller
public class PartnerController {
	@Value("#{mainSettings['offerorder.cars.actual.geo.minutes']}")
	private Integer actualGeoMinutes;

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

	@Transactional
	@RequestMapping(value = "/cars", method = RequestMethod.GET)
	public String cars(@RequestParam("id") Long partnerId, Model model) {
		Date d = new Date();
		d = new Date(d.getTime() - (actualGeoMinutes * 60 * 1000));

		List<Object[]> list = carDao.getCarsWithCarStates(partnerId);
		List<LastGeoData> lastGeoDatas = carDao.getLastGeoDatas(partnerId);
		List<CarModel> carModels = new ArrayList<>(list.size());
		LastGeoData lastGeoDataClear = new LastGeoData();
		for (Object[] objects : list) {
			CarState a = (CarState) objects[0];
			Car b = (Car) objects[1];

			LastGeoData c;
			Optional<LastGeoData> olgd = lastGeoDatas.stream()
					.filter(p -> p.getUuid().equals(a.getUuid()))
					.findFirst();
			if (olgd.isPresent()) {
				c = olgd.get();
			} else {
				c = lastGeoDataClear;
				c.setDate(new Date(0));
				c.setLon(0D);
				c.setLat(0D);
			}

			CarModel car = new CarModel();
			car.setDisp(b.getRealName());
			car.setUuid(b.getUuid());
			car.setName(b.getDriverDisplayName());
			car.setState(a.getState().toString());
			car.setDate(c.getDate());
			car.setLat(c.getLat());
			car.setLon(c.getLon());

			StringBuilder req = new StringBuilder();
			for (Entry<String, String> entry : b.getCarRequires().entrySet()) {
				req.append(entry.getKey() + "=" + entry.getValue() + ", ");
			}
			car.setRequirmets(req.toString());

			StringBuilder carClass = new StringBuilder();
			for (VehicleClass vehicleClass : b.getVehicleClasses()) {
				carClass.append(vehicleClass.name() + ", ");
			}
			car.setCarclass(carClass.toString());

			if (c.getDate().before(d)) {
				car.setGeoObsolete(true);
			}

			carModels.add(car);
		}

		carModels = carModels.stream()
				.sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate()))
				.collect(Collectors.toList());

		model.addAttribute("cars", carModels);
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
			@RequestParam(value = "customCarOptions", required = false, defaultValue = "false") Boolean customCarOptions,
			@RequestParam("codeName") String codeName,
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
		partner.setCustomCarOptions(customCarOptions);
		partner.setCodeName(codeName == "" ? null : codeName);
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
		model.addAttribute("customCarOptions", partner.getCustomCarOptions());
		model.addAttribute("codeName", partner.getCodeName());

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
			@RequestParam("mapareas") String[] mapAreaIds,
			@RequestParam(value = "customCarOptions", required = false, defaultValue = "false") Boolean customCarOptions,
			@RequestParam("codeName") String codeName) {

		Set<MapArea> mapAreasSet = new HashSet<MapArea>();
		for (String mapAreaId : mapAreaIds) {
			MapArea mapArea = new MapArea();
			mapArea.setId(Long.parseLong(mapAreaId));
			mapAreasSet.add(mapArea);
		}

		partnerService.update(partnerId, apiId, apiKey, name, apiUrl, timezoneId, mapAreasSet, customCarOptions,
				codeName == "" ? null : codeName);
		return "redirect:list";
	}
}
