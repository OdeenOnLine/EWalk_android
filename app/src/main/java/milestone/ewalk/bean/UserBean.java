package milestone.ewalk.bean;

import org.json.JSONObject;

import milestone.ewalk.BaseBean;
import milestone.ewalk.exception.NetRequestException;

/**
 * Created by ltf on 2016/6/20.
 * 用户
 */
public class UserBean extends BaseBean {
    private String name;
    private String pwd;
    private double record;
    private int height;
    private double weight;
    private String union;
    private String company;
    private String poster;
    private long steps;
    private double points;
    private String token;
    private long calory;
    private int userId;

    @Override
    public JSONObject toJSON() {
        return null;
    }

    @Override
    public Object parseJSON(JSONObject jsonObj) throws NetRequestException {
        if(jsonObj.has("userId")) {
            userId = jsonObj.optInt("userId");
        }
        if(jsonObj.has("token")) {
            token = jsonObj.optString("token");
        }
        name = jsonObj.optString("name");
        record = jsonObj.optDouble("record");
        height = jsonObj.optInt("height");
        weight = jsonObj.optDouble("weight");
        union = jsonObj.optString("union");
        company = jsonObj.optString("company");
        poster = jsonObj.optString("poster");
        steps = jsonObj.optLong("steps");
        points = jsonObj.optDouble("points");
        calory = jsonObj.optLong("calory");
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public double getRecord() {
        return record;
    }

    public void setRecord(double record) {
        this.record = record;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getUnion() {
        return union;
    }

    public void setUnion(String union) {
        this.union = union;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public long getSteps() {
        return steps;
    }

    public void setSteps(long steps) {
        this.steps = steps;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public long getCalory() {
        return calory;
    }

    public void setCalory(long calory) {
        this.calory = calory;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
