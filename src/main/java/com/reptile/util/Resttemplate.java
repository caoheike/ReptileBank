package com.reptile.util;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
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
	private static Logger logger = Logger.getLogger(Resttemplate.class);
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
  			ps.state(id, "bankBillFlow", 300);
        }else{
    		ps.state(id, "bankBillFlow", 200);
    		PushSocket.push(map, UUID, "9000","认证失败");
      		message.put("errorCode",jsonObject.get("errorCode"));//异常处理
  			message.put("errorInfo",jsonObject.get("errorInfo"));
        }
         
		} catch (Exception e) {
			logger.warn("----------将数据推送给数据中心失败--------------",e);
			ps.state(id, "bankBillFlow", 200);
			message.put("errorCode","0003");//异常处理
			message.put("errorInfo","推送失败");
		}
	  	return message;
	  
}
  public Map<String,Object> SendMessageX(Map<String,Object> map,String Url,String id,String UUID){
	  boolean isok = (boolean) map.get("isok");
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
      
      logger.warn("-------------向数据中心推送的数据为："+JSONObject.fromObject(map).toString()+"------------");
      
      HttpEntity<String> formEntity = new HttpEntity<String>(JSONObject.fromObject(map).toString(), headers);
      String result = restTemplate.postForObject(Url, formEntity,String.class);
      logger.warn("-------------数据中心返回的结果为："+result+"------------");
      
      JSONObject jsonObject=JSONObject.fromObject(result);
      if(jsonObject.get("errorCode").equals("0000")){
    		message.put("errorCode","0000");
			message.put("errorInfo","查询成功");
			PushSocket.push(map, UUID, "8000","认证成功");
			if(isok==true){
				ps.state(id, "bankBillFlow", 300);
			}
      }else{
    	  PushSocket.push(map, UUID, "9000","认证失败");
    	  if(isok==true){
    		  ps.state(id, "bankBillFlow", 200);
    	  }   		  
    		message.put("errorCode",jsonObject.get("errorCode"));//异常处理
			message.put("errorInfo",jsonObject.get("errorInfo"));
      }
       
		} catch (Exception e) {
			logger.warn("----------将数据推送给数据中心失败--------------",e);
			if(isok==true){
	    		  ps.state(id, "bankBillFlow", 200);
	    	 } 
			PushSocket.push(map, UUID, "9000","认证失败");
			message.put("errorCode","0003");//异常处理
			message.put("errorInfo","推送失败");
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
