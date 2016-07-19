package milestone.ewalk.util;

import java.math.BigDecimal;
/**    
 * * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指    
 * * 定精度，以后的数字四舍五入。    
 * * @param v1 被除数
 * * @param scale 表示表示需要精确到小数点以后几位。    
 * * @return 两个参数的商    
 * */   
public class BigDecimalUtil {
	// 进行除法运算 
	public static double div(double d1,double d2,int len) {
		double result = 0;
		
		BigDecimal b1 = new BigDecimal(Double.toString(d1)); 
		BigDecimal b2 = new BigDecimal(Double.toString(d2));
		try {
			result = b1.divide(b2,len,BigDecimal.ROUND_HALF_UP).doubleValue();
		} catch (Exception e) {
		
			result = 0;
		}
		
		
		return result; 
	}

    /**
     * 小数四舍五入
     * @return
     */
    public static double doubleChange(double num,int digit){
        BigDecimal b   =   new   BigDecimal(num);
        return b.setScale(digit,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

}
