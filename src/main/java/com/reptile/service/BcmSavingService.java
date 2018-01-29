package com.reptile.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Augmenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoomsun.keyBoard.HttpWatchUtil;
import com.hoomsun.keyBoard.SendKeys;
import com.reptile.util.CYDMDemo;
import com.reptile.util.CrawlerUtil;
import com.reptile.util.Dates;
import com.reptile.util.DriverUtil;
import com.reptile.util.JsonUtil;
import com.reptile.util.PushSocket;
import com.reptile.util.PushState;
import com.reptile.util.Resttemplate;
import com.reptile.util.SimpleHttpClient;
import com.reptile.util.application;

/**
 *
 * @author Bigyoung
 * @version V1.0
 * @Description 交通储蓄卡
 * @date 2017-11-13 2:50
 *
 */
public class BcmSavingService {
	private static Logger logger = LoggerFactory.getLogger(BcmSavingService.class);

	private static CYDMDemo cydmDemo = new CYDMDemo();

	public static Map<String, Object> BcmLogins(HttpServletRequest request,String UserName,
			String UserPwd, String UUID,String userCard) {//TODO throws Exception
		int flag = 0;
		Map<String, Object> status = new HashMap<String, Object>();
		PushSocket.push(status, UUID, "1000","交通储蓄卡登录中");	
		PushState.state(userCard, "savings", 100);
		flag = 1;
		WebDriver driver = null;
		JavascriptExecutor js = null;
		System.out.println(js);//System.out.println(js)
		try {
			
			logger.warn("########【农业储蓄卡########登陆开始】########【用户名：】"
					+ UserName + "【密码：】" + UserPwd+"【身份证号：】"+userCard);	
			/* 打开此网页 */
			driver = DriverUtil.getDriverInstance("ie");
			driver.manage().window().maximize();
			
			driver.get("https://pbank.95559.com.cn/personbank/logon.jsp#");
			
			/* 获得账号输入框 */
			WebElement username = driver.findElement(By.id("alias"));
		
			WebElement login = driver.findElement(By.id("login"));

			/* 输入账号 */
			username.sendKeys(UserName);
			Thread.sleep(2000);
			
			SendKeys.sendStr(1139+80, 338-15, UserPwd);
//			SendKeys.sendStr(1139+80, 338+35, UserPwd);
			Thread.sleep(1000);
			WebElement element = driver.findElement(By
					.className("captchas-img-bg"));
			/* 判断是否有验证码 */
			if (element.getAttribute("src") != null) {
				WebElement input_captcha = driver.findElement(By
						.id("input_captcha"));
				logger.warn("########【交通储蓄卡开始打码】########【身份证号：】"+userCard);
				String imgtext = downloadImgs(driver, element);
				logger.warn("########【交通储蓄卡开始打码  imgtext=】"+imgtext+"】########【身份证号：】"+userCard);
				input_captcha.sendKeys(imgtext);
			}
			
			login.click();			
			Thread.sleep(3000);
			//调出httpwatch
			HttpWatchUtil.openHttpWatch();
			/* //此处判断是否登陆成功 */
			boolean flgs = ElementExist(driver, By.className("lanse-12-b")); /* 错误表示 */
			boolean flgb = ElementExist(driver, By.id("captchaErrMsg")); /* JS错误提示 */
			boolean template = ElementExist(driver, By.className("template")); 
			if(driver.getPageSource().contains("您未注册或登录密码输入错误")) {
				logger.warn("########【交通储蓄卡登陆失败 原因=】您未注册或登录密码输入错误  #####【身份证号：】"+userCard);
				logger.warn("-----------交通储蓄卡-----------登陆失败--------------账号密码错误----------身份证号："+userCard);
				status.put( "errorInfo", "您未注册或登录密码输入错误" );
				status.put( "errorCode", "0001" );
				PushSocket.push(status, UUID, "3000","您未注册或登录密码输入错误");
				PushState.state(userCard, "savings", 200,"您未注册或登录密码输入错误");
				DriverUtil.close(driver);
				logger.warn("----交通储蓄卡------errorCode："+status.get("errorCode")+"-----errorInfo："+status.get("errorInfo"));
				return status;
			}
			if (flgb == true
					&& !driver.findElement(By.id("captchaErrMsg")).getText()
							.equals("")) {
				logger.warn("-----------交通储蓄卡-----------打码错误----------");
				driver.quit();
				status = BcmSavingService.BcmLogins(request,UserName, UserPwd, UUID,userCard);
			} else if (flgs == true) {
				logger.warn("########【交通储蓄卡登陆失败 原因=】账号密码错误，登录失败  #####【身份证号：】"+userCard);
				status.put( "errorInfo", "账号密码错误" );
				status.put( "errorCode", "0001" );
				PushSocket.push(status, UUID, "3000","账号密码错误，登录失败");
				PushState.state(userCard, "savings", 200,"账号密码错误，登录失败");
				DriverUtil.close(driver);
				logger.warn("----交通储蓄卡------errorCode："+status.get("errorCode")+"-----errorInfo："+status.get("errorInfo"));
				return status;
			} else {
				logger.warn("-----------交通储蓄卡-----------登陆失败----------帐号认证异常------------身份证号："+userCard);
				if(driver.getPageSource().contains("为了进一步保障您使用我行个人网银的安全性")) {
					//发送短信验证码
					logger.warn("########【交通储蓄卡登陆失败 原因=】为了进一步保障您使用我行个人网银的安全性，请你先尝试在官网登录！【身份证号：】"+userCard);
					PushSocket.push(status, UUID, "3000","帐号认证异常，请你先尝试在官网登录！");
					
					request.getSession().setAttribute("BcmCodePage", driver);
					status.put( "errorInfo", "帐号认证异常，请你先尝试在官网登录！" );
					status.put( "errorCode", "0011" );
					DriverUtil.close(driver);///TODO 	代码重复
					return status;
				}else if(template){
					Map<String, Object> params = new HashMap<String, Object>();
									
					Thread.sleep(3000);
					
					Map<String, String> headers = new HashMap<String, String>();
					PushSocket.push(status, UUID, "2000","交通储蓄卡登陆成功");
					logger.warn("########【交通储蓄卡登陆成功】##########【身份证号：】"+userCard);
					Thread.sleep(1000);
					logger.warn("-----------交通储蓄卡-----------登陆成功----------身份证号："+userCard);
					PushSocket.push(status, UUID, "5000","交通储蓄卡数据获取中");
					logger.warn("########【交通储蓄卡数据获取中】##########【身份证号：】"+userCard);
					flag = 2;
						boolean flg = ElementExist(driver, By.id("btnConf1"));
						/* 判断是否有登陆确认信息 */
						if (flg == true) {
							WebElement btnConf1 = driver.findElement(By.id("btnConf1"));
							btnConf1.click();
							Thread.sleep(3000);
						}
						Thread.sleep(2000);
						
						logger.warn("-----------交通储蓄卡-----------获取数据明细----------身份证号："+userCard);
						List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();
						list1 = getInfo(driver,list1,UserName, request);
						logger.warn("########【交通储蓄卡数据获取完成】#######【身份证号：】"+userCard+"########【list1：】"+list1);
						if(list1.toString().contains("数据为空")) {							
							logger.warn("---------------无明细信息 已推送app状态");
							PushSocket.push(status, UUID, "7000","此卡无银行流水信息");
							PushState.state(userCard, "savings", 200,"此卡无银行流水信息");				
							status.put("errorCode", "0002");
							status.put("errorInfo", "此卡无银行流水信息");
							DriverUtil.close(driver);
							logger.warn("----交通储蓄卡------errorCode："+status.get("errorCode")+"-----errorInfo："+status.get("errorInfo"));
							return status;
						}

						
						
						
//						 //执行JS去点击账单查询 
//						
//						js = (JavascriptExecutor) driver;
//						System.out.println(js);
//						String zd = "Util.changeMenu('P001000');";
//						js.executeScript(zd, "");
////						
//					driver.switchTo().frame("frameMain");
//					driver.switchTo().frame("tranArea");
//					//执行JS去点击账单查询 
//					
//					Thread.sleep(2000);
////						/* 切入ifrmae */
////						driver.switchTo().frame("frameMain");
////						driver.switchTo().frame("tranArea");
//					
//					
//					List<Object> list =null;
//						List<Map<String, Object>> lists = yuefen();
//						
//						
//						/* //点击明细 */
//						WebElement mx = driver.findElement(By.linkText("明细"));
//						mx.click();
//						js.executeScript(
//								"$('#startDate_show').val('"
//										+ lists.get(0).get("begin").toString()
//										+ "');$('#endDate_show').val('"
//										+ lists.get(5).get("end").toString()
//										+ "');$('#btnQry2').click()", "");
//						/* 开始解析 */
//						Document docs = Jsoup.parse(driver.getPageSource());
//						Elements trs = docs.getElementsByClass("form-table");
//						Elements tr = trs.select("tr");
//						list = new ArrayList<Object>();
//						for (int i = 1; i < tr.size(); i++) {
//							Map<String, Object> map = new HashMap<String, Object>();
//
//							Elements td = tr.get(i).select("td");
//							for (int j = 0; j < td.size(); j++) {
//								if (j == 0) {
//									/* 交易时间 */
//									if (td.get(j).text().equals("查询")) {
//									} else if (td.get(j).text().contains("保存文件格式")) {
//									} else {
//										map.put("dealTime", td.get(j).text());
//									}
//								}
//								if (j == 1) {
//									/* 交易方式 */
//									map.put("dealReferral", td.get(j).text()); /* 业务摘要 */
//								}
//								if (j == 2) {
//									/* 交易币种 */
//									map.put("currency", td.get(j).text()); /* 业务摘要 */
//								}
//								if (j == 3) {
//									map.put("dealAmount", td.get(j).text()); /* 交易金额 */
//									/* 支出金额 */
//								}
//								if (j == 4) {
//									/* 收入金额 */
//								}
//								if (j == 5) {
//									/* 收入余额 */
//									map.put("balanceAmount", td.get(j).text()); /* 余额 */
//								}
//
//								if (j == 6) {
//									/* 交易地点 */
//									map.put("dealDitch", td.get(j).text()); /* 交易渠道 */
//								}
//							}
//							map.put("oppositeSideName", "");
//							map.put("oppositeSideNumber", "");
//							map.put("currency", "");
//							list.add(map);
//						}
						logger.warn("-----------交通储蓄卡-----------数据存储----------身份证号："+userCard);
						params.clear();
						headers.clear();
						headers.put("accountType", "");
						headers.put("openBranch", "");
						headers.put("openTime", "");

						params.put("billMes", list1);
						params.put("baseMes", headers);
						params.put("bankName", "中国交通银行");
						params.put("IDNumber", userCard);
						params.put("cardNumber", UserName);
						params.put("userName", "");
						PushSocket.push(status, UUID, "6000","交通银行储蓄卡数据获取成功");
						logger.warn("########【交通储蓄卡数据获取成功】##########【身份证号：】"+userCard);
						flag = 3;
						logger.warn("########【交通储蓄卡数据开始推送】##########【身份证号：】"+userCard);
						status = new Resttemplate().SendMessage(params, application.sendip+"/HSDC/savings/authentication");
						logger.warn("########【交通储蓄卡推送完成    身份证号：】"+userCard+"数据中心返回结果："+status.toString());
						
					    if(status!= null && "0000".equals(status.get("errorCode").toString())){
				           	PushState.state(userCard, "savings", 300);
				           	PushSocket.push(status, UUID, "8000","认证成功");
				           	status.put("errorInfo","推送成功");
				           	status.put("errorCode","0000");
			           }else{
				           	PushState.state(userCard, "savings", 200,status.get("errorInfo").toString());
				           	PushSocket.push(status, UUID, "9000",status.get("errorInfo").toString());
				           	status.put("errorCode",status.get("errorCode"));//异常处理
				           	status.put("errorInfo",status.get("errorInfo"));
			           }
				}else {
					logger.warn("########【交通储蓄卡登陆失败：账号密码错误】##########【身份证号：】"+userCard);
					status.put("errorCode", "0001");// 异常处理
					status.put("errorInfo", "账号密码错误，登录失败");
					PushSocket.push(status, UUID, "3000","账号密码错误，登录失败");
					PushState.state(userCard, "savings", 200,"账号密码错误，登录失败");
					DriverUtil.close(driver);
					logger.warn("----交通储蓄卡------errorCode："+status.get("errorCode")+"-----errorInfo："+status.get("errorInfo"));
					return status;
				}
				
			}
		}catch(Exception e) {
			
			if(flag == 1) {
				status.put("errorCode", "0001");
				logger.warn("--------------flag="+flag+"----------网络异常，登录失败");
				PushSocket.push(status, UUID, "3000","网络异常，登录失败");								
			}else if(flag == 2) {
				status.put("errorCode", "0002");
				logger.warn("--------------flag="+flag+"----------网络异常，数据获取异常");
				PushSocket.push(status, UUID, "7000","网络异常");					
			}else if(flag == 3) {
				status.put("errorCode", "0003");
				logger.warn("--------------flag="+flag+"----------网络异常，认证失败");
				PushSocket.push(status, UUID, "9000","网络异常");						
			}
			
			PushState.state(userCard, "savings", 200,"网络异常");
			logger.warn("########【交通银行储蓄卡进入try-catch】【身份证号：】"+userCard);
			logger.warn("----交通储蓄卡------errorCode："+status.get("errorCode")+"-----errorInfo："+status.get("errorInfo"));
			status.put("errorInfo", "网络错误");
			DriverUtil.close(driver);
		}
		
		 
		logger.warn("-----------交通储蓄卡-----------查询结果----------返回结果："+status.toString());
		return status;
	}
	
