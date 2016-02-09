package tb.service.exceptions;

public class CarNotFoundException extends Exception {

	private static final long serialVersionUID = 8928839101640492631L;
	private Long partnerId;
	private String uuid;

	public CarNotFoundException(Long partnerId, String uuid) {
		this.setPartnerId(partnerId);
		this.setUuid(uuid);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Long getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(Long partnerId) {
		this.partnerId = partnerId;
	}
}
