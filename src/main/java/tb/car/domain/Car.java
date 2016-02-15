package tb.car.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Car implements Serializable {
	private static final long serialVersionUID = -1972220629253322094L;
	
	private Long partnerId;
	private String uuid;

	private String realClid;
	private String realName;
	private String realWeb;
	private String realScid;
	private List<String> carClasses;

	private String driverDisplayName;
	private String driverPhone;
	private Integer driverAge;
	private String driverLicense;
	private String driverPermit;

	private String carModel;
	private Integer carAge;
	private String carColor;
	private String carNumber;
	private String carPermit;
	private Map<String, String> carRequires;

	public Car() {
		setCarClasses(new ArrayList<String>());
		carRequires = new HashMap<String, String>();
	}

	public String getCarModel() {
		return carModel;
	}

	public void setCarModel(String carModel) {
		this.carModel = carModel;
	}

	public String getCarColor() {
		return carColor;
	}

	public void setCarColor(String carColor) {
		this.carColor = carColor;
	}

	public String getCarNumber() {
		return carNumber;
	}

	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}

	public String getCarPermit() {
		return carPermit;
	}

	public void setCarPermit(String carPermit) {
		this.carPermit = carPermit;
	}

	public String getDriverPhone() {
		return driverPhone;
	}

	public void setDriverPhone(String driverPhone) {
		this.driverPhone = driverPhone;
	}

	public Integer getDriverAge() {
		return driverAge;
	}

	public void setDriverAge(Integer driverAge) {
		this.driverAge = driverAge;
	}

	public String getDriverLicense() {
		return driverLicense;
	}

	public void setDriverLicense(String driverLicense) {
		this.driverLicense = driverLicense;
	}

	public String getDriverPermit() {
		return driverPermit;
	}

	public void setDriverPermit(String driverPermit) {
		this.driverPermit = driverPermit;
	}

	public String getRealClid() {
		return realClid;
	}

	public void setRealClid(String realClid) {
		this.realClid = realClid;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getRealWeb() {
		return realWeb;
	}

	public void setRealWeb(String realWeb) {
		this.realWeb = realWeb;
	}

	public String getRealScid() {
		return realScid;
	}

	public void setRealScid(String realScid) {
		this.realScid = realScid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getDriverDisplayName() {
		return driverDisplayName;
	}

	public void setDriverDisplayName(String driverDisplayName) {
		this.driverDisplayName = driverDisplayName;
	}

	public Integer getCarAge() {
		return carAge;
	}

	public void setCarAge(Integer carAge) {
		this.carAge = carAge;
	}

	public Map<String, String> getCarRequires() {
		return carRequires;
	}

	public void setCarRequires(Map<String, String> carRequires) {
		this.carRequires = carRequires;
	}

	public Long getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(Long partnerId) {
		this.partnerId = partnerId;
	}

	public List<String> getCarClasses() {
		return carClasses;
	}

	public void setCarClasses(List<String> carClasses) {
		this.carClasses = carClasses;
	}
}