	/**
	 * 发包获取明细
	 * @param driver
	 * @param list
	 * @param userCard
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String,Object>>	getInfo(WebDriver driver,List<Map<String,Object>> list,String userCard,HttpServletRequest request) throws Exception{
		String jsession = HttpWatchUtil.getCookie("JSESSIONID");
		Map<String, Object> params = null;
		Map<String, String> headers = new HashMap<String, String>(16);
		params = new HashMap<String, Object>(16);		
		String frameMain = driver.findElement(By.id("frameMain")).getAttribute("src");
		String pSessionId = frameMain.substring(frameMain.indexOf("=")+1, frameMain.indexOf("&"));
		logger.warn("--------------pSessionId-----------------"+pSessionId);
		logger.warn("--------------账单第二步  获取safeValue-----------------");

		headers.put("x-requested-with", "XMLHttpRequest");		
		headers.put("Accept-Language", "zh-CN");
		headers.put("Accept", "text/javascript;charset=utf-8");	
		headers.put("If-Modified-Since", "0");
		headers.put("Cache-Control", "no-cache,must-revalidate");		
		headers.put("Accept-Encoding", "gzip, deflate");
//		headers.put("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)");	
		headers.put("Host", "pbank.95559.com.cn");	
		headers.put("DNT", "1");	
		headers.put("Connection", "keep-alive");		
		headers.put("Cookie", jsession);		
		double random = Math.random();
		long time = new Date().getTime();
		headers.put("Referer", "https://pbank.95559.com.cn/personbank/app/pebs.do?PSessionId="+pSessionId+"&x-channel=0&menuCode=&appId=&startMenu=&ibpsProtocolReq=&pebsUrl=&args=");
		
		String response = SimpleHttpClient.get("https://pbank.95559.com.cn/personbank/system/syCardList.ajax?"
				+ "PSessionId="+pSessionId+"&x-channel=0&tab=0&menuCode=P001001&random="+random+"&_="+time, headers);
		Map<String, Object> pageHeader = (Map<String, Object>) JsonUtil.getJsonValue1(response, "RSP_BODY");		
		String safeValue = (String) pageHeader.get("safeValue");
		safeValue.replaceAll("%2", "&252");
		safeValue.replaceAll("%3", "&253");
		logger.warn("--------账单第二步成功------safeValue-----------------"+safeValue);		
		logger.warn("--------------账单开始-----------------");		
		headers.clear();
		headers.put("Accept-Language", "zh-CN");//
		headers.put("Accept", "text/html, application/xhtml+xml, */*");	//
		headers.put("Accept-Encoding", "gzip, deflate");//
//		headers.put("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)");	//
		headers.put("Host", "pbank.95559.com.cn");	//
		headers.put("DNT", "1");	//
		headers.put("Connection", "keep-alive");	//	
		headers.put("Cookie", jsession);	//
		headers.put("Referer", "https://pbank.95559.com.cn/personbank/app/pebs.do?PSessionId="+pSessionId+"&x-channel=0&menuCode=&appId=&startMenu=&ibpsProtocolReq=&pebsUrl=&args=");
		response = SimpleHttpClient.get("https://pbank.95559.com.cn/personbank/account/acQuery.do?"
				+ "PSessionId="+pSessionId+"&x-channel=0&tab=0&menuCode=P001001&random="+random+"&ReqSafeFields"+safeValue, headers);
		logger.warn("--------------账单获取结果-----------------response："+response);
		Document doc = Jsoup.parse(response);
        Element options = doc.getElementsByTag("td").get(16);
        Element a = options.getElementsByTag("a").get(0);        
		String str = a.attr("onclick");
		logger.warn("--------------str-----------------"+str);
		String cardNo = str.substring(str.indexOf("('")+2, str.indexOf("','"));
		
