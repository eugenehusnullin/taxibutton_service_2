package tb.flightstats;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tb.dao.IFlightStatusDao;
import tb.dao.IOrderDao;
import tb.domain.order.Order;
import tb.flightstats.domain.FlightStatusTask;
import tb.flightstats.dto.FlightStatus;
import tb.flightstats.dto.FlightStatusDto;
import tb.flightstats.retrofit2.FlightstatusV2Api;

@Service
@EnableScheduling
public class FlightStatsService {

	private static final Logger logger = LoggerFactory.getLogger(FlightStatsService.class);

	@Value("#{mainSettings['flightstats.id']}")
	protected String FLIGHTSTATS_APPLICATION_ID;
	@Value("#{mainSettings['flightstats.key']}")
	protected String FLIGHTSTATS_APPLICATION_KEY;
	@Value("#{mainSettings['flightstats.baseurl']}")
	protected String FLIGHTSTATS_BASEURL;

	private FlightstatusV2Api flightstatusV2Api;
	@Autowired
	private IFlightStatusDao flightStatusDao;
	@Autowired
	private IOrderDao orderDao;

	@Transactional
	@Scheduled(cron = "0 0/5 * * * *")
	public void main() {
		logger.debug("Starting FlightstatusV2Api scheduled method.");

		ZonedDateTime zdtNow = ZonedDateTime.now();
		List<FlightStatusTask> list = flightStatusDao.getUnfinishedFlightStatusTasks();
		logger.debug("FlightstatusV2Api найдено %s UnfinishedFlightStatusTasks", list.size());

		for (FlightStatusTask fst : list) {
			logger.debug("UnfinishedFlightStatusTask id=%s arrivalScheduledUtc=%s", fst.getId(),
					fst.getArrivalScheduledUtc());
			ZonedDateTime zdtArrivalScheduledUtc = ZonedDateTime
					.ofInstant(fst.getArrivalScheduledUtc().toInstant(), ZoneId.of("UTC"))
					.minusMinutes(15);

			if (zdtNow.isAfter(zdtArrivalScheduledUtc)) {
				logger.debug("UnfinishedFlightStatusTask id=%s arrivalScheduledUtc=%s it is time to process",
						fst.getId(),
						fst.getArrivalScheduledUtc());
				try {
					FlightStatusDto dto = getFlightStatusDto(fst.getCarrier(), fst.getFlight(), fst.getDepYear(),
							fst.getDepMonth(), fst.getDepDay());
					processFlightStatus(fst, dto);
				} catch (Exception e) {
					logger.error("error makeFlightStatusRequest to FlightStatusTask id=" + fst.getId(), e);
				}
			} else {
				logger.debug("UnfinishedFlightStatusTask id=%s arrivalScheduledUtc=%s it is NOT time to process",
						fst.getId(),
						fst.getArrivalScheduledUtc());
			}
		}
	}

	private void processFlightStatus(FlightStatusTask fst, FlightStatusDto dto) {
		FlightStatus firstFlightStatus = dto.getFlightStatuses().get(0);
		String status = firstFlightStatus.getStatus();

		logger.debug("UnfinishedFlightStatusTask id=%s arrivalScheduledUtc=%s status=%s", fst.getId(),
				fst.getArrivalScheduledUtc(), status);

		Order order = orderDao.get(fst.getOrderId());
		switch (status) {
		case "L":
			fst.setLastStatus(status);
			fst.setFinished(true);
			flightStatusDao.updateFlightStatusTask(fst);

			// врме€ подачи
			ZonedDateTime zdt = ZonedDateTime.now().plusMinutes(30);
			Date bookingDate = new Date(
					Date.from(zdt.toInstant()).getTime() - (zdt.getOffset().getTotalSeconds() * 1000));
			order.setBookingDate(bookingDate);

			// терминал прибыти€
			String arrivalTerminal = firstFlightStatus.getAirportResources().getArrivalTerminal();
			if (arrivalTerminal != null && !arrivalTerminal.isEmpty()) {
				String sourceDescription = order.getSource().getDescription();
				if (sourceDescription == null) {
					sourceDescription = "";
				}
				order.getSource().setDescription(sourceDescription + " “ерминал " + arrivalTerminal);
			}

			orderDao.save(order);

			break;

		case "A":
		case "S":
			fst.setLastStatus(status);
			flightStatusDao.updateFlightStatusTask(fst);
			break;

		default:
			fst.setLastStatus(status);
			fst.setFinished(true);
			flightStatusDao.updateFlightStatusTask(fst);

			order.setProcessingFinished(true);
			orderDao.save(order);
			break;
		}
	}

	@PostConstruct
	public void onPostConstruct() {
		init();
	}

	public void init() {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(FLIGHTSTATS_BASEURL)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		flightstatusV2Api = retrofit.create(FlightstatusV2Api.class);
	}

	public FlightStatusTask initFlightStatsService(JSONObject createOrderObject) throws Exception {
		JSONObject airportJson = createOrderObject.getJSONObject("airport");
		FlightStatusTask fst = new FlightStatusTask();
		fst.setCarrier(airportJson.getString("carrier"));
		fst.setFlight(airportJson.getString("flight"));
		fst.setDepYear(airportJson.getInt("depyear"));
		fst.setDepMonth(airportJson.getInt("depmonth"));
		fst.setDepDay(airportJson.getInt("depday"));
		fst.setFinished(false);

		FlightStatusDto dto = getFlightStatusDto(fst.getCarrier(), fst.getFlight(), fst.getDepYear(),
				fst.getDepMonth(), fst.getDepDay());
		FlightStatus flightStatus = dto.getFlightStatuses().get(0);
		if (checkValidCreateOrderStatus(flightStatus)) {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			Date arrivalScheduledUtc = dateFormatter.parse(flightStatus.getArrivalDate().getDateUtc());
			fst.setArrivalScheduledUtc(arrivalScheduledUtc);

			fst.setLastStatus(flightStatus.getStatus());
			return fst;
		} else {
			throw new Exception("Not valid status: " + flightStatus.getStatus());
		}
	}

	private boolean checkValidCreateOrderStatus(FlightStatus flightStatus) {
		return flightStatus.getStatus().equals("S") || flightStatus.getStatus().equals("L");
	}

	public FlightStatusDto getFlightStatusDto(String carrier, String flight, int depYear, int depMonth,
			int depDay) throws Exception {
		Call<FlightStatusDto> call = flightstatusV2Api.getFlightStatusByDepGson(carrier, flight, depYear, depMonth,
				depDay,
				FLIGHTSTATS_APPLICATION_ID, FLIGHTSTATS_APPLICATION_KEY, true);
		Response<FlightStatusDto> response = call.execute();

		if (response.isSuccessful()) {
			return response.body();
		} else {
			logger.error(response.toString());
			throw new Exception(response.toString());
		}
	}

	public void createFlightStatusTask(FlightStatusTask fst) {
		flightStatusDao.saveFlightStatusTask(fst);
	}
}
