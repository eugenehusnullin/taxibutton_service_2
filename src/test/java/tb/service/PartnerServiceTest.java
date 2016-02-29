package tb.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import tb.domain.order.Requirement;

public class PartnerServiceTest {
	@Test
	public void testHoldCarOptions() throws IOException {
		PartnerService partnerService = new PartnerService();

		URL url = getClass().getResource("/CarOptions.json");
		File file = new File(url.getFile());
		FileInputStream fis = new FileInputStream(file);
		String settings = IOUtils.toString(fis, "UTF-8");

		partnerService.holdCarOptions(0L, settings);
		List<Requirement> reqs = partnerService.getCarOptions(0L);
		Assert.assertEquals(21, reqs.size());

		List<Requirement> reqs2 = reqs.stream().filter(a -> a.getNeedCarCheck()).collect(Collectors.toList());
		Assert.assertEquals(7, reqs2.size());
	}

}
