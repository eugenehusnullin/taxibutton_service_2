package tb.service;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
	private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

	@Value("#{mainSettings['sms.url.old']}")
	private String smsOldUrl;
	@Value("#{mainSettings['sms.url']}")
	private String smsUrl;

	public boolean send(String taxi, String reciever, String msg) {
		String body = createOutput(reciever, msg);

		try {
			String smsGatewayUrl = smsOldUrl;
			if (!taxi.isEmpty()) {
				smsGatewayUrl = smsUrl.replace("TAXIID", taxi);
			}
			URL obj = new URL(smsGatewayUrl);

			HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			connection.setDoOutput(true);
			IOUtils.write(body.getBytes("UTF-8"), connection.getOutputStream());
			IOUtils.closeQuietly(connection.getOutputStream());

			int responceCode = connection.getResponseCode();
			String response = IOUtils.toString(connection.getInputStream());
			if (responceCode != 200) {
				logger.error(response);
				return false;
			} else {
				logger.debug(smsGatewayUrl);
				logger.debug(body);
				logger.debug(response);
				return true;
			}
		} catch (Exception e) {
			logger.error("sms exception.", e);
		}
		return false;
	}

	public String createOutput(String reciever, String msg) {
		JSONObject jsonMsg = new JSONObject();
		jsonMsg.put("sendNumber", reciever);
		jsonMsg.put("sendText", msg);

		JSONArray array = new JSONArray();
		array.put(jsonMsg);

		return array.toString();
	}

}
