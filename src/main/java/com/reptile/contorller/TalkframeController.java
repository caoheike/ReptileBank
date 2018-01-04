package com.reptile.contorller;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Component;

import com.reptile.Bank.CustomSpringConfigurator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cuiyongjuan
 * @version v1.0
 * @deprecated APP交互处理
 * @date 2017年11月16日10:51:14
 */
@Component

@ServerEndpoint(value = "/hello",configurator=CustomSpringConfigurator.class) 
public class TalkframeController {
	private static  Map<String,Session> wsUserMap = new HashMap<String,Session>();
	private static  Map<String,String> wsInfoMap = new HashMap<String,String>();
	
    public static Map<String, String> getWsInfoMap() {
		return wsInfoMap;
	}
	public static void setWsInfoMap(Map<String, String> wsInfoMap) {
		TalkframeController.wsInfoMap = wsInfoMap;
	}
	public static Map<String, Session> getWsUserMap() {
		return wsUserMap;
	}
	public static void setWsUserMap(Map<String, Session> wsUserMap) {
		TalkframeController.wsUserMap = wsUserMap;
	}
	@OnOpen
    public void onopen(Session session){
        System.out.println("连接成功");
//        try {
//            session.getBasicRemote().sendText("1234...");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    @OnClose
    public void onclose(Session session){
        System.out.println("close....");
    }
    @OnMessage
    public void onsend(Session session,String msg){
    	if(msg!=null&&!msg.equals("")){
    		try {
    			
//    			JSONObject json1=JSONObject.fromObject(msg);
//    			json1.accumulate("resultCode", "0000");
    			//session.getBasicRemote().sendObject(json1);
    		
				//session.getBasicRemote().sendText(json1.toString());//链接成功
    			System.out.println(msg.substring(0, msg.length()-1).toString()+",\"resultCode\": \"1111\"}");
				session.getBasicRemote().sendText(msg.substring(0, msg.length()-1).toString()+",\"resultCode\":\"1111\"}");//链接成功
				
			} catch (Exception e) {
				
				e.printStackTrace();
			}
    		JSONObject json=JSONObject.fromObject(msg);
    		msg=json.get("req").toString();
    		wsUserMap.put(msg, session);
    		
    		wsInfoMap.put(msg, json.get("seq_id").toString());
    		
    		
    	}else{
    		//System.out.println("qddd");
    		onclose(session);
    	}
     
    }
    
    
}
