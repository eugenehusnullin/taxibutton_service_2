package tb.domain.order;

import java.io.Serializable;
import java.util.Date;

import tb.domain.Partner;

public class AssignRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	private Partner partner;
	private Order order;
	private String uuid;
	private Date date;
	private Boolean fail;
	private Integer failHttpCode;
	private Double farIndex;

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Boolean getFail() {
		return fail;
	}

	public void setFail(Boolean fail) {
		this.fail = fail;
	}

	public Integer getFailHttpCode() {
		return failHttpCode;
	}

	public void setFailHttpCode(Integer failHttpCode) {
		this.failHttpCode = failHttpCode;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public Double getFarIndex() {
		return farIndex;
	}

	public void setFarIndex(Double farIndex) {
		this.farIndex = farIndex;
	}
}
