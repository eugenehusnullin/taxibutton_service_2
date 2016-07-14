
package tb.flightstats.dto;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Appendix {

    @SerializedName("airlines")
    @Expose
    private List<Airline_> airlines = new ArrayList<Airline_>();
    @SerializedName("airports")
    @Expose
    private List<Airport_> airports = new ArrayList<Airport_>();
    @SerializedName("equipments")
    @Expose
    private List<Equipment> equipments = new ArrayList<Equipment>();

    /**
     * 
     * @return
     *     The airlines
     */
    public List<Airline_> getAirlines() {
        return airlines;
    }

    /**
     * 
     * @param airlines
     *     The airlines
     */
    public void setAirlines(List<Airline_> airlines) {
        this.airlines = airlines;
    }

    /**
     * 
     * @return
     *     The airports
     */
    public List<Airport_> getAirports() {
        return airports;
    }

    /**
     * 
     * @param airports
     *     The airports
     */
    public void setAirports(List<Airport_> airports) {
        this.airports = airports;
    }

    /**
     * 
     * @return
     *     The equipments
     */
    public List<Equipment> getEquipments() {
        return equipments;
    }

    /**
     * 
     * @param equipments
     *     The equipments
     */
    public void setEquipments(List<Equipment> equipments) {
        this.equipments = equipments;
    }

}
