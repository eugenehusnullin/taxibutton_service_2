package tb.dao;

import java.util.List;

import tb.flightstats.domain.FlightStatusTask;

public interface IFlightStatusDao {
	List<FlightStatusTask> getUnfinishedFlightStatusTasks();

	void updateFlightStatusTask(FlightStatusTask fst);

	void saveFlightStatusTask(FlightStatusTask fst);
}
