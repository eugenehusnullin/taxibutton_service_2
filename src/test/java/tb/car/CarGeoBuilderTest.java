package tb.car;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

public class CarGeoBuilderTest {
	@Test
	public void testSynch() throws ParserConfigurationException, SAXException, IOException {
		CarGeoBuilder gb = new CarGeoBuilder();
		Date d1 = gb.parseDate("09022016:150612" + "Z");
		LocalDateTime ldt = LocalDateTime.of(2016, 2, 9, 15, 06, 12);
		Date d2 = Date.from(ldt.toInstant(ZoneOffset.UTC));

		Assert.assertEquals(d2, d1);
	}

}
