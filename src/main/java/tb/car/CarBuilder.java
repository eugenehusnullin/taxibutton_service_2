package tb.car;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tb.car.domain.Car;
import tb.domain.Partner;
import tb.utils.XmlUtils;

@Service
public class CarBuilder {

	public List<Car> createCars(Document doc, Partner partner, Date loadDate) {
		List<Car> list = new ArrayList<Car>();
		doc.getDocumentElement().normalize();

		NodeList carNodeList = doc.getElementsByTagName("Car");
		for (int i = 0; i < carNodeList.getLength(); i++) {
			Car car = new Car();
			car.setPartnerId(partner.getId());

			Node carNode = carNodeList.item(i);
			Element carElement = (Element) carNode;

			car.setUuid(XmlUtils.getElementContent(carElement, "Uuid").substring(0, 32));
			car.setRealClid(XmlUtils.getElementContent(carElement, "RealClid").substring(0, 32));
			car.setRealName(XmlUtils.getElementContent(carElement, "RealName").substring(0, 100));
			car.setRealWeb(XmlUtils.getElementContent(carElement, "RealWeb").substring(0, 100));
			car.setRealScid(XmlUtils.getElementContent(carElement, "RealScid").substring(0, 50));

			NodeList tariffNodeList = carElement.getElementsByTagName("Tariff");
			for (int j = 0; j < tariffNodeList.getLength(); j++) {
				Node tariffNode = tariffNodeList.item(j);
				Element tariffElement = (Element) tariffNode;
				car.getTariffs().add(tariffElement.getFirstChild().getNodeValue().substring(0, 32));
			}

			Element driverDetailsElement = XmlUtils.getOneElement(carElement, "DriverDetails");
			if (driverDetailsElement != null) {
				car.setDriverDisplayName(XmlUtils.getElementContent(driverDetailsElement, "DisplayName").substring(0, 100));
				car.setDriverPhone(XmlUtils.getElementContent(driverDetailsElement, "Phone").substring(0, 20));
				car.setDriverAge(Integer.parseInt(XmlUtils.getElementContent(driverDetailsElement, "Age")));
				car.setDriverLicense(XmlUtils.getElementContent(driverDetailsElement, "DriverLicense").substring(0, 60));
				car.setDriverPermit(XmlUtils.getElementContent(driverDetailsElement, "Permit").substring(0, 60));
			}

			Element carDetailsElement = XmlUtils.getOneElement(carElement, "CarDetails");
			car.setCarModel(XmlUtils.getElementContent(carDetailsElement, "Model").substring(0, 100));
			String carAge = XmlUtils.getElementContent(carDetailsElement, "Age");
			car.setCarAge(carAge == null ? null : Integer.parseInt(carAge));
			car.setCarColor(XmlUtils.getElementContent(carDetailsElement, "Color").substring(0, 12));
			car.setCarNumber(XmlUtils.getElementContent(carDetailsElement, "CarNumber").substring(0, 12));
			car.setCarPermit(XmlUtils.getElementContent(carDetailsElement, "Permit").substring(0, 12));

			NodeList carRequireNodeList = carDetailsElement.getElementsByTagName("Require");
			for (int j = 0; j < carRequireNodeList.getLength(); j++) {
				String requireValue = carRequireNodeList.item(j).getFirstChild().getNodeValue().substring(0, 32);
				String requireName = ((Element) carRequireNodeList.item(j)).getAttribute("name").substring(0, 32);
				car.getCarRequires().put(requireName, requireValue);
			}

			list.add(car);
		}
		return list;
	}

}
