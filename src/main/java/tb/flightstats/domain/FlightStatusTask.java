package tb.flightstats.domain;

import java.util.Date;

public class FlightStatusTask {
	private Long id;
	private Long orderId;
	private Boolean finished;

	private String lastStatus;
	private Date arrivalScheduledUtc;

	private String carrier;
	private String flight;
	private Integer depYear;
	private Integer depMonth;
	private Integer depDay;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getFinished() {
		return finished;
	}

	public void setFinished(Boolean finished) {
		this.finished = finished;
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public String getFlight() {
		return flight;
	}

	public void setFlight(String flight) {
		this.flight = flight;
	}

	public Integer getDepYear() {
		return depYear;
	}

	public void setDepYear(Integer depYear) {
		this.depYear = depYear;
	}

	public Integer getDepMonth() {
		return depMonth;
	}

	public void setDepMonth(Integer depMonth) {
		this.depMonth = depMonth;
	}

	public Integer getDepDay() {
		return depDay;
	}

	public void setDepDay(Integer depDay) {
		this.depDay = depDay;
	}

	public Date getArrivalScheduledUtc() {
		return arrivalScheduledUtc;
	}

	public void setArrivalScheduledUtc(Date arrivalScheduledUtc) {
		this.arrivalScheduledUtc = arrivalScheduledUtc;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getLastStatus() {
		return lastStatus;
	}

	public void setLastStatus(String lastStatus) {
		this.lastStatus = lastStatus;
	}
}
