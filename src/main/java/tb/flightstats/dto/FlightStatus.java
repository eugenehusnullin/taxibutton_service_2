
package tb.flightstats.dto;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class FlightStatus {

    @SerializedName("flightId")
    @Expose
    private Integer flightId;
    @SerializedName("carrierFsCode")
    @Expose
    private String carrierFsCode;
    @SerializedName("flightNumber")
    @Expose
    private String flightNumber;
    @SerializedName("departureAirportFsCode")
    @Expose
    private String departureAirportFsCode;
    @SerializedName("arrivalAirportFsCode")
    @Expose
    private String arrivalAirportFsCode;
    @SerializedName("departureDate")
    @Expose
    private DepartureDate departureDate;
    @SerializedName("arrivalDate")
    @Expose
    private ArrivalDate arrivalDate;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("schedule")
    @Expose
    private Schedule schedule;
    @SerializedName("operationalTimes")
    @Expose
    private OperationalTimes operationalTimes;
    @SerializedName("delays")
    @Expose
    private Delays delays;
    @SerializedName("flightDurations")
    @Expose
    private FlightDurations flightDurations;
    @SerializedName("airportResources")
    @Expose
    private AirportResources airportResources;
    @SerializedName("flightEquipment")
    @Expose
    private FlightEquipment flightEquipment;

    /**
     * 
     * @return
     *     The flightId
     */
    public Integer getFlightId() {
        return flightId;
    }

    /**
     * 
     * @param flightId
     *     The flightId
     */
    public void setFlightId(Integer flightId) {
        this.flightId = flightId;
    }

    /**
     * 
     * @return
     *     The carrierFsCode
     */
    public String getCarrierFsCode() {
        return carrierFsCode;
    }

    /**
     * 
     * @param carrierFsCode
     *     The carrierFsCode
     */
    public void setCarrierFsCode(String carrierFsCode) {
        this.carrierFsCode = carrierFsCode;
    }

    /**
     * 
     * @return
     *     The flightNumber
     */
    public String getFlightNumber() {
        return flightNumber;
    }

    /**
     * 
     * @param flightNumber
     *     The flightNumber
     */
    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    /**
     * 
     * @return
     *     The departureAirportFsCode
     */
    public String getDepartureAirportFsCode() {
        return departureAirportFsCode;
    }

    /**
     * 
     * @param departureAirportFsCode
     *     The departureAirportFsCode
     */
    public void setDepartureAirportFsCode(String departureAirportFsCode) {
        this.departureAirportFsCode = departureAirportFsCode;
    }

    /**
     * 
     * @return
     *     The arrivalAirportFsCode
     */
    public String getArrivalAirportFsCode() {
        return arrivalAirportFsCode;
    }

    /**
     * 
     * @param arrivalAirportFsCode
     *     The arrivalAirportFsCode
     */
    public void setArrivalAirportFsCode(String arrivalAirportFsCode) {
        this.arrivalAirportFsCode = arrivalAirportFsCode;
    }

    /**
     * 
     * @return
     *     The departureDate
     */
    public DepartureDate getDepartureDate() {
        return departureDate;
    }

    /**
     * 
     * @param departureDate
     *     The departureDate
     */
    public void setDepartureDate(DepartureDate departureDate) {
        this.departureDate = departureDate;
    }

    /**
     * 
     * @return
     *     The arrivalDate
     */
    public ArrivalDate getArrivalDate() {
        return arrivalDate;
    }

    /**
     * 
     * @param arrivalDate
     *     The arrivalDate
     */
    public void setArrivalDate(ArrivalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    /**
     * 
     * @return
     *     The status
     */
    public String getStatus() {
        return status;
    }

    /**
     * 
     * @param status
     *     The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 
     * @return
     *     The schedule
     */
    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * 
     * @param schedule
     *     The schedule
     */
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    /**
     * 
     * @return
     *     The operationalTimes
     */
    public OperationalTimes getOperationalTimes() {
        return operationalTimes;
    }

    /**
     * 
     * @param operationalTimes
     *     The operationalTimes
     */
    public void setOperationalTimes(OperationalTimes operationalTimes) {
        this.operationalTimes = operationalTimes;
    }

    /**
     * 
     * @return
     *     The delays
     */
    public Delays getDelays() {
        return delays;
    }

    /**
     * 
     * @param delays
     *     The delays
     */
    public void setDelays(Delays delays) {
        this.delays = delays;
    }

    /**
     * 
     * @return
     *     The flightDurations
     */
    public FlightDurations getFlightDurations() {
        return flightDurations;
    }

    /**
     * 
     * @param flightDurations
     *     The flightDurations
     */
    public void setFlightDurations(FlightDurations flightDurations) {
        this.flightDurations = flightDurations;
    }

    /**
     * 
     * @return
     *     The airportResources
     */
    public AirportResources getAirportResources() {
        return airportResources;
    }

    /**
     * 
     * @param airportResources
     *     The airportResources
     */
    public void setAirportResources(AirportResources airportResources) {
        this.airportResources = airportResources;
    }

    /**
     * 
     * @return
     *     The flightEquipment
     */
    public FlightEquipment getFlightEquipment() {
        return flightEquipment;
    }

    /**
     * 
     * @param flightEquipment
     *     The flightEquipment
     */
    public void setFlightEquipment(FlightEquipment flightEquipment) {
        this.flightEquipment = flightEquipment;
    }

}
