package milestone.ewalk;


import org.json.JSONObject;

import java.io.Serializable;

import milestone.ewalk.exception.NetRequestException;

public abstract class BaseBean<T> implements Serializable {

	/**
	 * 将Bean实例转化为json对象
	 * 
	 * @return
	 */
	public abstract JSONObject toJSON();

	/**
	 * 将json对象转化为Bean实例
	 * 
	 * @param jsonObj
	 * @return
	 */
	public abstract Object parseJSON(JSONObject jsonObj) throws NetRequestException;

}
