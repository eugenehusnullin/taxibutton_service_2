package tb.car;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tb.car.domain.GeoData;
import tb.utils.XmlUtils;

@Service
public class CarGeoBuilder {

	public List<GeoData> createCarGeos(Document doc, Date loadDate) {

		doc.getDocumentElement().normalize();
		NodeList trackNodeList = doc.getElementsByTagName("track");
		List<GeoData> list = new ArrayList<>(trackNodeList.getLength());
		for (int i = 0; i < trackNodeList.getLength(); i++) {
			GeoData geoData = new GeoData();
			geoData.setDate(loadDate);

			Node trackNode = trackNodeList.item(i);
			Element trackElement = (Element) trackNode;

			geoData.setUuid(trackElement.getAttribute("uuid"));
			Element pointElement = XmlUtils.getOneElement(trackElement, "point");

			geoData.setLat(Double.parseDouble(pointElement.getAttribute("latitude")));
			geoData.setLon(Double.parseDouble(pointElement.getAttribute("longitude")));

			list.add(geoData);
		}
		return list;
	}

	public String defineCarStateGeosPartnerClid(Document doc) {
		doc.getDocumentElement().normalize();
		Node tracksNode = doc.getElementsByTagName("tracks").item(0);
		Element tracksElement = (Element) tracksNode;
		return tracksElement.getAttribute("clid");
	}
}