package tb.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import tb.dao.IFlightStatusDao;
import tb.flightstats.domain.FlightStatusTask;

@Repository("FlightStatusDao")
public class FlightStatusDao implements IFlightStatusDao {
	@Autowired
	private SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<FlightStatusTask> getUnfinishedFlightStatusTasks() {
		return sessionFactory.getCurrentSession()
				.createCriteria(FlightStatusTask.class)
				.add(Restrictions.eq("finished", false))
				.list();
	}

	@Override
	public void updateFlightStatusTask(FlightStatusTask fst) {
		sessionFactory.getCurrentSession().update(fst);
	}

	@Override
	public void saveFlightStatusTask(FlightStatusTask fst) {
		sessionFactory.getCurrentSession().save(fst);
	}
}
