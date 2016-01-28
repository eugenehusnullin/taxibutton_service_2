package tb.car.domain;

import java.io.Serializable;

public class CarState implements Serializable {
	private static final long serialVersionUID = 1586844821594975295L;

	private Long partnerId;
	private String uuid;
	private CarStateEnum state;

	public CarStateEnum getState() {
		return state;
	}

	public void setState(CarStateEnum state) {
		this.state = state;
	}

	public Long getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(Long partnerId) {
		this.partnerId = partnerId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
