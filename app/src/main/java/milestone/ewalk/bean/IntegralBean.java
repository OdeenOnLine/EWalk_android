package milestone.ewalk.bean;

import org.json.JSONException;
import org.json.JSONObject;

import milestone.ewalk.BaseBean;
import milestone.ewalk.exception.NetRequestException;

/**
 * Created by ltf on 2016/6/20.
 */
public class IntegralBean extends BaseBean {
    private JSONObject pdtime;
    private String pdsource;
    private double pdpoints;
    private long time=0;


    @Override
    public JSONObject toJSON() {
        return null;
    }

    @Override
    public Object parseJSON(JSONObject jsonObj) throws NetRequestException {
        try {
            pdtime = jsonObj.getJSONObject("pdtime");
            if(pdtime!=null) {
                time = pdtime.optLong("time");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pdsource = jsonObj.optString("pdsource");
        pdpoints = jsonObj.optDouble("pdpoints");
        return this;
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
