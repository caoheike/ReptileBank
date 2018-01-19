package com.reptile.util;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Component
@ConfigurationProperties(  prefix = "manInfo" )
/**
 * 
 * @author Bigyoung
 * @version V1.0
 * @Description 推送地址
 * @date 2017-11-13 2:50
 *
 */
public class application {
	public static String sendip="http://113.200.105.37:8080";//外地
//public static String sendip="http://192.168.3.16:8089";//本地
//public static String sendip="http://117.34.70.217:8080";//测试环境

public static List<String> Getdate() {
	List<String> list=new ArrayList<String>();
	for (int i = 0; i >=-5; i--) {
		SimpleDateFormat matter=new SimpleDateFormat("yyyyMM");
	    Calendar calendar = Calendar.getInstance();

	    //获取当前时间的前6个月
	    calendar.add(Calendar.MONTH,i);
	    Date date02 = calendar.getTime();
	    BigDecimal time02=new BigDecimal(matter.format(date02));
	    list.add(time02.toString());

	 
	}

	return list;
}

}
