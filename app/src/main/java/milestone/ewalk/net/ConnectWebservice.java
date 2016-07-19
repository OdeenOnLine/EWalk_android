package milestone.ewalk.net;

import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.List;
import java.util.Map;

import milestone.ewalk.config.AndroidConfig;
import milestone.ewalk.util.Util;

/**
 * Created by ltf on 2016/6/16.
 */
public class ConnectWebservice {
    private static ConnectWebservice connectWebservice = null;

    public static ConnectWebservice getInStance() {
        connectWebservice = new ConnectWebservice();
        return connectWebservice;
    }

    public String connectEwalk(String methodName,List<PropertyInfo> pils) {
        // 命名空间
        String nameSpace = "http://webservice.kyweb.com/";
        // EndPoint
        String endPoint = AndroidConfig.ip_address+"/kyweb_oracle/ws/rpc?wsdl=CoreService.wsdl";
//        http://120.76.41.254:8080/kyweb_oracle/ws/rpc?wsdl=CoreService.wsdl

        // 指定WebService的命名空间和调用的方法名
        SoapObject rpc = new SoapObject(nameSpace, methodName);


        // 设置需调用WebService接口需要传入的一个参数prams
        for (int i = 0; i < pils.size(); i++) {
            rpc.addProperty(pils.get(i));
        }

        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);

        envelope.bodyOut = rpc;
        // 设置是否调用的是dotNet开发的WebService
        envelope.dotNet = true;
        // 等价于envelope.bodyOut = rpc;
        envelope.setOutputSoapObject(rpc);

        HttpTransportSE transport = new HttpTransportSE(endPoint);
        try {
            // 调用WebService
            transport.call(null, envelope);
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            // 获取返回的数据

            SoapObject object = (SoapObject) envelope.bodyIn;
            // 获取返回的结果
            String result = object.getProperty(0).toString();
            Util.Log("ltf","result========"+result);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
