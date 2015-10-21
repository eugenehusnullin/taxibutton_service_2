package tb.domain.order;

import java.io.Serializable;
import java.util.Date;

import tb.domain.Partner;

public class Offer implements Serializable {
	private static final long serialVersionUID = 1679325554802755364L;
	private Order order;
	private Partner partner;
	private Date timestamp;

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
