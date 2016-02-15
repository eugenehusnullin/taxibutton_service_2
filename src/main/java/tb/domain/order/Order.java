package tb.domain.order;

import java.util.Date;
import java.util.Set;
import java.util.SortedSet;

import tb.domain.Partner;
import tb.domain.Device;

public class Order {

	private Long id;
	private String uuid;
	private Device device;
	private String phone;
	private Date bookingDate;
	private Partner partner;
	private String carUuid;
	private SortedSet<AddressPoint> destinations;
	private Set<Requirement> requirements;
	private Set<OrderStatus> statuses;
	private Set<OrderCancel> orderCancel;
	// only this partners whom to offer order, if is null or size()==0 then to all partners
	private Set<Partner> offerPartners;
	private String carClass;
	private Boolean notlater;
	private String comments;
	private Date createdDate;
	private Boolean processingFinished;
	private Date startProcessing;

	public Order() {
		processingFinished = false;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public SortedSet<AddressPoint> getDestinations() {
		return destinations;
	}

	public void setDestinations(SortedSet<AddressPoint> destinations) {
		this.destinations = destinations;
	}

	public AddressPoint getSource() {
		for (AddressPoint currentPoint : destinations) {
			if (currentPoint.getIndexNumber() == 0) {
				return currentPoint;
			}
		}
		return null;
	}

	public Set<Requirement> getRequirements() {
		return requirements;
	}

	public void setRequirements(Set<Requirement> requirements) {
		this.requirements = requirements;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String guid) {
		this.uuid = guid;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public Set<OrderStatus> getStatuses() {
		return statuses;
	}

	public void setStatuses(Set<OrderStatus> statuses) {
		this.statuses = statuses;
	}

	public Set<OrderCancel> getOrderCancel() {
		return orderCancel;
	}

	public void setOrderCancel(Set<OrderCancel> orderCancel) {
		this.orderCancel = orderCancel;
	}

	public Set<Partner> getOfferPartners() {
		return offerPartners;
	}

	public void setOfferPartners(Set<Partner> offerPartners) {
		this.offerPartners = offerPartners;
	}

	public Date getBookingDate() {
		return bookingDate;
	}

	public void setBookingDate(Date bookingDate) {
		this.bookingDate = bookingDate;
	}

	public Boolean getNotlater() {
		return notlater;
	}

	public void setNotlater(Boolean notlater) {
		this.notlater = notlater;
	}

	public String getCarUuid() {
		return carUuid;
	}

	public void setCarUuid(String carUuid) {
		this.carUuid = carUuid;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Boolean getProcessingFinished() {
		return processingFinished;
	}

	public void setProcessingFinished(Boolean processingFinished) {
		this.processingFinished = processingFinished;
	}

	public Date getStartProcessing() {
		return startProcessing;
	}

	public void setStartProcessing(Date startProcessing) {
		this.startProcessing = startProcessing;
	}

	public String getCarClass() {
		return carClass;
	}

	public void setCarClass(String carClass) {
		this.carClass = carClass;
	}
}
