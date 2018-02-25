package com.reptile.util;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author bigyoung
 * @version 1.0
 * 
 *
 */
public class Resttemplate {
	private static Logger logger =  LoggerFactory.getLogger(Resttemplate.class);
	/**
	 * 
	 * @param map 需要推送的数据
	 * @param Url 推送的地址
	 * @return 返回推送状态
	 */
  public Map<String,Object> SendMessage(Map<String,Object> map,String Url){
	  Map<String,Object> message=new HashMap<String, Object>();
	  try {
		  StringHttpMessageConverter m = new StringHttpMessageConverter(Charset.forName("UTF-8"));  
		  RestTemplate restTemplate = new RestTemplateBuilder().additionalMessageConverters(m).build();  
          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.MULTIPART_FORM_DATA);
          MediaType type = MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
          headers.setContentType(type);
          headers.add("Accept", MediaType.APPLICATION_JSON.toString());
          
          logger.warn("-------------请求入参："+JSONObject.fromObject(map).toString()+"------------");
          HttpEntity<String> formEntity = new HttpEntity<String>(JSONObject.fromObject(map).toString(), headers);
          String result = restTemplate.postForObject(Url, formEntity,String.class);
          logger.warn("-------------请求结果："+result+"------------");
          
          JSONObject jsonObject=JSONObject.fromObject(result);
          if(jsonObject.get("errorCode").equals("0000")){
        		message.put("errorCode","0000");
    			message.put("errorInfo","查询成功");
          }else{
        		message.put("errorCode",jsonObject.get("errorCode"));//异常处理
    			message.put("errorInfo",jsonObject.get("errorInfo"));
          }
           
		} catch (Exception e) {
			logger.warn("----------请求失败--------------",e);
			message.put("errorCode","0003");//异常处理
			message.put("errorInfo","推送失败");
		}
	  	return message;
	  
  }
  
  public Map<String,Object> SendMessage(Map<String,Object> map,String Url,String id,String UUID){
		PushState ps=new PushState();
	  Map<String,Object> message=new HashMap<String, Object>();
	  try {
		  StringHttpMessageConverter m = new StringHttpMessageConverter(Charset.forName("UTF-8"));  
		  RestTemplate restTemplate = new RestTemplateBuilder().additionalMessageConverters(m).build();  
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MediaType type = MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        
        logger.warn("-------------向数据中心推送的数据为："+JSONObject.fromObject(map).toString()+"------------");
        
        HttpEntity<String> formEntity = new HttpEntity<String>(JSONObject.fromObject(map).toString(), headers);
        String result = restTemplate.postForObject(Url, formEntity,String.class);
        logger.warn("-------------数据中心返回的结果为："+result+"------------");
        
        JSONObject jsonObject=JSONObject.fromObject(result);
        if(jsonObject.get("errorCode").equals("0000")){
      		message.put("errorCode","0000");
  			message.put("errorInfo","查询成功");
  			PushSocket.push(map, UUID, "8000","认证成功");
  			ps.state(id, "savings", 300);
        }else{
    		ps.state(id, "savings", 200,jsonObject.get("errorInfo").toString());
    		PushSocket.push(map, UUID, "9000",jsonObject.get("errorInfo").toString());
      		message.put("errorCode",jsonObject.get("errorCode"));//异常处理
  			message.put("errorInfo",jsonObject.get("errorInfo"));
        }
         
		} catch (Exception e) {
			logger.warn("----------将数据推送给数据中心失败--------------",e);
			PushSocket.push(map, UUID, "9000","网络异常，认证失败");
			ps.state(id, "savings", 200,"网络异常，认证失败");
			message.put("errorCode","0003");//异常处理
			message.put("errorInfo","推送失败");
		}
	  	return message;
	  
}
  
  /**
   * 
   * @param map
   * @param Url
   * @param id
   * @param UUID
   * @return
   */
  public Map<String,Object> newSendMessageX(Map<String,Object> map,String Url,String id,String UUID){
	  boolean isok =  (Boolean) map.get("isok");
	  map.remove("isok");
	  PushState ps=new PushState();
	  Map<String,Object> message=new HashMap<String, Object>();
	  try {
		  StringHttpMessageConverter m = new StringHttpMessageConverter(Charset.forName("UTF-8"));  
		  RestTemplate restTemplate = new RestTemplateBuilder().additionalMessageConverters(m).build();  
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      MediaType type = MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
      headers.setContentType(type);
      headers.add("Accept", MediaType.APPLICATION_JSON.toString());
      Map<String,String> datamap = new HashMap<String,String>();
      datamap.put("data", JSONObject.fromObject(map).toString());
      String par="";
      if(datamap!=null){
	        Iterator<String> iter = datamap.keySet().iterator(); 
	          while(iter.hasNext()){ 
	              String key=iter.next(); 
	              Object value = datamap.get(key);
	             par=par+key+"="+value+"&";
	          }
	          par=par.substring(0,par.length()-1);
	    }	        
      
      logger.warn("-------------向数据中心推送的数据为："+par+"------------");     
      HttpEntity<String> formEntity = new HttpEntity<String>(par, headers);
      String result = restTemplate.postForObject(Url, formEntity,String.class);
      logger.warn("-------------数据中心返回的结果为："+result+"------------");
      System.out.println("-------------数据中心返回的结果为："+result+"------------");
      JSONObject jsonObject=JSONObject.fromObject(result);
      if(jsonObject.get("errorCode").equals("0000")){
    		message.put("errorCode","0000");
			message.put("errorInfo","查询成功");
			PushSocket.push(map, UUID, "8000","认证成功");
			if(isok==true){
				ps.state(id, "bankBillFlow", 300);
			}
      }else{
    	  PushSocket.push(map, UUID, "9000",jsonObject.get("errorInfo").toString());
    	  if(isok==true){
    		  ps.state(id, "bankBillFlow", 200,jsonObject.get("errorInfo").toString());
    	  } else {
    		  ps.stateX(id, "bankBillFlow", 200,jsonObject.get("errorInfo").toString());
    	  }
    		message.put("errorCode",jsonObject.get("errorCode"));//异常处理
			message.put("errorInfo",jsonObject.get("errorInfo"));
      }
       
		} catch (Exception e) {
			logger.warn("----------将数据推送给数据中心失败--------------",e);
			if(isok==true){
	    		  ps.state(id, "bankBillFlow", 200,"网络异常，认证失败");
	    	 }  else {
	    		  ps.stateX(id, "bankBillFlow", 200,"网络异常，认证失败");
	    	  }
			PushSocket.push(map, UUID, "9000","网络异常，认证失败");
			message.put("errorCode","0003");//异常处理
			message.put("errorInfo","推送失败");
			logger.warn("----交通信用卡------errorCode："+message.get("errorCode")+"-----errorInfo："+message.get("errorInfo"));
		}
	  	return message;
	  
}
  
  
  
  public Map<String,Object> SendMessageX(Map<String,Object> map,String Url,String id,String UUID){
	  boolean isok =  (Boolean) map.get("isok");
	  map.remove("isok");
	  PushState ps=new PushState();
	  Map<String,Object> message=new HashMap<String, Object>();
	  try {
		  StringHttpMessageConverter m = new StringHttpMessageConverter(Charset.forName("UTF-8"));  
		  RestTemplate restTemplate = new RestTemplateBuilder().additionalMessageConverters(m).build();  
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      MediaType type = MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
      headers.setContentType(type);
      headers.add("Accept", MediaType.APPLICATION_JSON.toString());
      System.out.println("-------------向数据中心推送的数据为："+JSONObject.fromObject(map).toString()+"------------");
      logger.warn("-------------向数据中心推送的数据为："+JSONObject.fromObject(map).toString()+"------------");
      
      HttpEntity<String> formEntity = new HttpEntity<String>(JSONObject.fromObject(map).toString(), headers);
      String result = restTemplate.postForObject(Url, formEntity,String.class);
      logger.warn("-------------数据中心返回的结果为："+result+"------------");
      System.out.println("-------------数据中心返回的结果为："+result+"------------");
      JSONObject jsonObject=JSONObject.fromObject(result);
      if(jsonObject.get("errorCode").equals("0000")){
    		message.put("errorCode","0000");
			message.put("errorInfo","查询成功");
			PushSocket.push(map, UUID, "8000","认证成功");
			if(isok==true){
				ps.state(id, "bankBillFlow", 300);
			}
//			PushState.stateByFlag(id,"bankBillFlow",300,isok);
      }else{
    	  PushSocket.push(map, UUID, "9000",jsonObject.get("errorInfo").toString());
    	  if(isok==true){
    		  ps.state(id, "bankBillFlow", 200,jsonObject.get("errorInfo").toString());
    	  } else {
    		  ps.stateX(id, "bankBillFlow", 200,jsonObject.get("errorInfo").toString());
    	  }
    		message.put("errorCode",jsonObject.get("errorCode"));//异常处理
			message.put("errorInfo",jsonObject.get("errorInfo"));
      }
       
		} catch (Exception e) {
			logger.warn("----------将数据推送给数据中心失败--------------",e);
			if(isok==true){
	    		  ps.state(id, "bankBillFlow", 200,"网络异常，认证失败");
	    	 }  else {
	    		  ps.stateX(id, "bankBillFlow", 200,"网络异常，认证失败");
	    	  }
			PushSocket.push(map, UUID, "9000","网络异常，认证失败");
			message.put("errorCode","0003");//异常处理
			message.put("errorInfo","推送失败");
			logger.warn("----交通信用卡------errorCode："+message.get("errorCode")+"-----errorInfo："+message.get("errorInfo"));
		}
	  	return message;
	  
}
  public Map<String,Object> SendMessage(JSONObject jsonObject,String Url,boolean flg){
	  Map<String,Object> message=new HashMap<String, Object>();
	  try {
		  StringHttpMessageConverter m = new StringHttpMessageConverter(Charset.forName("UTF-8"));  
		  RestTemplate restTemplate = new RestTemplateBuilder().additionalMessageConverters(m).build();  
		  HttpHeaders headers = new HttpHeaders();
		  MediaType type = MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
		  headers.setContentType(type);
		  headers.add("Accept", MediaType.APPLICATION_JSON.toString());
		  logger.warn("-------------向数据中心推送的数据为："+jsonObject.toString()+"------------");
		  HttpEntity<String> formEntity = new HttpEntity<String>(jsonObject.toString(), headers);
		  String result = restTemplate.postForObject(Url, formEntity,String.class);
		  logger.warn("-------------数据中心返回的结果为："+result+"------------");
		  JSONObject jsonObjects=JSONObject.fromObject(result);
		  if(jsonObjects.equals("0000")){
			  message.put("errorCode","0000");
			  message.put("errorInfo","查询成功");
		  }else{
			  message.put("errorCode",jsonObjects.get("errorCode"));//异常处理
			  message.put("errorInfo",jsonObjects.get("errorInfo"));
		  }
		  
	  } catch (Exception e) {
		  logger.warn("----------将数据推送给数据中心失败--------------",e);
		  message.put("errorCode","0003");//异常处理
		  message.put("errorCode","推送失败");
	  }
	  return message;
	  
  }
  
  //ludangwei 
  public Map<String,Object> SendMessageCredit(JSONObject jsonObject,String Url){
	  Map<String,Object> message=new HashMap<String, Object>();
	  try {
		  StringHttpMessageConverter m = new StringHttpMessageConverter(Charset.forName("UTF-8"));  
		  RestTemplate restTemplate = new RestTemplateBuilder().additionalMessageConverters(m).build();  
		  HttpHeaders headers = new HttpHeaders();
		  MediaType type = MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
		  headers.setContentType(type);
		  headers.add("Accept", MediaType.APPLICATION_JSON.toString());
		  
	      logger.warn("-------------向数据中心推送的数据为："+JSONObject.fromObject(jsonObject).toString()+"------------");
		  HttpEntity<String> formEntity = new HttpEntity<String>(jsonObject.toString(), headers);
		  String result = restTemplate.postForObject(Url, formEntity,String.class);
		  logger.warn("-------------数据中心返回的结果为："+result+"------------");
		  JSONObject jsonObjects=JSONObject.fromObject(result);
		  if("0".equals(jsonObjects.get("errorCode").toString())){
			  message.put("ResultCode","0000");
			  message.put("ResultInfo","查询成功");
		  }else{
			  message.put("ResultCode",jsonObjects.get("errorCode"));//异常处理
			  message.put("ResultInfo",jsonObjects.get("errorInfo"));
		  }
		  
	  } catch (Exception e) {
		  logger.warn("----------将数据推送给数据中心失败--------------",e);
		  message.put("ResultCode","0003");//异常处理
		  message.put("ResultInfo","推送失败");
	  }
	  return message;
	  
  }
  		 

}
