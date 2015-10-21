package tb.apidevice;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import tb.domain.Partner;
import tb.domain.maparea.MapArea;
import tb.maparea.MapareaSerializer;
import tb.service.PartnerService;

@RequestMapping("/maparea")
@Controller("apiDeviceMapareaController")
public class MapareaController {
	@Autowired
	private PartnerService partnerService;
	@Autowired
	private MapareaSerializer mapareaSerializer;

	@RequestMapping(value = "/getmapareas", method = RequestMethod.POST)
	@Transactional
	public void getMapareas(HttpServletRequest request, HttpServletResponse response) {
		try {
			JSONObject requestJson = (JSONObject) new JSONTokener(IOUtils.toString(request.getInputStream(), "UTF-8"))
					.nextValue();

			String uuid = requestJson.optString("uuid");
			if (uuid != null) {
				Partner partner = partnerService.getByUuid(uuid);
				if (partner != null) {
					Set<MapArea> mapAreas = partner.getMapAreas();
					JSONObject result = mapareaSerializer.serialize(mapAreas);
					IOUtils.write(result.toString(), response.getOutputStream(), "UTF-8");
				}
			}
		} catch (Exception e) {

		}
	}
}
