package tb.admin.model;

import tb.domain.Brand;

public class BrandModel {
	private Long id;
	private String name;
	private String partners;

	public BrandModel(Brand brand) {
		id = brand.getId();
		name = brand.getCodeName();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPartners() {
		return partners;
	}

	public void setPartners(String partners) {
		this.partners = partners;
	}
}
