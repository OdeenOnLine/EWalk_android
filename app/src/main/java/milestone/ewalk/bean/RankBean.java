package milestone.ewalk.bean;

import org.json.JSONObject;

import milestone.ewalk.BaseBean;
import milestone.ewalk.exception.NetRequestException;

/**
 * Created by ltf on 2016/7/11.
 */
public class RankBean extends BaseBean{
    private int companyId;
    private String name;
    private String companyName;
    private String poster;
    private int rank;
    private int steps;
    private double wanbu;
    private int userId;

    @Override
    public JSONObject toJSON() {
        return null;
    }

    @Override
    public Object parseJSON(JSONObject jsonObj) throws NetRequestException {
        companyId = jsonObj.optInt("companyId");
        name = jsonObj.optString("name");
        companyName = jsonObj.optString("companyName");
        poster = jsonObj.optString("poster");
        rank = jsonObj.optInt("rank");
        steps = jsonObj.optInt("steps");
        wanbu = jsonObj.optDouble("wanbu");
        userId = jsonObj.optInt("userId");
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getWanbu() {
        return wanbu;
    }

    public void setWanbu(double wanbu) {
        this.wanbu = wanbu;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
