package tb.car;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import tb.car.dao.CarDao;
import tb.car.domain.Car;
import tb.domain.Partner;
import tb.utils.HttpUtils;
import tb.utils.XmlUtils;

@Service
public class CarSynchProcessing {
	private static final Logger logger = LoggerFactory.getLogger(CarSynch.class);
	
	@Autowired
	private CarBuilder carBuilder;
	@Autowired
	private CarDao carDao;

	@Transactional
	public void makeCarSynch(Partner partner) {
		logger.info("partner - " + partner.getName() + "(" + partner.getApiId() + ")");
		try {
			InputStream carsInputStream = HttpUtils.makeGetRequest(partner.getDriverUrl(), "application/xml");
			Document doc = XmlUtils.buildDomDocument(carsInputStream);
			Date loadDate = new Date();
			List<Car> cars = carBuilder.createCars(doc, partner, loadDate);
			logger.info(cars.size() + " cars - pulled from partner.");
			updateCars(cars, partner, loadDate);
			logger.info("Cars saved to db.");
		} catch (Exception e) {
			logger.error("partner - " + partner.getName() + "(" + partner.getApiId() + ")" + " Car synch error: ", e);
		}
	}
	
	private void updateCars(List<Car> cars, Partner partner, Date loadDate) {
		carDao.updateCars(cars, partner, loadDate);
	}
}
