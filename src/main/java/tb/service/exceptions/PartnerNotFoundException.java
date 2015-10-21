package tb.service.exceptions;

public class PartnerNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8250483277974266894L;

	private String apiId;

	public PartnerNotFoundException(String apiId) {
		this.setApiId(apiId);
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}
}
