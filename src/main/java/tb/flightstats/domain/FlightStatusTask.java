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

	private String departureDateLocal;
	private String departureDateUtc;
	private String arrivalDateLocal;
	private String arrivalDateUtc;
	private String departureAirport;
	private String arrivalAirport;
	private String departureCity;
	private String arrivalCity;

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

	public String getDepartureDateLocal() {
		return departureDateLocal;
	}

	public void setDepartureDateLocal(String departureDateLocal) {
		this.departureDateLocal = departureDateLocal;
	}

	public String getDepartureDateUtc() {
		return departureDateUtc;
	}

	public void setDepartureDateUtc(String departureDateUtc) {
		this.departureDateUtc = departureDateUtc;
	}

	public String getArrivalDateLocal() {
		return arrivalDateLocal;
	}

	public void setArrivalDateLocal(String arrivalDateLocal) {
		this.arrivalDateLocal = arrivalDateLocal;
	}

	public String getArrivalDateUtc() {
		return arrivalDateUtc;
	}

	public void setArrivalDateUtc(String arrivalDateUtc) {
		this.arrivalDateUtc = arrivalDateUtc;
	}

	public String getDepartureAirport() {
		return departureAirport;
	}

	public void setDepartureAirport(String departureAirport) {
		this.departureAirport = departureAirport;
	}

	public String getArrivalAirport() {
		return arrivalAirport;
	}

	public void setArrivalAirport(String arivalAirport) {
		this.arrivalAirport = arivalAirport;
	}

	public String getDepartureCity() {
		return departureCity;
	}

	public void setDepartureCity(String departureCity) {
		this.departureCity = departureCity;
	}

	public String getArrivalCity() {
		return arrivalCity;
	}

	public void setArrivalCity(String arrivalCity) {
		this.arrivalCity = arrivalCity;
	}
}
