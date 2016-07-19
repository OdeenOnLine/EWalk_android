package milestone.ewalk.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import milestone.ewalk.BaseBean;
import milestone.ewalk.exception.NetRequestException;

/**
 * Created by ltf on 2016/6/20.
 */
public class SevenMessageBean extends BaseBean {
    private String[] steps;
    private double today_mile;
    private long today_last;

    @Override
    public JSONObject toJSON() {
        return null;
    }

    @Override
    public Object parseJSON(JSONObject jsonObj) throws NetRequestException {
        try {
            JSONArray array = jsonObj.getJSONArray("steps");
            steps = new String[array.length()];
            for (int i=0;i<array.length();i++){
                steps[i] = array.optString(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        today_mile = jsonObj.optDouble("todayMile");
        today_last = jsonObj.optLong("todayLast");
        return this;
    }

    public String[] getSteps() {
        return steps;
    }

    public void setSteps(String[] steps) {
        this.steps = steps;
    }

    public double getToday_mile() {
        return today_mile;
    }

    public void setToday_mile(double today_mile) {
        this.today_mile = today_mile;
    }

    public long getToday_last() {
        return today_last;
    }

    public void setToday_last(long today_last) {
        this.today_last = today_last;
    }
}
