package tb.domain;

import java.io.Serializable;

public class Tariff implements Serializable {
	private static final long serialVersionUID = 1038637471754719518L;
	private Partner partner;
	private String tariffId;
	private String name;
	private String tariff;

	public String getTariff() {
		return tariff;
	}

	public void setTariff(String tariff) {
		this.tariff = tariff;
	}

	public String getTariffId() {
		return tariffId;
	}

	public void setTariffId(String tariffId) {
		this.tariffId = tariffId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}
}