		logger.warn("--------------明细-----------------");

		headers.clear();
		driver.quit();
		headers.put("Accept-Language", "zh-CN");
		headers.put("Accept", "text/html, application/xhtml+xml, */*");	
		headers.put("Accept-Encoding", "gzip, deflate");
//		headers.put("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)");	
		headers.put("Host", "pbank.95559.com.cn");	
		headers.put("DNT", "1");	
		headers.put("Connection", "keep-alive");	
		headers.put("Cookie", jsession);	
		headers.put("Referer", "https://pbank.95559.com.cn/personbank/app/pebs.do?PSessionId="+pSessionId+"&x-channel=0&menuCode=&appId=&startMenu=&ibpsProtocolReq=&pebsUrl=&args=");		
		response = SimpleHttpClient.get("https://pbank.95559.com.cn/personbank/account/acTranRecordQuery.do?"
				+ "PSessionId="+pSessionId+"&x-channel=0&cardNo="+cardNo+"&selectCardNo="+userCard+"&menuCode=P002000&random="+random+"&ReqSafeFields"+safeValue, headers);
		
		logger.warn("--------------明细-----------------response："+response);
		if(!response.contains("recordtbody")) {
	          Map<String,Object> map=new HashMap<String,Object>();
	          map.put("null", "数据为空");
	        	list.add(map);
	        	return list;
	        }
		
