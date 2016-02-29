package tb.domain.order;

public class Requirement {

	private Long id;
	private Order order;
	private String type;
	private String options;
	private Boolean needCarCheck;
	
	public Order getOrder() {
		return order;
	}
	
	public void setOrder(Order order) {
		this.order = order;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getOptions() {
		return options;
	}
	
	public void setOptions(String options) {
		this.options = options;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getNeedCarCheck() {
		return needCarCheck;
	}

	public void setNeedCarCheck(Boolean needCarCheck) {
		this.needCarCheck = needCarCheck;
	}
}
