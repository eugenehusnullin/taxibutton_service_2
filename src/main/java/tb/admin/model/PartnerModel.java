package tb.admin.model;

import tb.domain.Partner;

public class PartnerModel {
	private Long id;
	private String apiId;
	private String apiKey;
	private String name;
	private String apiurl;
	private String comment;
	private String brands;

	public PartnerModel(Partner s) {
		id = s.getId();
		apiId = s.getApiId();
		apiKey = s.getApiKey();
		name = s.getName();
		apiurl = s.getApiurl();
		comment = s.getComment();
	}

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

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApiurl() {
		return apiurl;
	}

	public void setApiurl(String apiurl) {
		this.apiurl = apiurl;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getBrands() {
		return brands;
	}

	public void setBrands(String brands) {
		this.brands = brands;
	}

}
