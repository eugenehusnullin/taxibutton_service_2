package tb.partnerside;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/1.x/cancelrequest")
public class CancelRequestController {
	private static final Logger logger = LoggerFactory.getLogger("partnerside");

	@RequestMapping(value = "", method = RequestMethod.POST)
	public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("/1.x/cancelrequest: orderid=" + request.getParameter("orderid") + " reason="
				+ request.getParameter("reason"));
		response.setStatus(200);
	}

}
