package tb.car.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import tb.car.domain.LastGeoData;
import tb.domain.Partner;
import tb.domain.order.Requirement;

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
				session.save(carState);
			}
		}
	}

	@Transactional
	public void updateCarGeos(Partner partner, List<GeoData> geoDatas) {
		Session session = sessionFactory.getCurrentSession();
		for (GeoData geoData : geoDatas) {
			session.save(geoData);
		}
	}

	@Transactional
	public boolean updateCarStateStatus(Partner partner, String uuid, CarStateEnum carStateEnum) {
		Session session = sessionFactory.getCurrentSession();
		String update = "update CarState c set c.state = :state "
				+ " where c.partnerId = :partnerId and c.uuid = :uuid";
		session.createQuery(update)
				.setParameter("state", carStateEnum)
				.setLong("partnerId", partner.getId())
				.setString("uuid", uuid)
				.executeUpdate();

		return true;
	}

	@Transactional
	public void updateCarStateStatuses(Partner partner, List<CarState> carStates) {
		for (CarState carState : carStates) {
			updateCarStateStatus(partner, carState.getUuid(), carState.getState());
		}
	}

	@Transactional
	public List<Object[]> getNearCarStates(List<Partner> partners, double lat, double lon, double diff) {
		Session session = sessionFactory.getCurrentSession();
		Date date = new Date((new Date()).getTime() - (actualGeoMinutes * 60 * 1000));
		String q = " from CarState cs, LastGeoData geo "
				+ " where cs.state=0 "
				+ " and cs.partnerId in (:partners) "
				+ " and cs.partnerId = geo.partnerId "
				+ " and cs.uuid = geo.uuid "
				+ " and geo.date>=:date "
				+ " and (abs(:lat-geo.lat) + abs(:lon-geo.lon)) <= :diff "
				+ " order by abs(:lat-geo.lat) + abs(:lon-geo.lon) ";

		@SuppressWarnings("unchecked")
		List<Object[]> list = session.createQuery(q)
				.setParameterList("partners", partners.stream().map(p -> p.getId()).collect(Collectors.toList()))
				.setTimestamp("date", date)
				.setDouble("lat", lat)
				.setDouble("lon", lon)
				.setDouble("diff", diff)
				.list();

		return list;
	}

	@Transactional
	public List<Object[]> getCarsWithCarStates(Long partnerId) {
		Session session = sessionFactory.getCurrentSession();
		String q = " from CarState a, Car b "
				+ " where a.partnerId = :partnerId "
				+ " and b.partnerId = a.partnerId "
				+ " and a.uuid = b.uuid "
				+ " order by b.uuid desc ";

		@SuppressWarnings("unchecked")
		List<Object[]> list = session.createQuery(q)
				.setLong("partnerId", partnerId)
				.list();

		return list;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public List<LastGeoData> getLastGeoDatas(Long partnerId) {
		Session session = sessionFactory.getCurrentSession();
		return session.createCriteria(LastGeoData.class).list();
	}

	@Transactional
	public List<LastGeoData> getCarStatesByRequirements(List<LastGeoData> lastGeoDatas, Set<Requirement> reqs,
			String carClass) {
		if (reqs == null || reqs.size() == 0) {
			return lastGeoDatas;
		}

		List<String> reqsKeys = reqs.stream().map(p -> p.getType()).collect(Collectors.toList());
		Session session = sessionFactory.getCurrentSession();
		List<LastGeoData> filteredCarStates = new ArrayList<LastGeoData>();
		for (LastGeoData lastGeoData : lastGeoDatas) {
			Car car = (Car) session.createCriteria(Car.class)
					.add(Restrictions.eq("partnerId", lastGeoData.getPartnerId()))
					.add(Restrictions.eq("uuid", lastGeoData.getUuid()))
					.uniqueResult();

			if (carClass != null) {
				if (!car.getCarClasses().contains(carClass)) {
					continue;
				}
			}

			boolean b = reqsKeys.stream().allMatch(p -> car.getCarRequires().containsKey(p)
					&& !car.getCarRequires().get(p).equals("no"));
			if (b) {
				filteredCarStates.add(lastGeoData);
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
