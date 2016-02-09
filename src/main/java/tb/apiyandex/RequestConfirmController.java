package tb.apiyandex;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import tb.service.OrderService;
import tb.service.exceptions.PartnerNotFoundException;
import tb.service.exceptions.CarNotFoundException;
import tb.service.exceptions.OrderNotFoundException;

@Controller("apiyandexRequestConfirmController")
@RequestMapping("requestconfirm")
// Обновление статуса заказа
public class RequestConfirmController {
	private static final Logger logger = LoggerFactory.getLogger(RequestConfirmController.class);

	@Autowired
	private OrderService orderService;

	@RequestMapping(value = "")
	// , method = RequestMethod.POST)
	public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String clid = request.getParameter("clid");
		String apikey = request.getParameter("apikey");
		String orderId = request.getParameter("orderid");
		String status = request.getParameter("status");
		String extra = request.getParameter("extra");
		String newcar = request.getParameter("newcar");

		try {
			if (newcar != null) {
				logger.info("Change the car: OrderId-" + orderId + ", Newcar-" + newcar + ".");
				orderService.setNewcar(clid, apikey, orderId, newcar);
			} else {
				logger.info("Set status: OrderId-" + orderId + ", Status-" + status + ".");
				orderService.setStatus(clid, apikey, orderId, status, extra);
			}
		} catch (PartnerNotFoundException e) {
			response.sendError(403, "PartnerNotFoundException");
		} catch (OrderNotFoundException e) {
			response.sendError(404, "OrderNotFoundException");
		} catch (CarNotFoundException e) {
			response.sendError(404, "CarNotFoundException");
		}
	}
}
