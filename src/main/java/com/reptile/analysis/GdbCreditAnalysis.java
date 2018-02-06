package com.reptile.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hoomsun.keyBoard.HttpWatchUtil;
import com.hoomsun.keyBoard.ReadFromFile;
import com.hoomsun.keyBoard.SendKeys;
import com.reptile.util.CountTime;
import com.reptile.util.Dates;
import com.reptile.util.DriverUtil;
import com.reptile.util.PushSocket;
import com.reptile.util.PushState;
import com.reptile.util.Resttemplate;
import com.reptile.util.SimpleHttpClient;
import com.reptile.util.application;
import com.reptile.winio.VirtualKeyBoard;

import net.sf.json.JSONObject;


@Service("GdbCreditAnalysis")
public class GdbCreditAnalysis {
	Resttemplate resttemplate = new Resttemplate();
	private static Logger logger= LoggerFactory.getLogger(VirtualKeyBoard.class);
	/**
	 * 广发银行信用卡
	 * @param number
	 * @param pwd
	 * @param usercard
	 * @param UUID
	 * @return
	 * @throws Exception
	 */
	public synchronized Map<String, Object> GDBLogin(String number, String pwd,
			String usercard, String UUID,String timeCnt) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> data = new HashMap<String, Object>();
		PushSocket.push(map, UUID, "1000","广发银行信用卡登录中");
		logger.warn("########【广发信用卡########登陆开始】########【用户名：】"
				+ number + "【密码：】" + pwd+"【身份证号：】"+usercard);	
		
		boolean isok = CountTime.getCountTime(timeCnt);
		if(isok==true){
			PushState.state(usercard, "bankBillFlow", 100);
		}
		WebDriver driver = null;
		JavascriptExecutor jss = null;
		String imgtext = "";
		try {
			driver = DriverUtil.getDriverInstance("ie");
			WebDriverWait wait = new WebDriverWait(driver, 20);
			driver.manage().window().maximize();
			driver.get("https://ebanks.cgbchina.com.cn/perbank/");
			Thread.sleep(1000);

			wait.until(ExpectedConditions.presenceOfElementLocated(By
					.linkText("广发银行官方网站")));
			WebElement elements = driver.findElement(By.id("loginId"));
			
			
			elements.click();
			elements.sendKeys(number);
			Thread.sleep(1000);

//			SendKeys.sendStr(1193+80, 358, pwd);
			SendKeys.sendStr(1193+80, 358, pwd);//本地
			Thread.sleep(1000);
			logger.warn("########【广发信用卡获取图形验证码图片】########【身份证号：】"+usercard);
			WebElement keyWord = driver.findElement(By.id("verifyImg"));
			imgtext = VirtualKeyBoard.downloadGFImgss(driver, keyWord);
			logger.warn("########【广发信用卡图形验证码打码结果 imgtext:】"+imgtext+"########【身份证号：】"+usercard);
			if (imgtext.contains("超时") || imgtext.equals("")) {
				logger.warn("########【广发信用卡获取图形验证码时超时】########【身份证号：】"+usercard);
				map.put("errorInfo", "连接超时");
				map.put("errorCode", "0001");
				PushSocket.push(map, UUID, "3000","连接超时");
				if(isok==true){
					PushState.state(usercard, "bankBillFlow", 200,"连接超时");
				}else {
					PushState.stateX(usercard, "bankBillFlow", 200,"连接超时");
				}
				logger.warn("--------广发银行信用卡--------------登陆失败---------身份证号："+ usercard+"--------返回信息为："+map);
				DriverUtil.close(driver);
			}
			WebElement _vTokenId = driver.findElement(By.id("captcha"));
			_vTokenId.sendKeys(imgtext);
			WebElement loginButton = driver.findElement(By.id("loginButton"));
			
			loginButton.click(); /* 点击登陆 */
			Thread.sleep(3000);
			//调出httpwatch
			HttpWatchUtil.openHttpWatch();
			//弹窗的内容
			//System.out.println(driver.getPageSource());
		} catch (Exception e) {
			logger.warn("########【广发银行信用卡登陆失败，进入try-catch】########【原因：】网络异常，登录失败【身份证号：】"+usercard);
			logger.warn("-----------广发银行登录失败----------",e);
			map.put("errorInfo", "网络异常,请重试！！");
			map.put("errorCode", "0001");
			PushSocket.push(map, UUID, "3000","网络异常");
			if(isok==true){
				PushState.state(usercard, "bankBillFlow", 200,"网络异常");
			}else {
				PushState.stateX(usercard, "bankBillFlow", 200,"网络异常");
			}
			driver.quit();
			logger.warn("--------广发银行信用卡--------------登陆失败---------身份证号："+ usercard+"--------返回信息为："+map);
			return map;
		}
		if(imgtext.length()<4) {
			logger.warn("########【广发银行信用卡打码结果位数不够】【身份证号：】"+usercard);
			DriverUtil.close(driver);
			map = GDBLogin(number, pwd, usercard, UUID,timeCnt);
		}
		String str = DriverUtil.alertFlag(driver);
	
