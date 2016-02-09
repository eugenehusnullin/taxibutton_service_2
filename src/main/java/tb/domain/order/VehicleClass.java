package tb.domain.order;

public enum VehicleClass {
	Econom, Comfort, Business, Minivan, VIP;
	
	public static int convert2Partner(VehicleClass vehicleClass) {
		switch (vehicleClass) {
		case Econom:
			return 0;
		case Comfort:
			return 1;
		case Business:
			return 2;
		case VIP:
			return 3;

		default:
			return 0;
		}
	}
	
	public static VehicleClass convertFromPartner(int value) {
		switch (value) {
		case 0:
			return Econom;
		case 1:
			return Comfort;
		case 2:
			return Business;
		case 3:
			return VIP;

		default:
			return Econom;
		}
	}
}
