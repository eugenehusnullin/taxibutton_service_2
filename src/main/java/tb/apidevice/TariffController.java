package tb.apidevice;

import java.io.DataOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import tb.service.TariffService;
import tb.service.exceptions.DeviceNotFoundException;

@RequestMapping("/tariff")
@Controller("apiDeviceTariffController")
public class TariffController {
	private static final Logger log = LoggerFactory.getLogger(TariffController.class);

	@Autowired
	private TariffService tariffService;

	@RequestMapping(value = "/get", method = RequestMethod.POST)
	public void getAll(HttpServletRequest request, HttpServletResponse response) {

		try {
			JSONObject requestJson = (JSONObject) new JSONTokener(IOUtils.toString(request.getInputStream(), "UTF-8"))
					.nextValue();

			try {
				JSONArray responseJson = tariffService.getByPartner(requestJson);
				DataOutputStream outputStream = new DataOutputStream(response.getOutputStream());
				byte[] bytes = responseJson.toString().getBytes("UTF-8");
				outputStream.write(bytes);
				outputStream.flush();
				outputStream.close();
			} catch (DeviceNotFoundException e) {
				response.setStatus(403);
			}
		} catch (IOException e) {
			response.setStatus(500);
			log.error("apiDeviceTariffController.getAll", e);
		}
	}
}