		if(!str.isEmpty()){
				if(str.contains("验证码")){
					logger.warn("########【广发银行信用卡打码结果不正确】【身份证号：】"+usercard);
					DriverUtil.close(driver);
					map = GDBLogin(number, pwd, usercard, UUID,timeCnt);
					//密码不为空并且报密码为空错误试递归
				}else if(str.contains("请输入密码")&&!"".equals(pwd)){
					logger.warn("########【广发银行信用卡密码未正确输入】【身份证号：】"+usercard);
					DriverUtil.close(driver);
					map = GDBLogin(number, pwd, usercard, UUID,timeCnt);
				}else{
					logger.warn("########【广发银行信用卡登陆失败  原因："+str+"】【身份证号：】"+usercard);
					map.put("errorInfo", str);
					map.put("errorCode", "0001");
					PushSocket.push(map, UUID, "3000",str);
					if(isok==true){
						PushState.state(usercard, "bankBillFlow", 200,str);
					}else {
						PushState.stateX(usercard, "bankBillFlow", 200,str);
					}
					logger.warn("--------广发银行登陆------------失败-----------用户名："+ number+"--------原因为："+map);
					DriverUtil.close(driver);
				}
				return map;								
		}else if(DriverUtil.waitById("errorMessage", driver, 3)){
			
				String errorMessage = driver.findElement(By.id("errorMessage")).getText();
				map.put("errorInfo", errorMessage);
				map.put("errorCode", "0001");
				PushSocket.push(map, UUID, "3000",errorMessage);
				logger.warn("########【广发银行信用卡登陆失败  原因："+errorMessage+"】【身份证号：】"+usercard);
				if(isok==true){
					PushState.state(usercard, "bankBillFlow", 200,errorMessage);
				}else {
					PushState.stateX(usercard, "bankBillFlow", 200,errorMessage);
				}
				logger.warn("--------广发银行登陆------------失败-----------用户名："+ number+"--------原因为："+map);
				DriverUtil.close(driver);
				return map;									
		}else if(DriverUtil.waitByTitle("广发银行个人网上银行", driver, 15)&&driver.getPageSource().contains("您好，欢迎您登录广发银行个人网银")){
			int flag = 0;
			try {
				
				logger.warn("--------广发银行登陆------------成功-----------用户名："+ number+"-----------");
				PushSocket.push(map, UUID, "2000","广发银行信用卡登陆成功");
				logger.warn("########【广发银行信用卡登陆成功】【身份证号：】"+usercard);
				Thread.sleep(8000);
				PushSocket.push(map, UUID, "5000","广发银行信用卡数据获取中");
				flag = 1;
				logger.warn("########【广发银行信用卡开始获取数据】【身份证号：】"+usercard);
				String sid = driver.getPageSource()
			            .substring(driver.getPageSource().indexOf("_emp_sid = '"),driver.getPageSource().indexOf("';"))
			            .replaceAll("_emp_sid = '", "");
				List<String> listinfo = new ArrayList<String>();
				listinfo = getInfos(driver,listinfo,number,sid);
				
				
				
				map = analysisData(listinfo,usercard);
				map.put("userAccount", number);
				logger.warn("*************"+JSONObject.fromObject(map).toString());
				
				
				
				PushSocket.push(map, UUID, "6000","广发银行信用卡数据获取成功");
				logger.warn("########【广发银行信用卡数据获取成功】【身份证号：】"+usercard);
				
//				data.put("html", listinfo);
//				data.put("backtype", "GDB");
//				data.put("idcard", usercard);
//				data.put("userAccount", number);
//				map.put("data", data);
				map.put("isok", isok);
				Resttemplate ct = new Resttemplate();
				logger.warn("########【广发银行信用卡开始推送数据】【身份证号：】"+usercard);
				flag = 2;
				map = ct.newSendMessageX(map, application.sendip
						+ "/HSDC/BillFlow/BillFlowByreditCard", usercard,UUID);
				logger.warn("########【广发银行信用卡推送完成    身份证号：】"+usercard+"数据中心返回结果："+map.toString());
				}catch (Exception e) {
					if(flag == 1) {
						logger.warn("--------------flag="+flag+"----------网络异常，数据获取异常");
						PushSocket.push(map, UUID, "7000","网络异常");					
					}else if(flag == 2) {
						logger.warn("--------------flag="+flag+"----------网络异常，认证失败");
						PushSocket.push(map, UUID, "9000","网络异常");						
					}
					logger.warn("########【广发银行信用卡登陆成功后进入try-catch】【身份证号：】"+usercard);
					if(isok==true){
						PushState.state(usercard, "bankBillFlow", 200,"网络异常");
					}else {
						PushState.stateX(usercard, "bankBillFlow", 200,"网络异常");
					}
					map.put("errorCode", "0002");
					map.put("errorInfo", "网络错误");
					logger.warn("----广发信用卡------errorCode："+map.get("errorCode")+"-----errorInfo："+map.get("errorInfo"));
				}finally{
					DriverUtil.close(driver);
				}			
			}else {
				logger.warn("########【广发银行信用卡登陆中页面未跳转，进入else】【身份证号：】"+usercard);
				logger.warn("-----------广发银行登陆失败----------");
				map.put("errorInfo", "网络异常,请重试！！");
				map.put("errorCode", "0001");
				PushSocket.push(map, UUID, "3000","系统繁忙，请重试");
				if(isok==true){
					PushState.state(usercard, "bankBillFlow", 200,"系统繁忙，请重试");
				}else {
					PushState.stateX(usercard, "bankBillFlow", 200,"系统繁忙，请重试");
				}
				DriverUtil.close(driver);
				logger.warn("----广发信用卡------errorCode："+map.get("errorCode")+"-----errorInfo："+map.get("errorInfo"));
			}
		logger.warn("--------------广发银行信用卡------------查询结束-----------返回信息为："+map+"---------------");
		return (map);
	}
	/**
	 *返回要获取数据的月份列表
	 * @return
	 */
	public List<String> getQueryMonth() {
		List<String> monthList = new ArrayList<String>();
		String beforeMonth = "";
		for(int i=0;i<6;i++) {
			if(i==0) {
				beforeMonth = Dates.beforMonth(0);
			}else {
				beforeMonth = Dates.beforMonth(i);
			}
			
			monthList.add(beforeMonth);
		}
		return monthList;
	}
	 
	public List<String> getInfos(WebDriver driver,List<String> listinfo,String number,String sid) throws InterruptedException{
		List<String> monthList = new ArrayList<String>();
		String cookie = getcokie("JSESSIONID");
		cookie = cookie.replace("<header name=\"Cookie\">", "").replace("</header>", "").trim();
		monthList = getQueryMonth();
//		Set<Cookie> cookies = driver.manage().getCookies();
//	    StringBuffer cookie = new StringBuffer();
//	    for (Cookie oneCookie : cookies) {
//	      cookie = cookie.append(oneCookie.getName()+"="+oneCookie.getValue()+";");
//	    }
	    String currentTime = Dates.currentTime();
		Map<String, String> headers = new HashMap<String, String>(16);
		headers.put("Referer", "https://ebanks.cgbchina.com.cn/perbank/html/creditcard/b080102_creditBillResult.htm?HTMVersion="+currentTime);
		headers.put("Host", "ebanks.cgbchina.com.cn");
		headers.put("Cookie", cookie);
		
		headers.put("Accept", "text/html, application/xhtml+xml, */*");
		headers.put("Accept-Encoding", "gzip, deflate");
		headers.put("Accept-Language", "zh-CN");
		headers.put("Connection", "Keep-Alive");
		headers.put("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");
		
		for(int i=0;i<6;i++) {
				String response="";
				try {
					response = SimpleHttpClient.get("https://ebanks.cgbchina.com.cn/perbank/CR1080.do?"
							+ "currencyType=&creditCardNo="+number+"&billDate="+monthList.get(i).substring(0, 6)+"&billType=1&abundantFlag=0&"
							+ "terseFlag=0&showWarFlag=1&EMP_SID="+sid,headers);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.warn("--------广发银行登陆------------账单查询：第"+i+1+"次请求具体内容："+response);		
				listinfo.add(response);
			
		}
		return listinfo;

	}
	
	public static String getcokie(String str) throws InterruptedException {
		//保存快捷键 ctrl+shift+X
	    SendKeys.key(600, 1);
	    SendKeys.key(500, 1);
	    
	    Thread.sleep(3000);
	    SendKeys.key(502, 1);
	    Thread.sleep(1000);
	    SendKeys.key(600, 2);
	    SendKeys.key(500, 2);
	    SendKeys.key(502, 2);
	    Thread.sleep(1000);
	    String uuid = UUID.randomUUID().toString().substring(0, 10);
	    //文件路径
	    File file = new File("C://reptile/httpwatch/");
	    if(!file.exists()){
	      file.mkdirs();
	    }
	    //文件名称
	    String fileName = uuid+".xml";
	    //文件路径+文件名称
	    String filePath = file.getAbsolutePath()+fileName;//"httpwatch"+uuid+".xml";
	    
	    SendKeys.sendStr(filePath);
	    Thread.sleep(1000);
	    
	    //保存 Alt + s
	    SendKeys.key(602, 1);
	    SendKeys.key(402, 1);
	    SendKeys.key(602, 2);
	    SendKeys.key(402, 2);
	    
	    Thread.sleep(1000);
	    
	    String strcookie = ReadFromFile.read(filePath,str);
	    return strcookie;
	}
	
	/**
	   * 解析数据
	   * @return
	   */
	  public Map<String,Object> analysisData(List<String> list,String idcard){
	    
	    List<JSONObject> bankList = new ArrayList<JSONObject>();
	    for (String item : list) {
	      Document doc = Jsoup.parse(item);
	      Elements fixBand7s = doc.getElementsByAttributeValue("id", "fixBand7");
	      if(fixBand7s.size()==0) {
	    	  continue;
	      }
	      Element basic = fixBand7s.get(0);
	      
	      Element detail = fixBand7s.get(1);
	      
	      JSONObject AccountSummary = new JSONObject();
	      AccountSummary.put("RMBCurrentAmountDue", this.analysisBasicInfo(basic, 1));
	      AccountSummary.put("PaymentDueDate", this.analysisBasicInfo(basic, 3));
	      AccountSummary.put("RMBMinimumAmountDue", this.analysisBasicInfo(basic, 2));
	      AccountSummary.put("CreditLimit", this.analysisBasicInfo(basic, 5));
	      AccountSummary.put("StatementDate","");	      
	      JSONObject object = new JSONObject();
	      object.put("accountSummary", AccountSummary);
	      object.put("payRecord", this.analysisDetail(detail));
	      
	      bankList.add(object);
	      
	    }
	    
	    Map<String,Object> map = new HashMap<String, Object>();
	    Map<String,Object> data = new HashMap<String, Object>();
	    data.put("bankList", bankList);
	    map.put("data", data);
	    map.put("idcard", idcard);
	    map.put("userAccount","广发银行");
	    map.put("bankname", "广发银行");
	    map.put("backtype", "GDB");
	    
	    logger.warn("解析后结果********************"+data.toString());
	    return map;
	  }
	  
	  /**
	   * 获取数据 
	   * 本期应还款额：//*[@id="fixBand7"]/table/tbody/tr/td[2]/div/font
	   * 本期最低还款额：//*[@id="fixBand7"]/table/tbody/tr/td[3]/div/font
	   * 最后还款日：//*[@id="fixBand7"]/table/tbody/tr/td[4]/div/font
	   * 额度：//*[@id="fixBand7"]/table/tbody/tr/td[6]/div/font
	   * 
	   * @param fixBand 
	   * @param i 数据在第几行
	   * @return
	   */
	  private String  analysisBasicInfo(Element fixBand,int i) {
	    
	    return fixBand.select("table").get(0).select("td").get(i).select("font").get(0).text();
	  }
	  
	  
	  /**
	   * 解析每个月的交易明细
	   * @param fixBand
	   * @return
	   */
	  private List<Map<String,Object>> analysisDetail(Element fixBand){
	     Elements trs = fixBand.select("tr");
	     List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
	     for (int i = 0; i < trs.size(); i++) {
	       Elements tds = trs.get(i).select("td");
	       Map<String,Object> map = new HashMap<String,Object>();
	       for (int j = 0; j < tds.size(); j++) {
	        String text = tds.get(j).text();
	        if(j == 0) {
	          map.put("tran_date", text);
	        }else if(j == 2) {
	          map.put("tran_desc", text);
	        }else if(j == 3) {
	          map.put("post_amt", text);
	        }
	      }
	       list.add(map); 
	    }
	     
	     return list ;
	  }
}
