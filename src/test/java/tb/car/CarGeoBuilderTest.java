package tb.car;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class CarGeoBuilderTest {
	@Test
	public void testSynch() throws ParserConfigurationException, SAXException, IOException {
		CarGeoBuilder gb = new CarGeoBuilder();
		System.out.println(gb.parseDate("09022016:150612" + "Z"));
	}

}
