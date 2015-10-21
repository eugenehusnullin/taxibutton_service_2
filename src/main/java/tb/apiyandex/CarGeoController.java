package tb.apiyandex;

import java.io.IOException;
import java.util.Date;
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

import tb.car.CarStateGeoBuilder;
import tb.car.dao.CarDao;
import tb.car.domain.CarState;
import tb.dao.IPartnerDao;
import tb.domain.Partner;
import tb.utils.XmlUtils;

@Controller("apiyandexCarGeoController")
@RequestMapping("/cargeo")
public class CarGeoController {
	private static final Logger log = LoggerFactory.getLogger(CarGeoController.class);

	@Autowired
	private CarStateGeoBuilder carStateGeoBuilder;
	@Autowired
	private IPartnerDao partnerDao;
	@Autowired
	private CarDao carDao;

	@RequestMapping(value = "", method = RequestMethod.POST)
	public void index(HttpServletRequest request, HttpServletResponse response) {
		try {
			try {
				Document document = XmlUtils.buildDomDocument(request.getParameter("data"));
				String partnerClid = carStateGeoBuilder.defineCarStateGeosPartnerClid(document);
				Partner partner = partnerDao.getByApiId(partnerClid);
				if (partner != null) {
					List<CarState> carStates = carStateGeoBuilder.createCarStateGeos(document, new Date());
					carDao.updateCarStateGeos(partner, carStates);
					response.setStatus(200);
				} else {
					response.setStatus(403);
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				response.setStatus(400);
			}
		} catch (Exception e) {
			log.error("cargeo:", e);
			throw e;
		}
	}
}