		Document info = Jsoup.parse(response);
        Element tbody = info.getElementById("recordtbody");
        
        Elements tr = tbody.getElementsByTag("tr");
        List<Map<String, Object>> list1 = new ArrayList<Map<String, Object>>();
        list = getInfo(tr,list1); 		
		return list;
		
		
	}
	public static List<Map<String, Object>> getInfo(Elements tr,List<Map<String, Object>> infos){
		for (Element list : tr) {
			Map<String, Object> datas=new HashMap<String, Object>();
			datas.put("dealTime", list.getElementsByTag("td").get(0).text());
			
			datas.put("incomeMoney", list.getElementsByTag("td").get(4).text());//收入金额
			datas.put("expendMoney ", list.getElementsByTag("td").get(3).text());//支出金额
			//余额
			datas.put("balanceAmount", list.getElementsByTag("td").get(5).text());
			// 交易渠道 
			datas.put("dealDitch", "");						
			// 交易摘要 
			datas.put("dealReferral", "");
			datas.put("oppositeSideName", "");
			datas.put("oppositeSideNumber", "");
			datas.put("currency", "");
			infos.add(datas);
		}
		return infos;		
	}
	
	
	/*
	 * 交通储蓄卡发送短信验证码
	 
	
	public static Map<String, Object> BCMSendCode(HttpServletRequest request){
		WebDriver driver = (WebDriver) request.getSession().getAttribute("BcmCodePage");
		Map<String, Object> status = new HashMap<String, Object>();
		if(driver==null) {
			status.put("errorCode", "0002");
			status.put("errorInfo", "网络异常，登录失败！");
		}else {			   	
			//driver.get("https://pbank.95559.com.cn/personbank/system/syVerifyCustomerNewControl.do");
			driver.findElement(By.id("authSMSSendBtn")).click();			
			request.getSession().setAttribute("jiaotongdriver", driver);
		}
		return status;
	}
	*//**
	 * 交通储蓄卡有验证码情况下查询
	 * @param request
	 * @param UUID
	 * @param userCard
	 * @param Sendcode
	 * @param UserName
	 * @return
	 * @throws InterruptedException
	 *//*
	public static Map<String, Object> BCMQueryInfo(HttpServletRequest request,String UUID,String userCard,String Sendcode,String UserName) throws InterruptedException{
		WebDriver driver = (WebDriver) request.getAttribute("jiaotongdriver");
		Map<String, Object> status = new HashMap<String, Object>();
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, String> headers = new HashMap<String, String>();
		Thread.sleep(1000);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		if(driver==null) {
			PushSocket.push(status, UUID, "3000","网络异常,登录失败");
			 PushState.state(userCard, "savings", 200);
			status.put("errorCode", "0002");// 异常处理
			status.put("errorInfo", "网络异常，登录失败！");
		}else {
			try {
				Alert alt = driver.switchTo().alert();//监控弹框
	    		String errorInfo = alt.getText();
				driver.findElement(By.id("mobileCode")).sendKeys(Sendcode);
				driver.findElement(By.id("btnConf2")).click();
            	logger.warn(errorInfo);
            	PushSocket.push(status, UUID, "3000",errorInfo);
            	PushState.state(userCard, "savings", 200);
            	status.put("errorCode", "0011");
            	status.put("errorInfo", errorInfo);
            	DriverUtil.close(driver);
            	return status;
			}catch (org.openqa.selenium.NoAlertPresentException e) {//没有弹窗时判断是否登陆成功
			
            	if(driver.getPageSource().contains("您可以将当前电脑设置为常用电脑")) {
            		driver.findElement(By.id("next")).click();
            		Thread.sleep(1000);
        			PushSocket.push(status, UUID, "2000","交通储蓄卡登陆成功");
        			logger.warn("-----------交通储蓄卡-----------登陆成功----------身份证号："+userCard);
        			
        			boolean flg = ElementExist(driver, By.id("btnConf1"));
        			 判断是否有登陆确认信息 
        			if (flg == true) {
        				WebElement btnConf1 = driver.findElement(By.id("btnConf1"));
        				btnConf1.click();
        			}

        			 执行JS去点击账单查询 
        			String zd = "Util.changeMenu('P001000');";

        			js.executeScript(zd, "");
//        			driver.switchTo().frame("frameMain");
//        			driver.switchTo().frame("tranArea");
//        			WebElement mx1 = driver.findElement(By.linkText("账户查询"));
//        			Thread.sleep(2000);
        			 切入ifrmae 
        			driver.switchTo().frame("frameMain");
        			driver.switchTo().frame("tranArea");
        			PushSocket.push(status, UUID, "5000","交通储蓄卡数据获取中");
        			List<Object> list =null;
        			try {
        				List<Map<String, Object>> lists = yuefen();
        				 //点击明细 
        				WebElement mx = driver.findElement(By.linkText("明细"));
        				mx.click();
        				js.executeScript(
        						"$('#startDate_show').val('"
        								+ lists.get(0).get("begin").toString()
        								+ "');$('#endDate_show').val('"
        								+ lists.get(5).get("end").toString()
        								+ "');$('#btnQry2').click()", "");
        				 开始解析 
        				Document docs = Jsoup.parse(driver.getPageSource());
        				Elements trs = docs.getElementsByClass("form-table");
        				Elements tr = trs.select("tr");
        				list = new ArrayList<Object>();
        				for (int i = 1; i < tr.size(); i++) {
        					Map<String, Object> map = new HashMap<String, Object>();

        					Elements td = tr.get(i).select("td");
        					for (int j = 0; j < td.size(); j++) {
        						if (j == 0) {
        							 交易时间 
        							if (td.get(j).text().equals("查询")) {
        							} else if (td.get(j).text().contains("保存文件格式")) {
        							} else {
        								map.put("dealTime", td.get(j).text());
        							}
        						}
        						if (j == 1) {
        							 交易方式 
        							map.put("dealReferral", td.get(j).text());  业务摘要 
        						}
        						if (j == 2) {
        							 交易币种 
        							map.put("currency", td.get(j).text());  业务摘要 
        						}
        						if (j == 3) {
        							map.put("dealAmount", td.get(j).text());  交易金额 
        							 支出金额 
        						}
        						if (j == 4) {
        							 收入金额 
        						}
        						if (j == 5) {
        							 收入余额 
        							map.put("balanceAmount", td.get(j).text());  余额 
        						}

        						if (j == 6) {
        							 交易地点 
        							map.put("dealDitch", td.get(j).text());  交易渠道 
        						}
        					}
        					map.put("oppositeSideName", "");
        					map.put("oppositeSideNumber", "");
        					map.put("currency", "");
        					list.add(map);
        				}
        				params.clear();
            			headers.clear();
            			headers.put("accountType", "");
            			headers.put("openBranch", "");
            			headers.put("openTime", "");

            			params.put("billMes", list);
            			params.put("baseMes", headers);
            			params.put("bankName", "中国交通银行");
            			params.put("IDNumber", userCard);
            			params.put("cardNumber", UserName);
            			params.put("userName", "");
            			PushSocket.push(status, UUID, "6000","交通银行储蓄卡数据获取成功");
            			status = new Resttemplate().SendMessage(params, application.sendip+"/HSDC/savings/authentication");  //推送数据
            		    if(status!= null && "0000".equals(status.get("errorCode").toString())){
            	           	PushState.state(userCard, "savings", 300);
            	           	PushSocket.push(status, UUID, "8000","认证成功");
            	           	status.put("errorInfo","推送成功");
            	           	status.put("errorCode","0000");
                       }else{
            	           	PushState.state(userCard, "savings", 200);
            	           	PushSocket.push(status, UUID, "9000",status.get("errorInfo").toString());
            	           	status.put("errorCode",status.get("errorCode"));//异常处理
            	           	status.put("errorInfo",status.get("errorInfo"));
                       }
        			}catch (Exception a) {
        				logger.warn("-----------交通银行查询失败-------------", a);
        				
        				status.put("errorCode", "0002");// 异常处理
        				status.put("errorInfo", "网络异常，请重试！");
        				PushSocket.push(status, UUID, "9000","网络异常,认证失败");
        				PushState.state(userCard, "savings", 200);
        				
        			} finally {
        				DriverUtil.close(driver);
        			}
        			
        			
            		
            	}else {
            		PushSocket.push(status, UUID, "3000","网络异常,登录失败");
        			status.put("errorCode", "0002");// 异常处理
        			status.put("errorInfo", "网络异常，登录失败！");
        			PushState.state(userCard, "savings", 200);
        			DriverUtil.close(driver);
            	}
			}
            
		
			
		}
		return params;
	}*/
	
	public static List<Map<String, Object>> yuefen() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		for (int i = -5; i < 1; i++) {
			SimpleDateFormat matter = new SimpleDateFormat("yyyyMM");
			SimpleDateFormat mattery = new SimpleDateFormat("yyyy");
			SimpleDateFormat matterm = new SimpleDateFormat("MM");

			Calendar calendar = Calendar.getInstance();
			/* 将calendar装换为Date类型 */
			Date date = calendar.getTime();
			/* 将date类型转换为BigDecimal类型（该类型对应oracle中的number类型） */
			BigDecimal time01 = new BigDecimal(matter.format(date));
			/* 获取当前时间的前6个月 */
			calendar.add(Calendar.MONTH, i);
			Date date02 = calendar.getTime();
			BigDecimal time02 = new BigDecimal(matter.format(date02));
			String s1 = mattery.format(date02);
			String s2 = matterm.format(date02);
			String end = getLastDayOfMonth(Integer.valueOf(s1),
					Integer.valueOf(s2));
			Map map = new HashMap();
			map.put("begin", s1 + "-" + s2 + "-01");
			map.put("end", end);
			list.add(map);
		}
		return (list);
	}

	public static boolean ElementExist(WebDriver driver, By locator) {
		try {
			driver.findElement(locator);
			return (true);
		} catch (NoSuchElementException e) {
			return (false);
		}
	}

	public static String downloadImgs(WebDriver driver, WebElement keyWord)
			throws IOException {
		String src = keyWord.getAttribute("src");
		String filename = new CrawlerUtil().getUUID();
		BufferedImage inputbig = createElementImages(driver, keyWord);
		ImageIO.write(inputbig, "png", new File("C://" + filename + ".png"));
		String codenice = cydmDemo.getcode(filename, 1006); /* 识别yanzhengma */
		return (codenice);
	}

	public static String downloadImgs(WebDriver driver, WebElement keyWord,
			int x, int y) throws IOException {
		String src = keyWord.getAttribute("src");
		String filename = new CrawlerUtil().getUUID();
		BufferedImage inputbig = createElementImages(driver, keyWord, x, y);
		ImageIO.write(inputbig, "png", new File("C://" + filename + ".png"));
		String codenice = cydmDemo.getcode(filename, 1006); /* 识别yanzhengma */
		return (codenice);
	}

	public static String getLastDayOfMonth(int year, int month) {
		Calendar cal = Calendar.getInstance();
		/* 设置年份 */
		cal.set(Calendar.YEAR, year);
		/* 设置月份 */
		cal.set(Calendar.MONTH, month - 1);
		/* 获取某月最大天数 */
		int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		/* 设置日历中月份的最大天数 */
		cal.set(Calendar.DAY_OF_MONTH, lastDay);
		/* 格式化日期 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String lastDayOfMonth = sdf.format(cal.getTime());

		return (lastDayOfMonth);
	}

	public static BufferedImage createElementImages(WebDriver driver,
			WebElement webElement) throws IOException {
		/* 获得webElement的位置和大小。 */
		Point location = webElement.getLocation();
		Dimension size = webElement.getSize();
		/* 创建全屏截图。 */
		BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(
				takeScreenshot(driver)));
		/* 截取webElement所在位置的子图。 */
		BufferedImage croppedImage = originalImage.getSubimage(location.getX(),
				location.getY(), size.getWidth(), size.getHeight());
		return (croppedImage);
	}

	public static BufferedImage createElementImages(WebDriver driver,
			WebElement webElement, int x, int y) throws IOException {
		/* 获得webElement的位置和大小。 */
		Point location = webElement.getLocation();
		Dimension size = webElement.getSize();
		/* 创建全屏截图。 */
		BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(
				takeScreenshot(driver)));
		/* 截取webElement所在位置的子图。 */
		BufferedImage croppedImage = originalImage.getSubimage(location.getX()
				- x, location.getY(), size.getWidth(), size.getHeight());
		return (croppedImage);
	}

	public static byte[] takeScreenshot(WebDriver driver) throws IOException {
		WebDriver augmentedDriver = new Augmenter().augment(driver);
		return (((TakesScreenshot) augmentedDriver)
				.getScreenshotAs(OutputType.BYTES));
	}
}
