package com.reptile.service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
				String userpwd, String UUID, String card,HttpSession session) throws InterruptedException{
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
//				SendKeys.sendStr(1143+80, 378-20, userpwd);
				SendKeys.sendStr(1143+80, 378+35, userpwd);//本地
				 //输入验证码 
				Thread.sleep(1000);
				WebElement elements = driver.findElement(By.id("vCode"));
				WebElement code = driver.findElement(By.id("code"));
				String imgtext = BcmLogin.downloadImgs(driver, elements, 10, 10);
				code.sendKeys(imgtext);
				
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
							doGetDetail(username,userpwd, UUID, card,session);
						}						
					}else{
						text = driver.findElement(By.className("logon-error")).getAttribute("title");
					}
					status.put("errorCode", "0001");// 异常处理	
					status.put("errorInfo", text);
					driver.quit();
				}else if(DriverUtil.waitByTitle("中国农业银行个人网银首页", driver, 10)){
					session.setAttribute("ABCdriver", driver);
					System.out.println("不需要短信验证*************");
					params.put("Verify", "no");
					status.put("errorCode", "0000");
					status.put("errorInfo", "成功");
					status.put("data", params);
				}else if(DriverUtil.waitByTitle("个人网上银行-用户名登录-短信校验", driver, 1)) {
					WebElement sendSms = driver.findElement(By.id("dynamicPswText_sendSms"));					
					sendSms.click();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					String currentHandler = driver.getWindowHandle();
					try {
						Alert alt = driver.switchTo().alert();
						alt.accept();
					} catch (Exception e) {
						// TODO: handle exception
					}
//					driver.switchTo().window(currentHandler);
					session.setAttribute("ABCdriver", driver);
					System.out.println("需要短信验证*************");
					params.put("Verify", "yes");
					status.put("errorCode", "0000");
					status.put("errorInfo", "成功");
					status.put("data", params);
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
						status = doGetDetail(username, userpwd, UUID, card,session);
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
					driver.quit();
					return status;
				}
				
				return status;					
			}
	
		public static Map<String, Object> abcQueryInfo(String code, String idCard,
				HttpSession session, String UUID,String numbe) throws InterruptedException{
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
					System.out.println(driver.getPageSource());
					logger.info(driver.getPageSource());
	//				driver.switchTo().window(driver.getWindowHandle());
					WebElement dynamicPswText = driver.findElement(By.id("dynamicPswText"));
					dynamicPswText.sendKeys(code);
					driver.findElement(By.id("orangeBtn")).click();
					Thread.sleep(2000);
					String login = driver.getPageSource();
					Document  infotable=  Jsoup.parse(login);  
			        Elements tags= infotable.getElementsByTag("title");
			        String title = tags.get(0).text();
			        if(title.contains("错误页面")) {	
			        	logger.warn("-------------农业银行储蓄卡------------错误页面-------------");
						status.put("errorCode", "0002");// 异常处理
						status.put("errorInfo", "验证码输入有误");
						PushSocket.push(status, UUID, "3000","验证码输入有误");
						PushState.state(idCard, "savings", 200,"验证码输入有误");
						driver.quit();
						return status;
			        }
					String cusname = "";						
					logger.warn("-----------农业储蓄卡-----------登陆成功----------身份证号："+idCard);
						// 登陆成功 
					//调出httpwatch
					HttpWatchUtil.openHttpWatch();
					PushSocket.push(status, UUID, "2000","农业储蓄卡登陆成功");// 开始执行推送登陆成功
					Thread.sleep(2000);
					driver.switchTo().frame("contentFrame");
					PushSocket.push(status, UUID, "5000","农业储蓄卡数据获取中");
					flag = 2;
					// 拿到姓名 
					WebElement custName = driver
							.findElement(By.id("show-custName"));
					cusname = custName.getText();
		
					List<Map<String, Object>> list1 = new ArrayList<Map<String, Object>>();
					list1 = getInfo(driver,list1,numbe,cusname);
							
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
					
					
				}			
			}else {
				try {
					
				//调出httpwatch
				HttpWatchUtil.openHttpWatch();
				String cusname = "";						
				logger.warn("-----------农业储蓄卡-----------登陆成功----------身份证号："+idCard);
					// 登陆成功 
					
				PushSocket.push(status, UUID, "2000","农业储蓄卡登陆成功");// 开始执行推送登陆成功
				Thread.sleep(2000);
				driver.switchTo().frame("contentFrame");
				PushSocket.push(status, UUID, "5000","农业储蓄卡数据获取中");
				flag = 2;
				// 拿到姓名 
				WebElement custName = driver
						.findElement(By.id("show-custName"));
				cusname = custName.getText();
	
				List<Map<String, Object>> list1 = new ArrayList<Map<String, Object>>();
				list1 = getInfo(driver,list1,numbe,cusname);
						
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
				
				
			}	
			}
			
			driver.quit();	
			return status;	
		}


		public static List<Map<String, Object>>	getInfo(WebDriver driver,List<Map<String, Object>> list,String userCard,String cusname) throws Exception{	
			String jsession = HttpWatchUtil.getCookie("WT_FPC");			
			System.out.println("-----------------jsession=:"+jsession);
			Map<String, Object> params = null;
			Map<String, String> headers = new HashMap<String, String>(16);
			params = new HashMap<String, Object>(16);			
			// 请求1  账单
			logger.warn("--------------账单请求-----------------");
			headers.put("Host", "perbank.abchina.com");					
			headers.put("Cookie", jsession);
			
			headers.put("Referer", "https://perbank.abchina.com/EbankSite/index.do");
			String response = SimpleHttpClient.get("https://perbank.abchina.com/EbankSite/MyAccountInitAct.do", headers);
			logger.warn("--------------账单请求结果-----------------"+response);
			
			//String response = SimpleHttpClient.post("https://perbank.abchina.com/EbankSite/MyAccountInitAct.do", params,headers);
//			driver.get("https://perbank.abchina.com/EbankSite/MyAccountInitAct.do");
			
			System.out.println(driver.getPageSource());
			
			// 请求2   明细
			logger.warn("--------------明细请求-----------------");
			jsession = HttpWatchUtil.getCookie("WT_FPC");			
			System.out.println("-----------------jsession=:"+jsession);
			headers.put("Cookie", jsession);
			params.put("acctId", userCard);	
			params.put("acctCurCode", 156);
			params.put("oofeFlg", 0);
			headers.put("Content-Type", "application/x-www-form-urlencoded");			
			headers.put("Referer", "https://perbank.abchina.com/EbankSite/MyAccountInitAct.do");
			
			headers.put("Accept", "text/html, application/xhtml+xml, */*");		
			headers.put("Accept-Language", "zh-CN");
			headers.put("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)");
			headers.put("Accept-Encoding", "gzip, deflate");	
			headers.put("Connection", "Keep-Alive");	
			headers.put("Content-Type", "application/x-www-form-urlencoded");
			response = SimpleHttpClient.post("https://perbank.abchina.com/EbankSite/AccountTradeDetailQueryInitAct.do",params, headers);			
			logger.warn("--------------明细请求结果-----------------"+response);

			
			
			//六个月
			Map<String, String> headers1 = new HashMap<String, String>(16);
			logger.warn("--------------六个月明细请求-----------------");
			headers1.put("Accept", "application/json, text/javascript, */*; q=0.01");
			headers1.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
			headers1.put("Referer", "https://perbank.abchina.com/EbankSite/AccountTradeDetailQueryInitAct.do");	
			headers1.put("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)");	
			headers1.put("X-Requested-With", "XMLHttpRequest");	
			headers1.put("Accept-Language", "zh-CN");
			headers1.put("Accept-Encoding", "gzip, deflate");
			headers1.put("Cookie", jsession);
			headers1.put("Connection", "Keep-Alive");
			headers1.put("DNT", "1");
			headers1.put("Cache-Control", "no-cache");
			headers1.put("Host", "perbank.abchina.com");	
			
					
		
			params.clear();
			//trnStartDt=20170801&trnEndDt=20180117&acctId=6228450216003329461&acctType=401&acctName=张焕斌&acctOpenBankId=33017&provCode=26&busCode=200002&oofeFlg=0&acctCurCode=156&nextPageKey=
			String startTime = Dates.beforMonth(6);
			String endTime = Dates.currentTime();
			logger.warn("---------------------startTime:"+startTime+"------------------------endTime:"+endTime);
			params.put("trnStartDt", startTime);	
			params.put("trnEndDt", endTime);
			params.put("acctId", userCard);
			
			params.put("acctType", 401);	
			params.put("acctName", cusname);
			params.put("acctOpenBankId", 33017);
			
			params.put("provCode", 26);	
			params.put("busCode", 200002);
			params.put("oofeFlg", 0);
			params.put("acctCurCode", 156);
			params.put("nextPageKey", "");
			response = SimpleHttpClient.post("https://perbank.abchina.com/EbankSite/AccountTradeDetailQueryAct.do",params, headers1);
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
			return infos;
			
			
		}
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
			
		
}