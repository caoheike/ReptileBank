package com.reptile.util;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * 
 * @author Bigyoung
 * @version V1.0
 * @Description 通用发包请求类 httpClient
 * @date 2017-11-13 2:50
 *
 */
public class SimpleHttpClient {

    public static CloseableHttpClient httpClient;

    private static CookieStore cookieStore;

    static {
    
        cookieStore  = new BasicCookieStore();

        // ��CookieStore���õ�httpClient��
        httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
     
    }
    
    public static String getCookie(String name){
       List<Cookie> cookies =  cookieStore.getCookies();
        for(Cookie cookie : cookies){
        	System.out.println("�ԡԡԡԡԡ�"+cookie.getName());
            if(cookie.getName().equalsIgnoreCase(name)){
                return cookie.getValue();
            }
        }
        return null;
    
        
    }
    
   
    


    /**
     * GET ����
     * @param url
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static String get(String url) throws ClientProtocolException, IOException {
    	
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        return EntityUtils.toString(httpResponse.getEntity());
    }

    
    /**
     * Get请求
     * @param url
     * @param headers
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static String get(String url,Map<String,String> headers) throws ClientProtocolException, IOException {
    	HttpGet httpGet = new HttpGet(url);
    	for(Map.Entry<String, String> entry : headers.entrySet()){
    		httpGet.addHeader(entry.getKey(), entry.getValue());
        }
    	HttpResponse httpResponse = httpClient.execute(httpGet);
    	return EntityUtils.toString(httpResponse.getEntity());
    }
    
    /**
     * POST ����
     * @param url
     * @param params
     * @param headers
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public static String post(String url, Map<String, Object> params, Map<String, String> headers) throws ParseException, IOException{
        HttpPost httpPost = new HttpPost(url);
        UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(getParam(params), "UTF-8");
        httpPost.setEntity(postEntity);
        if(headers != null){
            addHeaders(httpPost, headers);
        }
        HttpResponse httpResponse = httpClient.execute(httpPost);
   
        return EntityUtils.toString(httpResponse.getEntity());
    }

    /**
     * ����
     * @param parameterMap
     * @return
     */
    private static List<NameValuePair> getParam(Map<String, Object> parameterMap) {
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        for(Map.Entry<String, Object> entry : parameterMap.entrySet()){
            param.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
        }
        return param;
    }
    
    /**
     * ���ͷ
     * @param httpRequest
     * @param headerMap
     */
    private static void addHeaders(HttpEntityEnclosingRequestBase httpRequest, Map<String, String> headerMap){
        for(Map.Entry<String, String> entry : headerMap.entrySet()){
            httpRequest.addHeader(entry.getKey(), entry.getValue());
        }
    }
    
    public static String httpPostWithJSON(String url,Map headers) throws Exception {

        HttpPost httpPost = new HttpPost(url);
        if(headers != null){
            addHeaders(httpPost, headers);
        }
        CloseableHttpClient client = HttpClients.createDefault();
        String respContent = null;
        
//        json方式
        JSONObject jsonParam = new JSONObject();  
        jsonParam.put("CreditAcType", "0010");
   
        StringEntity entity = new StringEntity(jsonParam.toString(),"utf-8");//解决中文乱码问题    
        entity.setContentEncoding("UTF-8");    
        entity.setContentType("application/json");    
        httpPost.setEntity(entity);
        System.out.println();
        
    
//        表单方式
//        List<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>(); 
//        pairList.add(new BasicNameValuePair("name", "admin"));
//        pairList.add(new BasicNameValuePair("pass", "123456"));
//        httpPost.setEntity(new UrlEncodedFormEntity(pairList, "utf-8"));   
        
        
        HttpResponse resp = client.execute(httpPost);
        if(resp.getStatusLine().getStatusCode() == 200) {
            HttpEntity he = resp.getEntity();
            respContent = EntityUtils.toString(he,"UTF-8");
        }
        return respContent;
    }
  
}