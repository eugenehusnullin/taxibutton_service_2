package tb.domain;

import java.util.Set;

public class Brand {
	private Long id;
	private String codeName;
	private Set<BrandService> services;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodeName() {
		return codeName;
	}

	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}

	public Set<BrandService> getServices() {
		return services;
	}

	public void setServices(Set<BrandService> services) {
		this.services = services;
	}
}
