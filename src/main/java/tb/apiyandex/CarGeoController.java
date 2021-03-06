package tb.apiyandex;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import tb.car.CarGeoBuilder;
import tb.car.dao.CarDao;
import tb.car.domain.GeoData;
import tb.dao.IPartnerDao;
import tb.domain.Partner;
import tb.utils.XmlUtils;

@Controller("apiyandexCarGeoController")
@RequestMapping("/cargeo")
public class CarGeoController {
	private static final Logger logger = LoggerFactory.getLogger(CarGeoController.class);

	@Autowired
	private CarGeoBuilder carGeoBuilder;
	@Autowired
	private IPartnerDao partnerDao;
	@Autowired
	private CarDao carDao;

	@RequestMapping(value = "", method = RequestMethod.POST)
	public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			try {
				String paramData = request.getParameter("data");
				logger.debug(paramData);
				Document document = XmlUtils.buildDomDocument(paramData);
				String partnerClid = carGeoBuilder.defineCarStateGeosPartnerClid(document);
				Partner partner = partnerDao.getByApiId(partnerClid);
				if (partner != null) {
					List<GeoData> geoDatas = carGeoBuilder.createCarGeos(document, partner);
					carDao.updateCarGeos(partner, geoDatas);
					response.setStatus(200);
				} else {
					response.setStatus(403);
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				response.setStatus(400);
				response.sendError(400, e.toString());
				logger.error("cargeo error.", e);
			}
		} catch (Exception e) {
			logger.error("cargeo:", e);
			response.sendError(500, e.toString());
		}
	}
}
