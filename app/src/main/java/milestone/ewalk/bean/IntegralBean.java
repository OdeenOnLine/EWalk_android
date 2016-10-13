package milestone.ewalk.bean;

import org.json.JSONObject;

import milestone.ewalk.BaseBean;
import milestone.ewalk.exception.NetRequestException;

/**
 * Created by ltf on 2016/6/20.
 */
public class IntegralBean extends BaseBean {
    private String pdtime;
    private String pdsource;
    private double pdpoints;


    @Override
    public JSONObject toJSON() {
        return null;
    }

    @Override
    public Object parseJSON(JSONObject jsonObj) throws NetRequestException {

        pdtime = jsonObj.optString("pdtime");
        pdsource = jsonObj.optString("pdsource");
        pdpoints = jsonObj.optDouble("pdpoints");
        return this;
    }

    public String getPdtime() {
        return pdtime;
    }

    public void setPdtime(String pdtime) {
        this.pdtime = pdtime;
    }

    public String getPdsource() {
        return pdsource;
    }

    public void setPdsource(String pdsource) {
        this.pdsource = pdsource;
    }

    public double getPdpoints() {
        return pdpoints;
    }

    public void setPdpoints(double pdpoints) {
        this.pdpoints = pdpoints;
    }
}
