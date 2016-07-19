package milestone.ewalk.bean;

import org.json.JSONObject;

import milestone.ewalk.BaseBean;
import milestone.ewalk.exception.NetRequestException;

/**
 * Created by ltf on 2016/7/14.
 */
public class MessageBean extends BaseBean{
    private String title;
    private long time;
    private String subTitle;
    private String content;

    @Override
    public JSONObject toJSON() {
        return null;
    }

    @Override
    public Object parseJSON(JSONObject jsonObj) throws NetRequestException {
        title = jsonObj.optString("title");
        time = jsonObj.optLong("time");
        subTitle = jsonObj.optString("subTitle");
        content = jsonObj.optString("content");
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
