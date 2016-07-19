package milestone.ewalk.bean;

import org.json.JSONObject;

import milestone.ewalk.BaseBean;
import milestone.ewalk.exception.NetRequestException;

/**
 * Created by ltf on 2016/7/7.
 */
public class RunRecordBean extends BaseBean{
    private long last;
    private long start_time;
    private long end_time;
    private double mile;
    private long calory;

    @Override
    public JSONObject toJSON() {
        return null;
    }

    @Override
    public Object parseJSON(JSONObject jsonObj) throws NetRequestException {
        last = jsonObj.optLong("last");
        start_time = jsonObj.optLong("startTime");
        end_time = jsonObj.optLong("endTime");
        mile = jsonObj.optDouble("mile");
        calory = jsonObj.optLong("calory");
        return this;
    }

    public long getLast() {
        return last;
    }

    public void setLast(long last) {
        this.last = last;
    }

    public long getStart_time() {
        return start_time;
    }

    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }

    public long getEnd_time() {
        return end_time;
    }

    public void setEnd_time(long end_time) {
        this.end_time = end_time;
    }

    public double getMile() {
        return mile;
    }

    public void setMile(double mile) {
        this.mile = mile;
    }

    public long getCalory() {
        return calory;
    }

    public void setCalory(long calory) {
        this.calory = calory;
    }
}
