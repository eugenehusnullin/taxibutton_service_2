
package tb.flightstats.dto;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Request {

    @SerializedName("airline")
    @Expose
    private Airline airline;
    @SerializedName("flight")
    @Expose
    private Flight flight;
    @SerializedName("date")
    @Expose
    private Date date;
    @SerializedName("utc")
    @Expose
    private Utc utc;
    @SerializedName("airport")
    @Expose
    private Airport airport;
    @SerializedName("codeType")
    @Expose
    private CodeType codeType;
    @SerializedName("extendedOptions")
    @Expose
    private ExtendedOptions extendedOptions;
    @SerializedName("url")
    @Expose
    private String url;

    /**
     * 
     * @return
     *     The airline
     */
    public Airline getAirline() {
        return airline;
    }

    /**
     * 
     * @param airline
     *     The airline
     */
    public void setAirline(Airline airline) {
        this.airline = airline;
    }

    /**
     * 
     * @return
     *     The flight
     */
    public Flight getFlight() {
        return flight;
    }

    /**
     * 
     * @param flight
     *     The flight
     */
    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    /**
     * 
     * @return
     *     The date
     */
    public Date getDate() {
        return date;
    }

    /**
     * 
     * @param date
     *     The date
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * 
     * @return
     *     The utc
     */
    public Utc getUtc() {
        return utc;
    }

    /**
     * 
     * @param utc
     *     The utc
     */
    public void setUtc(Utc utc) {
        this.utc = utc;
    }

    /**
     * 
     * @return
     *     The airport
     */
    public Airport getAirport() {
        return airport;
    }

    /**
     * 
     * @param airport
     *     The airport
     */
    public void setAirport(Airport airport) {
        this.airport = airport;
    }

    /**
     * 
     * @return
     *     The codeType
     */
    public CodeType getCodeType() {
        return codeType;
    }

    /**
     * 
     * @param codeType
     *     The codeType
     */
    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
    }

    /**
     * 
     * @return
     *     The extendedOptions
     */
    public ExtendedOptions getExtendedOptions() {
        return extendedOptions;
    }

    /**
     * 
     * @param extendedOptions
     *     The extendedOptions
     */
    public void setExtendedOptions(ExtendedOptions extendedOptions) {
        this.extendedOptions = extendedOptions;
    }

    /**
     * 
     * @return
     *     The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * 
     * @param url
     *     The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

}
