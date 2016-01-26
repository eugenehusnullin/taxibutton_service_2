package tb.car.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tb.car.domain.Car;
import tb.car.domain.CarState;
import tb.car.domain.CarStateEnum;
import tb.car.domain.GeoData;
import tb.domain.Partner;
import tb.domain.order.Requirement;
import tb.domain.order.VehicleClass;

@Service
public class CarDao {

	@Autowired
	private SessionFactory sessionFactory;

	// offerorder.cars.actual.geo.minutes
	@Value("#{mainSettings['offerorder.cars.actual.geo.minutes']}")
	private Integer actualGeoMinutes;

	@Transactional
	public void updateCars(List<Car> cars, Partner partner, Date loadDate) {
		Session session = sessionFactory.getCurrentSession();

		for (Car car : cars) {
			session.saveOrUpdate(car);

			CarState carState = (CarState) session.createCriteria(CarState.class)
					.add(Restrictions.eq("partnerId", partner.getId()))
					.add(Restrictions.eq("uuid", car.getUuid()))
					.uniqueResult();

			if (carState == null) {
				carState = new CarState();
				carState.setPartnerId(partner.getId());
				carState.setUuid(car.getUuid());
				carState.setState(CarStateEnum.Undefined);
				carState.setLatitude(0);
				carState.setLongitude(0);
				carState.setDate(loadDate);

				session.save(carState);
			}
		}
	}

	@Transactional
	public void updateCarStateGeos(Partner partner, List<CarState> carStates) {
		Session session = sessionFactory.getCurrentSession();
		String update = "update CarState c set c.latitude = :latitude, c.longitude = :longitude"
				+ ", c.date = :date "
				+ "where c.partnerId = :partnerId and c.uuid = :uuid";
		Query query = session.createQuery(update);
		for (CarState carState : carStates) {

			query
					.setDouble("latitude", carState.getLatitude())
					.setDouble("longitude", carState.getLongitude())
					.setTimestamp("date", carState.getDate())
					.setLong("partnerId", partner.getId())
					.setString("uuid", carState.getUuid())
					.executeUpdate();

			GeoData geoData = createGeoData(partner, carState);
			session.save(geoData);
		}
	}

	private GeoData createGeoData(Partner partner, CarState carState) {
		GeoData geoData = new GeoData();
		geoData.setPartnerId(partner.getId());
		geoData.setUuid(carState.getUuid());
		geoData.setDate(carState.getDate());
		geoData.setLat(carState.getLatitude());
		geoData.setLon(carState.getLongitude());

		return geoData;
	}

	@Transactional
	public boolean updateCarStateStatus(Partner partner, String uuid, CarStateEnum carStateEnum) {
		Session session = sessionFactory.getCurrentSession();

		CarState carState = (CarState) session.createCriteria(CarState.class)
				.add(Restrictions.eq("partnerId", partner.getId()))
				.add(Restrictions.eq("uuid", uuid))
				.uniqueResult();

		if (carState == null) {
			return false;
		} else {
			String update = "update CarState c set c.state = :state "
					+ " where c.partnerId = :partnerId and c.uuid = :uuid";
			session.createQuery(update)
					.setParameter("state", carStateEnum)
					.setLong("partnerId", carState.getPartnerId())
					.setString("uuid", carState.getUuid())
					.executeUpdate();

			return true;
		}
	}

	@Transactional
	public void updateCarStateStatuses(Partner partner, List<CarState> carStates) {
		for (CarState carState : carStates) {
			updateCarStateStatus(partner, carState.getUuid(), carState.getState());
		}
	}

	@Transactional
	public List<CarState> getNearCarStates(List<Partner> partners, double lat, double lon, double diff) {
		Session session = sessionFactory.getCurrentSession();
		Date date = new Date((new Date()).getTime() - (actualGeoMinutes * 60 * 1000));
		String q = " from CarState cs "
				+ " where cs.state=0 "
				+ " and cs.partnerId in (:partners) "
				+ " and cs.date>=:date "
				+ " and (abs(:lat-cs.latitude) + abs(:lon-cs.longitude)) <= :diff "
				+ " order by abs(:lat-cs.latitude) + abs(:lon-cs.longitude) ";

		@SuppressWarnings("unchecked")
		List<CarState> list = (List<CarState>) session.createQuery(q)
				.setParameterList("partners", partners.stream().map(p -> p.getId()).collect(Collectors.toList()))
				.setTimestamp("date", date)
				.setDouble("lat", lat)
				.setDouble("lon", lon)
				.setDouble("diff", diff)
				.list();

		return list;
	}

	@Transactional
	public List<?> getCarsWithCarStates(Long partnerId) {
		Session session = sessionFactory.getCurrentSession();
		String q = " from CarState a, Car b"
				+ " where a.partnerId = b.partnerId "
				+ " and a.partnerId = :partnerId "
				+ " and a.uuid = b.uuid "
				+ " order by a.date desc ";

		List<?> list = session.createQuery(q)
				.setLong("partnerId", partnerId)
				.list();

		return list;
	}

	@Transactional
	public List<CarState> getCarStatesByRequirements(List<CarState> carStates, Set<Requirement> reqs,
			VehicleClass vehicleClass) {
		if (reqs == null || reqs.size() == 0) {
			return carStates;
		}

		List<String> reqsKeys = reqs.stream().map(p -> p.getType()).collect(Collectors.toList());
		Session session = sessionFactory.getCurrentSession();
		List<CarState> filteredCarStates = new ArrayList<CarState>();
		for (CarState carState : carStates) {
			Car car = (Car) session.createCriteria(Car.class)
					.add(Restrictions.eq("partnerId", carState.getPartnerId()))
					.add(Restrictions.eq("uuid", carState.getUuid()))
					.uniqueResult();

			if (vehicleClass != null) {
				if (!car.getVehicleClasses().contains(vehicleClass)) {
					continue;
				}
			}

			boolean b = reqsKeys.stream().allMatch(p -> car.getCarRequires().containsKey(p)
					&& !car.getCarRequires().get(p).equals("no"));
			if (b) {
				filteredCarStates.add(carState);
			}
		}
		return filteredCarStates;
	}

	@Transactional
	public Car getCar(Long partnerId, String uuid) {
		Session session = sessionFactory.getCurrentSession();

		return (Car) session.createCriteria(Car.class)
				.add(Restrictions.eq("partnerId", partnerId))
				.add(Restrictions.eq("uuid", uuid))
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public List<GeoData> getGeoData(Long partnerId, String carUuid, Date date) {
		return sessionFactory.getCurrentSession()
				.createCriteria(GeoData.class)
				.add(Restrictions.eq("partnerId", partnerId))
				.add(Restrictions.eq("uuid", carUuid))
				.add(Restrictions.ge("date", date))
				.addOrder(Order.asc("date"))
				.list();
	}
}
