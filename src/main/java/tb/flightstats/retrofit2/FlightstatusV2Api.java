package tb.flightstats.retrofit2;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import tb.flightstats.dto.FlightStatusDto;

public interface FlightstatusV2Api {

	// https://api.flightstats.com/flex/flightstatus/rest/v2/json/flight/status/S7/4534/dep/2016/5/5?appId=cc15&appKey=270e&utc=true

	@GET("flex/flightstatus/rest/v2/json/flight/status/{flightid}")
	Call<String> getFlightStatusByFlightId(@Path("flightid") String flight, @Query("appId") String appId,
			@Query("appKey") String appKey, @Query("utc") Boolean utc);

	@GET("flex/flightstatus/rest/v2/json/flight/status/{carrier}/{flight}/dep/{year}/{month}/{day}")
	Call<FlightStatusDto> getFlightStatusByDepGson(@Path("carrier") String carrier, @Path("flight") String flight,
			@Path("year") int year, @Path("month") int month, @Path("day") int day, @Query("appId") String appId,
			@Query("appKey") String appKey, @Query("utc") Boolean utc);
}