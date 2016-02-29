package tb.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tb.dao.IPartnerDao;
import tb.domain.Partner;
import tb.domain.PartnerSettings;
import tb.domain.maparea.MapArea;
import tb.domain.order.Requirement;

@Service
public class PartnerService {

	@Autowired
	private IPartnerDao partnerDao;
	@Autowired
	private MapAreaAssist mapAreaAssist;
	private Map<Long, List<Requirement>> requirements = new HashMap<>();

	@Transactional
	public List<Partner> getAll() {
		return partnerDao.getAll();
	}

	@Transactional
	public void savePartnerCarOptions(PartnerSettings settings) {
		holdCarOptions(settings.getPartner().getId(), settings.getSettings());
		partnerDao.saveSettings(settings);
	}

	public void holdCarOptions(Long partnerId, String settings) {
		requirements.put(partnerId, createRequirements(settings));
	}

	public List<Requirement> getCarOptions(Long partnerId) {
		return requirements.get(partnerId);
	}

	private List<Requirement> createRequirements(String settings) {
		JSONObject settingsJSON = (JSONObject) new JSONTokener(settings).nextValue();
		JSONArray array = settingsJSON.getJSONArray("requirements");
		List<Requirement> updatedRequirements = new ArrayList<>(array.length());
		for (int i = 0; i < array.length(); i++) {
			JSONObject jsonObject = array.getJSONObject(i);
			Requirement req = new Requirement();
			req.setType(jsonObject.getString("code"));
			req.setNeedCarCheck(jsonObject.getString("classType").equals("Car"));
			updatedRequirements.add(req);
		}

		return updatedRequirements;
	}

	public String loadDefaultCarOptions() throws IOException {
		FileInputStream fis = (FileInputStream) getCarOptionsResource();
		return IOUtils.toString(fis, "UTF-8");
	}

	private InputStream getCarOptionsResource() throws FileNotFoundException {
		URL url = getClass().getResource("/CarOptions.json");
		File file = new File(url.getFile());
		return new FileInputStream(file);
	}

	public boolean isNeedCarCheck(Long partnerId, String reqName) throws IOException {
		List<Requirement> reqs = requirements.get(partnerId);
		if (reqs == null) {
			String s;
			if (partnerId == 0L) {
				s = loadDefaultCarOptions();
			} else {
				Partner partner = get(partnerId);
				PartnerSettings ps = getPartnerSettings(partner);
				s = ps.getSettings();
			}
			holdCarOptions(partnerId, s);
			reqs = requirements.get(partnerId);
		}
		Requirement req = reqs.stream()
				.filter(p -> p.getType().equals(reqName))
				.findFirst()
				.get();
		return req.getNeedCarCheck();
	}

	@Transactional
	public List<Partner> getPartnersByMapAreas(double lat, double lon) {
		List<Partner> partners = partnerDao.getAll();
		List<Partner> result = new ArrayList<Partner>();
		for (Partner partner : partners) {
			for (MapArea mapArea : partner.getMapAreas()) {
				if (mapAreaAssist.contains(mapArea, lat, lon)) {
					result.add(partner);
					break;
				}
			}
		}
		return result;
	}

	@Transactional
	public void add(Partner partner) {
		partner.setUuid(UUID.randomUUID().toString());
		partnerDao.save(partner);
	}

	@Transactional
	public void delete(Long partnerId) {
		Partner partner = partnerDao.get(partnerId);
		partnerDao.delete(partner);
	}

	@Transactional
	public Partner get(Long partnerId) {
		return partnerDao.get(partnerId);
	}

	@Transactional
	public void update(Long partnerId, String apiId, String apiKey, String name, String apiUrl, String timezoneId,
			Set<MapArea> mapAreasSet, String codeName) {
		Partner partner = partnerDao.get(partnerId);

		partner.setApiId(apiId);
		partner.setApiKey(apiKey);
		partner.setApiurl(apiUrl);
		partner.setName(name);
		partner.setTimezoneId(timezoneId);
		partner.setMapAreas(mapAreasSet);
		partner.setCodeName(codeName);
		partnerDao.saveOrUpdate(partner);
	}

	@Transactional
	public Partner getByUuid(String uuid) {
		return partnerDao.get(uuid);
	}

	@Transactional
	public Partner getByCodeName(String codeName) {
		return partnerDao.getByCodeName(codeName);
	}

	@Transactional
	public PartnerSettings getPartnerSettings(Partner partner) {
		return partnerDao.getPartnerSettings(partner);
	}
}
