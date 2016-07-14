
package tb.flightstats.dto;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class FlightDurations {

    @SerializedName("scheduledBlockMinutes")
    @Expose
    private Integer scheduledBlockMinutes;
    @SerializedName("blockMinutes")
    @Expose
    private Integer blockMinutes;
    @SerializedName("airMinutes")
    @Expose
    private Integer airMinutes;
    @SerializedName("taxiOutMinutes")
    @Expose
    private Integer taxiOutMinutes;
    @SerializedName("taxiInMinutes")
    @Expose
    private Integer taxiInMinutes;

    /**
     * 
     * @return
     *     The scheduledBlockMinutes
     */
    public Integer getScheduledBlockMinutes() {
        return scheduledBlockMinutes;
    }

    /**
     * 
     * @param scheduledBlockMinutes
     *     The scheduledBlockMinutes
     */
    public void setScheduledBlockMinutes(Integer scheduledBlockMinutes) {
        this.scheduledBlockMinutes = scheduledBlockMinutes;
    }

    /**
     * 
     * @return
     *     The blockMinutes
     */
    public Integer getBlockMinutes() {
        return blockMinutes;
    }

    /**
     * 
     * @param blockMinutes
     *     The blockMinutes
     */
    public void setBlockMinutes(Integer blockMinutes) {
        this.blockMinutes = blockMinutes;
    }

    /**
     * 
     * @return
     *     The airMinutes
     */
    public Integer getAirMinutes() {
        return airMinutes;
    }

    /**
     * 
     * @param airMinutes
     *     The airMinutes
     */
    public void setAirMinutes(Integer airMinutes) {
        this.airMinutes = airMinutes;
    }

    /**
     * 
     * @return
     *     The taxiOutMinutes
     */
    public Integer getTaxiOutMinutes() {
        return taxiOutMinutes;
    }

    /**
     * 
     * @param taxiOutMinutes
     *     The taxiOutMinutes
     */
    public void setTaxiOutMinutes(Integer taxiOutMinutes) {
        this.taxiOutMinutes = taxiOutMinutes;
    }

    /**
     * 
     * @return
     *     The taxiInMinutes
     */
    public Integer getTaxiInMinutes() {
        return taxiInMinutes;
    }

    /**
     * 
     * @param taxiInMinutes
     *     The taxiInMinutes
     */
    public void setTaxiInMinutes(Integer taxiInMinutes) {
        this.taxiInMinutes = taxiInMinutes;
    }

}
