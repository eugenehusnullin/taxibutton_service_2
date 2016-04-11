package tb.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tb.admin.model.DeviceModel;
import tb.dao.IDeviceDao;
import tb.domain.Device;

@Service
public class DeviceService {
	@Autowired
	private IDeviceDao deviceDao;
	@Autowired
	private SmsService smsService;

	@Transactional
	public JSONObject register(JSONObject registerJson) {
		JSONObject resultJson = new JSONObject();

		String phone = registerJson.optString("phone");
		String userName = registerJson.optString("name");

		if (phone.isEmpty()) {
			resultJson.put("result", "ERROR");
			resultJson.put("description", "bad params (not exist phone)");
			return resultJson;
		}

		String taxi = registerJson.optString("taxi");

		Device device = null;
		if (taxi.isEmpty()) {
			device = deviceDao.getByPhone(phone);
		} else {
			device = deviceDao.get(phone, taxi);
		}

		String newDeviceUuid = UUID.randomUUID().toString();
		device = new Device();
		device.setApiId(newDeviceUuid);
		device.setPhone(phone);
		device.setTaxi(taxi.isEmpty() ? null : taxi);
		device.setRegDate(new Date());
		device.setUserName(userName);

		boolean hasConfirm = registerJson.has("confirm");
		if ((hasConfirm && registerJson.getBoolean("confirm")) || !hasConfirm) {
			int keyInt = (new Random()).nextInt(9999 - 1000) + 1000;
			device.setConfirmKey(Integer.toString(keyInt));
			device.setConfirmDate(new Date());
			boolean smsResult = smsService.send(taxi, phone,
					"Ваш код активации для приложения вызова такси " + Integer.toString(keyInt));
			resultJson.put("result", smsResult ? "WAITSMS" : "ERROR");
		} else {
			resultJson.put("result", "OK");
			resultJson.put("apiId", device.getApiId());
		}

		deviceDao.save(device);

		return resultJson;
	}

	@Transactional
	public JSONObject confirm(JSONObject confirmJson) {
		JSONObject resultJson = new JSONObject();

		String phone = confirmJson.optString("phone");
		String key = confirmJson.optString("key");

		if (phone.isEmpty() || key.isEmpty()) {
			resultJson.put("result", "ERROR");
			resultJson.put("description", "bad params (not exist phone or key)");
			return resultJson;
		}

		String taxi = confirmJson.optString("taxi");

		Device device = null;
		if (taxi.isEmpty()) {
			device = deviceDao.getByPhone(phone);
		} else {
			device = deviceDao.get(phone, taxi);
		}

		if (device == null) {
			resultJson.put("result", "ERROR");
			resultJson.put("description", "phone not found");
			return resultJson;
		}

		if (!device.getConfirmKey().equals(key)) {
			resultJson.put("result", "WRONGKEY");
			return resultJson;
		} else {
			device.setConfirmedDate(new Date());
			deviceDao.save(device);
			resultJson.put("result", "OK");
			resultJson.put("apiId", device.getApiId());
			return resultJson;
		}
	}

	@Transactional
	public List<DeviceModel> getAll() {
		List<Device> devices = deviceDao.getAll();

		List<DeviceModel> models = new ArrayList<>();
		for (Device device : devices) {
			DeviceModel model = new DeviceModel();
			model.setId(device.getId());
			model.setApiId(device.getApiId());
			model.setPhone(device.getPhone());
			model.setConfirmKey(device.getConfirmKey());
			model.setTaxi(device.getTaxi());
			model.setUserName(device.getUserName());

			models.add(model);
		}

		return models;
	}
}
