package tb.admin.model;

public class DeviceModel {
	private Long id;
	private String apiId;
	private String phone;
	private String confirmKey;
	private String taxi;
	private String userName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getConfirmKey() {
		return confirmKey;
	}

	public void setConfirmKey(String confirmKey) {
		this.confirmKey = confirmKey;
	}

	public String getTaxi() {
		return taxi;
	}

	public void setTaxi(String taxi) {
		this.taxi = taxi;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
