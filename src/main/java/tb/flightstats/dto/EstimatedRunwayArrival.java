
package tb.flightstats.dto;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class EstimatedRunwayArrival {

    @SerializedName("dateLocal")
    @Expose
    private String dateLocal;
    @SerializedName("dateUtc")
    @Expose
    private String dateUtc;

    /**
     * 
     * @return
     *     The dateLocal
     */
    public String getDateLocal() {
        return dateLocal;
    }

    /**
     * 
     * @param dateLocal
     *     The dateLocal
     */
    public void setDateLocal(String dateLocal) {
        this.dateLocal = dateLocal;
    }

    /**
     * 
     * @return
     *     The dateUtc
     */
    public String getDateUtc() {
        return dateUtc;
    }

    /**
     * 
     * @param dateUtc
     *     The dateUtc
     */
    public void setDateUtc(String dateUtc) {
        this.dateUtc = dateUtc;
    }

}
