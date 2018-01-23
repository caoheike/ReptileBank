package com.reptile.service;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoomsun.keyBoard.HttpWatchUtil;
import com.hoomsun.keyBoard.SendKeys;
import com.reptile.Bank.BcmLogin;
import com.reptile.util.Dates;
import com.reptile.util.DriverUtil;
import com.reptile.util.JsonUtil;
import com.reptile.util.PushSocket;
import com.reptile.util.PushState;
import com.reptile.util.Resttemplate;
import com.reptile.util.SimpleHttpClient;
import com.reptile.util.application;
@SuppressWarnings("deprecation")

/**
 * @author Bigyoung
 * @version v1.0
 * @deprecated 农业银行
 * @date 2017年11月16日10:51:14
 */
public class AbcSavingService {
	
		private static Logger logger= LoggerFactory.getLogger(AbcSavingService.class);

		/**
		 * 获取农业银行详情信息
		 * @param username
		 * @param userpwd
		 * @param UUID
		 * @param card
		 * @return
		 * @throws InterruptedException 
		 * @throws Exception
		 */
		public static Map<String, Object> doGetDetail(String username,
				String userpwd, String UUID, String card,HttpSession session,HttpServletRequest request) throws InterruptedException{
			int numCount=0;//打码次数
			logger.warn("-----------农业储蓄卡-----------登陆开始----------身份证号："+card);
			Map<String, Object> status = new HashMap<String, Object>();
			
			WebDriver driver = null;
			
				Map<String, Object> params = new HashMap<String, Object>();
				Map<String, String> headers = new HashMap<String, String>();
				try{
				 //打开此网页 
				driver = DriverUtil.getDriverInstance("ie");				
				driver.manage().window().maximize();				
				Thread.sleep(1000);
				driver.get("https://perbank.abchina.com/EbankSite/startup.do");
				 //判断是否加载页面 
				DriverUtil.waitByTitle(driver.getTitle(), driver, 10);
				// 键入账号 
				WebElement element = driver.findElement(By.id("username"));
				element.sendKeys(username);
				Thread.sleep(1500);
				// 特殊字符处理,输入密码  
//				SendKeys.sendStr(1143+80, 378, userpwd);
				SendKeys.sendStr(1143+80, 378+35, userpwd);//本地
				 //输入验证码 
				Thread.sleep(1000);
				WebElement elements = driver.findElement(By.id("vCode"));
				WebElement code = driver.findElement(By.id("code"));
				String imgtext = BcmLogin.downloadImgs(driver, elements, 10, 10);
				code.sendKeys(imgtext);
				//调出httpwatch
				HttpWatchUtil.openHttpWatch();
				 //登陆 
				WebElement logo = driver.findElement(By.id("logo"));				
				logo.click(); 
				
				Thread.sleep(2000);				

				if(DriverUtil.visibilityById("powerpass_ie_dyn_Msg", driver, 2) || DriverUtil.visibilityById("username-error", driver, 0) || (DriverUtil.waitByClassName("logon-error", driver, 1)&&!driver.findElement(By.className("logon-error")).getAttribute("title").equals(""))){					
					String text = "";
					if(DriverUtil.visibilityById("username-error", driver, 2)){
						text = driver.findElement(By.id("username-error")).getText();
					}else if(DriverUtil.visibilityById("powerpass_ie_dyn_Msg", driver, 2)){
						text = driver.findElement(By.id("powerpass_ie_dyn_Msg")).getText();
						//密码不为空并且报密码为空错误试递归
						if(text.contains("密码内容不能为空")&&!"".equals(userpwd)) {
							driver.quit();
							doGetDetail(username,userpwd, UUID, card,session,request);
						}						
					}else{
						text = driver.findElement(By.className("logon-error")).getAttribute("title");
					}
					status.put("errorCode", "0001");// 异常处理	
					status.put("errorInfo", text);
					driver.quit();
				}else if(DriverUtil.waitByTitle("中国农业银行个人网银首页", driver, 10)){
					System.out.println("不需要短信验证*************");
					params.put("Verify", "no");
					status.put("errorCode", "0000");
					status.put("errorInfo", "成功");
					status.put("data", params);                    
					String jsession = HttpWatchUtil.getCookie("ASP.NET_SessionId");
					StringBuffer getCookie = GetCookie(driver);	
					
					String cookie = getCookie.toString().replaceAll("path=/;", "")+jsession;
					
					logger.warn("-------------农业银行储蓄卡----------cookie："+cookie);
					String jiaotongUuid = String.valueOf(Math.random());
					logger.warn("-------------农业银行储蓄卡----------jiaotongUuid："+jiaotongUuid);
					session.setAttribute("jiaotong-uuid",jiaotongUuid);
					session.setAttribute(jiaotongUuid,cookie);
					
				}else if(DriverUtil.waitByTitle("个人网上银行-用户名登录-短信校验", driver, 1)) {
					System.out.println("需要短信验证*************");
					logger.warn("-------------需要短信验证----------");
					String securityPhone = driver.findElement(By.id("securityPhone")).getAttribute("value");
					logger.warn("-------------短信验证码发送至手机号码----------securityPhone："+securityPhone);
					String jsession = HttpWatchUtil.getCookie("ASP.NET_SessionId");
//					StringBuffer getCookie = GetCookie(driver);						
//					String cookie = getCookie.toString().replaceAll("path=/;", "")+jsession;

					headers.put("Referer", "https://perbank.abchina.com/EbankSite/upLogin.do");
					headers.put("Host", "perbank.abchina.com");
					headers.put("Cookie", jsession);
					params.put("isValidMac", 1);
					params.put("mobile",securityPhone);
					params.put("mobileNoField", "securityPhone");
					params.put("sendType", 17);
			    	
					// 发送短信验证码
					String response = SimpleHttpClient.post("https://perbank.abchina.com/EbankSite/SendSmsVerifyCodeAct.ebf",params, headers);

					try {
						Alert alt = driver.switchTo().alert();
						alt.accept();
					} catch (Exception e) {
						// TODO: handle exception
					}
					params.clear();
					headers.clear();
					logger.warn("-------------农业银行储蓄卡----------cookie："+jsession);
					String jiaotongUuid = String.valueOf(Math.random());
					logger.warn("-------------农业银行储蓄卡----------jiaotongUuid："+jiaotongUuid);
					session.setAttribute("jiaotong-uuid",jiaotongUuid);
					session.setAttribute(jiaotongUuid,jsession);
					if("".equals(response)) {
						status.put("errorCode", "0000");
						status.put("errorInfo", "成功");
					}else {
						status.put("errorCode", "0001");
						status.put("errorInfo", "验证码发送失败");						
					}										
					
					params.put("Verify", "yes");					
					status.put("data", params);
					String formId = driver.findElement(By.name("abc_formId")).getAttribute("value");
					String jiaotongformId = String.valueOf(Math.random());
					session.setAttribute("jiaotong-formId",jiaotongformId);
					session.setAttribute(jiaotongformId,formId);
					
				}else{
					if(DriverUtil.waitByTitle("中国农业银行个人网银登录入口", driver, 1)) {
						numCount=numCount+1;
						driver.quit();
						if(numCount>5) {
	    		           	status.put("errorCode","0001");//异常处理
	    		           	status.put("errorInfo","网页异常，登录失败");
	    		           	driver.quit();
							return status;
						}
						status = doGetDetail(username, userpwd, UUID, card,session,request);
					}else if(DriverUtil.waitByTitle("个人网上银行-重置登录密码", driver, 1)) {
						status.put("errorCode","0001");//异常处理
    		           	status.put("errorInfo","您的密码过于简单，请登录官网重置密码！");
					}
					driver.quit();
				}
				}catch(Exception e){
					logger.warn("-------------农业银行储蓄卡------------登录失败-------------", e);
					status.put("errorCode", "0002");// 异常处理
					status.put("errorInfo", "网络异常，请重试！");
					driver.quit();C:
					return status;
				}
				session.setAttribute("ABCdriver",driver);
				setDriver(request,driver);
				return status;					
			}
	
