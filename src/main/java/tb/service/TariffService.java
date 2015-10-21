package tb.service;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tb.dao.IPartnerDao;
import tb.dao.IDeviceDao;
import tb.dao.ITariffDao;
import tb.domain.Partner;
import tb.domain.Device;
import tb.domain.Tariff;
import tb.service.exceptions.DeviceNotFoundException;

@Service
public class TariffService {
	@Autowired
	private ITariffDao tariffDao;
	@Autowired
	private IDeviceDao deviceDao;
	@Autowired
	private IPartnerDao partnerDao;

	@Transactional
	public JSONArray getByPartner(JSONObject requestJson) throws DeviceNotFoundException {

		String apiId = requestJson.optString("apiId");
		Device device = deviceDao.get(apiId);
		if (device == null) {
			throw new DeviceNotFoundException(apiId);
		}

		String uuid = requestJson.optString("uuid");
		Partner partner = partnerDao.get(uuid);

		List<Tariff> tariffs = tariffDao.get(partner);
		JSONArray tariffsJsonArray = new JSONArray();
		for (Tariff tariff : tariffs) {
			JSONObject tariffJson = new JSONObject();
			tariffJson.put("partnerId", tariff.getPartner().getUuid());
			tariffJson.put("tariff", tariff.getTariff());
			tariffsJsonArray.put(tariffJson);
		}
		return tariffsJsonArray;
	}

	@Transactional
	public String getTariff(Long partnerId) {
		Partner partner = partnerDao.get(partnerId);
		Tariff simpleTariff = tariffDao.get(partner).get(0);
		return simpleTariff.getTariff();
	}
}
