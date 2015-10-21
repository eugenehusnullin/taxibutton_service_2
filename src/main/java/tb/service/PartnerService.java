package tb.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tb.dao.IPartnerDao;
import tb.domain.Partner;
import tb.domain.TariffType;
import tb.domain.maparea.MapArea;

@Service
public class PartnerService {

	@Autowired
	private IPartnerDao partnerDao;
	// @Autowired
	// private IMapAreaDao mapAreaDao;
	@Autowired
	private MapAreaAssist mapAreaAssist;

	@Transactional
	public List<Partner> getAll() {
		return partnerDao.getAll();
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
	public void update(Long partnerId, String apiId, String apiKey, String name, String apiUrl, TariffType tariffType,
			String tariffUrl, String driverUrl, String timezoneId, String mapareaUrl,
			String costUrl, Set<MapArea> mapAreasSet) {
		Partner partner = partnerDao.get(partnerId);

		partner.setApiId(apiId);
		partner.setApiKey(apiKey);
		partner.setApiurl(apiUrl);
		partner.setName(name);
		partner.setTariffType(tariffType);
		partner.setTariffUrl(tariffUrl);
		partner.setDriverUrl(driverUrl);
		partner.setTimezoneId(timezoneId);
		partner.setMapareaUrl(mapareaUrl);
		partner.setCostUrl(costUrl);
		partner.setMapAreas(mapAreasSet);
		partnerDao.saveOrUpdate(partner);
	}

	@Transactional
	public Partner getByUuid(String uuid) {
		return partnerDao.get(uuid);
	}
}
