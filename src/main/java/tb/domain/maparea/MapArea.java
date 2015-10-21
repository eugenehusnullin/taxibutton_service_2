package tb.domain.maparea;

import java.util.Set;

import tb.domain.Partner;

public class MapArea {

	private Long id;
	private String name;
	private String about;
	private Set<Partner> partners;
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MapArea) {
			MapArea mapArea = (MapArea) obj;
			if (id != null && mapArea.id != null) {
				return id.equals(mapArea.id);
			}
		}
		return super.equals(obj);
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

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public Set<Partner> getPartners() {
		return partners;
	}

	public void setPartners(Set<Partner> partners) {
		this.partners = partners;
	}
}
