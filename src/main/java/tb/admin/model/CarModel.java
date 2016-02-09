package tb.admin.model;

import java.util.Date;

public class CarModel {
	private String disp;
	private String uuid;
	private String name;
	private String state;
	private Date date;
	private Double lat;
	private Double lon;
	private String requirmets;
	private String carclass;
	private boolean geoObsolete = false;

	public String getDisp() {
		return disp;
	}

	public void setDisp(String disp) {
		this.disp = disp;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Double getLon() {
		return lon;
	}

	public void setLon(Double lon) {
		this.lon = lon;
	}

	public String getRequirmets() {
		return requirmets;
	}

	public void setRequirmets(String requirmets) {
		this.requirmets = requirmets;
	}

	public String getCarclass() {
		return carclass;
	}

	public void setCarclass(String carclass) {
		this.carclass = carclass;
	}

	public boolean isGeoObsolete() {
		return geoObsolete;
	}

	public void setGeoObsolete(boolean geoObsolete) {
		this.geoObsolete = geoObsolete;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
