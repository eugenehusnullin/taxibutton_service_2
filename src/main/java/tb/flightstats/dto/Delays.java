
package tb.flightstats.dto;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Delays {

    @SerializedName("departureGateDelayMinutes")
    @Expose
    private Integer departureGateDelayMinutes;
    @SerializedName("arrivalGateDelayMinutes")
    @Expose
    private Integer arrivalGateDelayMinutes;

    /**
     * 
     * @return
     *     The departureGateDelayMinutes
     */
    public Integer getDepartureGateDelayMinutes() {
        return departureGateDelayMinutes;
    }

    /**
     * 
     * @param departureGateDelayMinutes
     *     The departureGateDelayMinutes
     */
    public void setDepartureGateDelayMinutes(Integer departureGateDelayMinutes) {
        this.departureGateDelayMinutes = departureGateDelayMinutes;
    }

    /**
     * 
     * @return
     *     The arrivalGateDelayMinutes
     */
    public Integer getArrivalGateDelayMinutes() {
        return arrivalGateDelayMinutes;
    }

    /**
     * 
     * @param arrivalGateDelayMinutes
     *     The arrivalGateDelayMinutes
     */
    public void setArrivalGateDelayMinutes(Integer arrivalGateDelayMinutes) {
        this.arrivalGateDelayMinutes = arrivalGateDelayMinutes;
    }

}
