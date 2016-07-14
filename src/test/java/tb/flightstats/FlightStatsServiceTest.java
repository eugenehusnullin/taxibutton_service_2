package tb.flightstats;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class FlightStatsServiceTest {

	@Test
	public void testCheckNow() throws ParseException {
		ZonedDateTime zdtNow = ZonedDateTime.now();
		int sec = zdtNow.getOffset().getTotalSeconds();

		// System.out.println(zdtNow.toInstant());

		// System.out.println(new Date(Date.from(zdtNow.toInstant()).getTime() - (sec * 1000)));
		// System.out.println(zdtNow);

		Date d1 = new Date();
		ZonedDateTime zdt = ZonedDateTime.ofInstant(d1.toInstant(), ZoneId.of("UTC"))
				.minusMinutes(15);
		System.out.println(zdt);

		zdtNow.isAfter(zdt);
		Assert.assertTrue(zdtNow.isAfter(zdt));

		// 2016-07-14T13:40:00.000
		// 2016-07-14T10:40:00.000Z
		ZonedDateTime zdtArr = ZonedDateTime.parse("2016-07-14T10:40:00.000Z");
		// LocalDateTime zdtArr = LocalDateTime.parse("2016-07-14T13:40:00.000");
		// System.out.println(zdtArr);
		// System.out.println(zdtArr.toInstant());

		Date d3 = Date.from(zdtArr.toInstant());
		// System.out.println(d3);

		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date d4 = dateFormatter.parse("2016-07-14T10:40:00.000Z");
		// System.out.println(d4);

		// System.out.format("nnn %s bbbb %s", 111, d4);
	}

	@Test
	public void testFlightStatsDto() throws Exception {
		// https://api.flightstats.com/
		// appId=84db15c3
		// appKey=2485e99d5a6e5cd3d1b98270a49363ab
		FlightStatsService fss = new FlightStatsService();
		fss.FLIGHTSTATS_APPLICATION_ID = "84db15c3";
		fss.FLIGHTSTATS_APPLICATION_KEY = "2485e99d5a6e5cd3d1b98270a49363ab";
		fss.FLIGHTSTATS_BASEURL = "https://api.flightstats.com/";
		fss.init();
		// FlightStatusDto fsdto = fss.getFlightStatusDto("S7", "157", 2016, 07, 13);
	}

}