		public static Map<String, Object> abcQueryInfo(String code, String idCard,
				HttpSession session, String UUID,String numbe,HttpServletRequest request) throws InterruptedException{
			logger.warn("------农业银行储蓄卡----------idCard："+idCard+"----------numbe=:"+numbe+"----------------");
			Map<String, Object> status = new HashMap<String, Object>();
			Map<String, String> headers = new HashMap<String, String>();
			int flag = 0;
			
			PushSocket.push(status, UUID, "1000","农业储蓄卡登录中");
			PushState.state(idCard, "savings", 100);
			flag = 1;
			Map<String, Object> params = new HashMap<String, Object>();
			WebDriver driver = (WebDriver) session.getAttribute("ABCdriver");
			if(!code.equals("0")) {
				try {
					String jiaotongformId = (String) session.getAttribute("jiaotong-formId");
					logger.warn("---------------------数据获取中jiaotongformId:"+jiaotongformId);
					String formId = (String) session.getAttribute(jiaotongformId);
					logger.warn("---------------------数据获取中formId:"+formId);
					
					String jiaotongUuid = (String) session.getAttribute("jiaotong-uuid");
					logger.warn("---------------------数据获取中jiaotongUuid:"+jiaotongUuid);
					String jsession = (String) session.getAttribute(jiaotongUuid);
					logger.warn("---------------------数据获取中jsession:"+jsession);
					
					
					headers.put("Referer", "https://perbank.abchina.com/EbankSite/upLogin.do");
					headers.put("Host", "perbank.abchina.com");
					headers.put("Cookie", jsession);
					headers.put("Content-Type", "application/x-www-form-urlencoded");
				
					params.put("abc_formId", formId);
					params.put("isValidMac",1);
					params.put("isReset", "");
					params.put("userName", "");
					params.put("verifycode", code);
					params.put("token","");
			    	
					// 登录请求
					String response = SimpleHttpClient.post("https://perbank.abchina.com/EbankSite/SelfHelpVerifySmsCodeAct.ebf",params, headers);

			        if(response.contains("验证码输入有误")) {	
			        	logger.warn("-------------农业银行储蓄卡------------错误页面-------------");
						status.put("errorCode", "0002");// 异常处理
						status.put("errorInfo", "短信验证码输入有误");
						PushSocket.push(status, UUID, "3000","短信验证码输入有误");
						PushState.state(idCard, "savings", 200,"短信验证码输入有误");
						driver.quit();
						return status;
			        }
					String cusname = "";						
					logger.warn("-----------农业储蓄卡-----------登陆成功----------身份证号："+idCard);
						// 登陆成功 
					
					PushSocket.push(status, UUID, "2000","农业储蓄卡登陆成功");// 开始执行推送登陆成功
					Thread.sleep(2000);
					PushSocket.push(status, UUID, "5000","农业储蓄卡数据获取中");
					flag = 2;
					
					List<Map<String, Object>> list1 = new ArrayList<Map<String, Object>>();
					list1 = getInfo(driver,list1,numbe,cusname,session);
							
						params.clear();
						headers.clear();
						headers.put("accountType", ""); //账号状态 
						headers.put("openBranch", "");// 开户网点 
						headers.put("openTime", ""); //开户日期 
						
						params.put("billMes", list1);
						params.put("baseMes", headers);
						params.put("bankName", "中国农业银行");
						params.put("IDNumber", idCard); //身份证 
						params.put("cardNumber", numbe); //用户卡号 
						params.put("userName", cusname); //用户姓名 
						PushSocket.push(status, UUID, "6000","农业银行储蓄卡数据获取成功");
						flag = 3;
//						Resttemplate resttemplate = new Resttemplate();
//						status = resttemplate.SendMessage(params, application.sendip+ "/HSDC/savings/authentication", card);
						status = new Resttemplate().SendMessage(params, application.sendip+"/HSDC/savings/authentication");  //推送数据
	    			    if(status!= null && "0000".equals(status.get("errorCode").toString())){
	    		           	 PushState.state(idCard, "savings", 300);
	    		           	PushSocket.push(status, UUID, "8000","认证成功");
	    		           	status.put("errorInfo","推送成功");
	    		           	status.put("errorCode","0000");
	    			    }else{
	    		           	PushState.state(idCard, "savings", 200,status.get("errorInfo").toString());
	    		           	PushSocket.push(status, UUID, "9000",status.get("errorInfo").toString());
	    		           	status.put("errorCode",status.get("errorCode"));//异常处理
	    		           	status.put("errorInfo",status.get("errorInfo"));
	    			    }

				 
				}catch(Exception e) {
					if(flag == 1) {
						logger.warn("--------------flag="+flag+"----------网络异常，登录失败");
						PushSocket.push(status, UUID, "3000","网络异常，登录失败");								
					}else if(flag == 2) {
						logger.warn("--------------flag="+flag+"----------网络异常，数据获取异常");
						PushSocket.push(status, UUID, "7000","网络异常");					
					}else if(flag == 3) {
						logger.warn("--------------flag="+flag+"----------网络异常，认证失败");
						PushSocket.push(status, UUID, "9000","网络异常");						
					}
					
					PushState.state(idCard, "savings", 200,"网络异常");
					
					status.put("errorCode", "0001");
					status.put("errorInfo", "网络错误");
					driver.quit();	
					
				}			
			}else {
				try {
					
				
				String cusname = "";						
				logger.warn("-----------农业储蓄卡-----------登陆成功----------身份证号："+idCard);
					// 登陆成功 
					
				PushSocket.push(status, UUID, "2000","农业储蓄卡登陆成功");// 开始执行推送登陆成功
				Thread.sleep(2000);
//				driver.switchTo().frame("contentFrame");
				PushSocket.push(status, UUID, "5000","农业储蓄卡数据获取中");
				flag = 2;
//				// 拿到姓名 
//				WebElement custName = driver
//						.findElement(By.id("show-custName"));
//				cusname = custName.getText();
	
				List<Map<String, Object>> list1 = new ArrayList<Map<String, Object>>();
				list1 = getInfo(driver,list1,numbe,cusname,session);
						
					params.clear();
					headers.clear();
					headers.put("accountType", ""); //账号状态 
					headers.put("openBranch", "");// 开户网点 
					headers.put("openTime", ""); //开户日期 
					
					params.put("billMes", list1);
					params.put("baseMes", headers);
					params.put("bankName", "中国农业银行");
					params.put("IDNumber", idCard); //身份证 
					params.put("cardNumber", numbe); //用户卡号 
					params.put("userName", cusname); //用户姓名 
					PushSocket.push(status, UUID, "6000","农业银行储蓄卡数据获取成功");
					flag = 3;
//					Resttemplate resttemplate = new Resttemplate();
//					status = resttemplate.SendMessage(params, application.sendip+ "/HSDC/savings/authentication", card);
					status = new Resttemplate().SendMessage(params, application.sendip+"/HSDC/savings/authentication");  //推送数据
    			    if(status!= null && "0000".equals(status.get("errorCode").toString())){
    		           	 PushState.state(idCard, "savings", 300);
    		           	PushSocket.push(status, UUID, "8000","认证成功");
    		           	status.put("errorInfo","推送成功");
    		           	status.put("errorCode","0000");
    			    }else{
    		           	PushState.state(idCard, "savings", 200,status.get("errorInfo").toString());
    		           	PushSocket.push(status, UUID, "9000",status.get("errorInfo").toString());
    		           	status.put("errorCode",status.get("errorCode"));//异常处理
    		           	status.put("errorInfo",status.get("errorInfo"));
    			    }
			}catch(Exception e) {
				if(flag == 1) {
					logger.warn("--------------flag="+flag+"----------网络异常，登录失败");
					PushSocket.push(status, UUID, "3000","网络异常，登录失败");								
				}else if(flag == 2) {
					logger.warn("--------------flag="+flag+"----------网络异常，数据获取异常");
					PushSocket.push(status, UUID, "7000","网络异常");					
				}else if(flag == 3) {
					logger.warn("--------------flag="+flag+"----------网络异常，认证失败");
					PushSocket.push(status, UUID, "9000","网络异常");						
				}
				
				PushState.state(idCard, "savings", 200,"网络异常");
				
				status.put("errorCode", "0001");
				status.put("errorInfo", "网络错误");
				driver.quit();					
			}	
			}
			driver.quit();	
			closeDriver(request);
			return status;	
		}

/**
 * 
 * @param driver
 * @param list
 * @param userCard
 * @param cusname
 * @param session
 * @return
 * @throws Exception
 */
		public static List<Map<String, Object>>	getInfo(WebDriver driver,List<Map<String, Object>> list,String userCard,String cusname,HttpSession session) throws Exception{	
			
			String jiaotongUuid = (String) session.getAttribute("jiaotong-uuid");
			logger.warn("---------------------数据获取中jiaotongUuid:"+jiaotongUuid);
			String jsession = (String) session.getAttribute(jiaotongUuid);
			logger.warn("---------------------数据获取中jsession:"+jsession);

			System.out.println("---------jsession---------"+jsession );
			
			//农业参数
			Map<String,Object> params=new HashMap<String,Object>();
			String startTime = Dates.beforMonth(6);
			String endTime = Dates.currentTime();
			logger.warn("---------------------startTime:"+startTime+"------------------------endTime:"+endTime);
			params.put("trnStartDt", startTime);	
			params.put("trnEndDt", endTime);
			params.put("acctId", userCard);
			
			params.put("acctType", 401);	
			params.put("acctName", "fghdfgh");
			params.put("acctOpenBankId", 33017);
			
			params.put("provCode", 26);	
			params.put("busCode", 200002);
			params.put("oofeFlg", 0);
			params.put("acctCurCode", 156);

			Map<String,String> headers1=new HashMap<String,String>();
			headers1.put("Cookie",jsession);
			String response = SimpleHttpClient.post("https://perbank.abchina.com/EbankSite/AccountTradeDetailQueryAct.do", params, headers1);

			logger.warn("--------------六个月明细请求结果-----------------"+response);
			List<List<String>> pageHeader = new ArrayList<List<String>>();
			
			pageHeader = (List<List<String>>) JsonUtil.getJsonValue1(response, "table");
			logger.warn("--------------六个月明细请求后取出查询结果-----------------table="+pageHeader);
			
			String nextPageKey = (String) JsonUtil.getJsonValue1(response, "nextPageKey");
			logger.warn("--------------下一页标示-----------------nextPageKey="+nextPageKey);
			//解析
			List<Map<String, Object>> infos = new ArrayList<Map<String, Object>>();
			infos = getInfo(pageHeader,infos);
			
			while(!"".equals(nextPageKey)) {				
				params.put("nextPageKey", nextPageKey);				
				logger.warn("--------------写一页请求-----------------");
				response = SimpleHttpClient.post("https://perbank.abchina.com/EbankSite/AccountTradeDetailQueryAct.do",params, headers1);
				pageHeader = (List<List<String>>) JsonUtil.getJsonValue1(response, "table");
				//解析
				infos = getInfo(pageHeader,infos);
				nextPageKey = (String) JsonUtil.getJsonValue1(response, "nextPageKey");				
			}
			driver.quit();
			return infos;
			
			
		}
		/**
		 * 
		 * @param pageHeader
		 * @param infos
		 * @return
		 */
		public static List<Map<String, Object>> getInfo(List<List<String>> pageHeader,List<Map<String, Object>> infos){
			for (List<String> list : pageHeader) {
				Map<String, Object> datas=new HashMap<String, Object>();
				datas.put("dealTime", list.get(0));
				datas.put("incomeMoney", list.get(2));
				//余额
				datas.put("balanceAmount", list.get(3));
				// 交易渠道 
				datas.put("dealDitch", list.get(7));						
				// 交易摘要 
				datas.put("dealReferral", list.get(10));
				datas.put("oppositeSideName", "");
				datas.put("oppositeSideNumber", "");
				datas.put("currency", "");
				infos.add(datas);
			}
			return infos;
			
		}
		/**
		 * 
		 * @param driver
		 * @return
		 */
		public static StringBuffer GetCookie(WebDriver driver) {
			// 获得cookie用于发包
			Set<Cookie> cookies = driver.manage().getCookies();
			StringBuffer tmpcookies = new StringBuffer();

			for (Cookie cookie : cookies) {
				tmpcookies.append(cookie.toString() + ";");

			}
			return tmpcookies;

		}	
		 /**
	     * 将当前Driver信息存入request中
	     * @param request
	     * @param driver
	     */
	    public static void setDriver(HttpServletRequest request,WebDriver driver) {
	      Object obj = request.getServletContext().getAttribute("driver");
	      if(obj == null) {
	        obj = new HashMap<Object,Object>(16);
	      }
	      @SuppressWarnings("unchecked")
	      Map<Object,Object> map = (Map<Object, Object>) obj;
	      
	      map.put(driver, new Date());
	      request.getServletContext().setAttribute("driver", map);
	    }
	    
	    
	    /**
	     * 关闭Driver信息
	     * @param request
	     */
	    public static void closeDriver(HttpServletRequest request) {
	      Object obj = request.getServletContext().getAttribute("driver");
	      if(obj != null) {
	        @SuppressWarnings("unchecked")
	        Map<Object,Object> map = (Map<Object, Object>) obj;
	        Set<Object> set = map.keySet();
	        for(Object item : set) {
	          if(item != null) {
	            Date date = (Date)map.get(item);
	            long times = new Date().getTime() - date.getTime();
	            if(times > 300000) {
	              WebDriver driver = (WebDriver)item;
	              driver.quit();
	            }
	          }
	        }
	        
	      }
	    }
		
		
		
}