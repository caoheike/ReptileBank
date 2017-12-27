package com.reptile.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Bigyoung
 * @version V1.0
 * @Description 通用推送类
 * @date 2017-11-13 2:50
 *
 */
public class PushState {
	private static Logger logger = LoggerFactory.getLogger(PushSocket.class);

	
	
	public static void state(String UserCard,String approveName ,int stat){
		application applications=new application();
		Map<String, Object> map1=new HashMap<String, Object>();
		Map<String,Object> stati=new HashMap<String, Object>();
		Map<String,Object> data=new HashMap<String, Object>();
		stati.put("cardNumber",UserCard);
		stati.put("approveName" , approveName);
		stati.put("approveState",stat+"");
		data.put("data", stati);
		Resttemplate resttemplatestati = new Resttemplate();
		logger.warn("----------------*****************------stat：" + stat);

	map1=resttemplatestati.SendMessage(data,application.sendip+"/HSDC/authcode/Autherized");
	}
}
