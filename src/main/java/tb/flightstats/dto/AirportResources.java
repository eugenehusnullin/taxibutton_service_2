
package tb.flightstats.dto;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class AirportResources {

	@SerializedName("departureGate")
	@Expose
	private String departureGate;

	@SerializedName("arrivalTerminal")
	@Expose
	private String arrivalTerminal;

	/**
	 * 
	 * @return The departureGate
	 */
	public String getDepartureGate() {
		return departureGate;
	}

	/**
	 * 
	 * @param departureGate
	 *            The departureGate
	 */
	public void setDepartureGate(String departureGate) {
		this.departureGate = departureGate;
	}

	public String getArrivalTerminal() {
		return arrivalTerminal;
	}

	public void setArrivalTerminal(String arrivalTerminal) {
		this.arrivalTerminal = arrivalTerminal;
	}

}
