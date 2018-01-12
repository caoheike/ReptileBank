package com.reptile.Bank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoomsun.keyBoard.SendKeys;
import com.reptile.util.DriverUtil;
import com.reptile.util.PushSocket;
import com.reptile.util.PushState;
import com.reptile.util.Resttemplate;
import com.reptile.util.application;
@SuppressWarnings("deprecation")

/**
 * @author Bigyoung
 * @version v1.0
 * @deprecated 农业银行
 * @date 2017年11月16日10:51:14
 */
public class AbcBank {
	
		private static Logger logger= LoggerFactory.getLogger(AbcBank.class);

		/**
		 * 获取农业银行详情信息
		 * @param username
		 * @param userpwd
		 * @param UUID
		 * @param card
		 * @return
		 * @throws Exception
		 */
		public static Map<String, Object> doGetDetail(String username,
				String userpwd, String UUID, String card,HttpSession session){
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
//				Thread.sleep(2000);
//				SendKeys.sendTab();
				Thread.sleep(1500);
				// 特殊字符处理,输入密码  
				SendKeys.sendStr(1143+80, 378-40, userpwd);
//				SendKeys.sendStr(1143+80, 378+15, userpwd);//本地
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
						
					PushSocket.push(status, UUID, "2000","农业储蓄卡登陆成功");// 开始执行推送登陆成功
					Thread.sleep(2000);
					driver.switchTo().frame("contentFrame");
					PushSocket.push(status, UUID, "5000","农业储蓄卡数据获取中");
					flag = 2;
					// 拿到姓名 
					WebElement custName = driver
							.findElement(By.id("show-custName"));
					cusname = custName.getText();
		
					WebElement dh = driver.findElement(By.linkText("本行账户"));
					dh.click();
					// 选择账号明细 
					WebElement mx = driver.findElement(By.linkText("明细"));
					// 隐患：万一多张卡 拿到的就是多个明细
					mx.click();
					// 选择日期 
					WebElement startDate = driver.findElement(By.id("startDate"));
					List<Map<String, Object>> data = BcmLogin.yuefen();
					// 隐患：万一多张卡 拿到的就是多个明细
					startDate.clear();
					startDate.sendKeys(data.get(0).get("begin").toString());
					WebElement btn_query = driver.findElement(By.id("btn_query"));
					Thread.sleep(2000);
					btn_query.click();
					
					Element trs = null;
					Elements tr = null;
					String[] sp = null;
					if(DriverUtil.visibilityById("AccountTradeDetailTable", driver, 15)){
						List<Object> list = new ArrayList<Object>();
						// 拿到开户行 以及卡号 
						WebElement label = driver.findElement(By.className("label"));
						// 卡号 
						sp = label.toString().split("|");
						// 开始解析 
						Document docs = Jsoup.parse(driver.getPageSource());
						trs = docs.getElementById("AccountTradeDetailTable");
						tr = trs.select("tr");
						
						
							
						for (int i = 0; i < tr.size(); i++) {
							Map<String, Object> map = new HashMap<String, Object>();
							Elements td = tr.get(i).select("td");
							for (int j = 0; j < td.size(); j++) {
								if (j == 0) {
									 //交易时间 
									map.put("dealTime", td.get(j).text());
									
								}
								if (j == 1) {
									 //交易金额 
									
									if (td.get(j).text().contains("+")) {
										map.put("incomeMoney", td.get(j).text());
										
									} else {
										map.put("expendMoney", td.get(j).text());
									}
								}
								if (j == 2) {
									 //本次余额 
									map.put("balanceAmount", td.get(j).text());
								}
								if (j == 3) {
									//对方信息 姓名加卡号 
								}
								if (j == 4) {
									// 交易类型 
									
								}
								if (j == 5) {
									// 交易渠道 
									map.put("dealDitch", td.get(j).text());
									
								}
								if (j == 6) {
									// 交易摘要 
									map.put("dealReferral", td.get(j).text());
									
								}
								map.put("oppositeSideName", "");
								map.put("oppositeSideNumber", "");
								map.put("currency", "");
								list.add(map);
							}
						}
							
							
						params.clear();
						headers.clear();
						headers.put("accountType", ""); //账号状态 
						headers.put("openBranch", sp[2]);// 开户网点 
						headers.put("openTime", ""); //开户日期 
						
						params.put("billMes", list);
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
					}else{
						PushSocket.push(status, UUID, "7000","网页异常，数据获取失败");
						PushState.state(idCard, "savings", 200,"网页异常，数据获取失败");
						status.put("errorCode","0001");//异常处理
 		           		status.put("errorInfo","网页异常,数据获取失败");
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
				String cusname = "";
				try {
					
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
	
				WebElement dh = driver.findElement(By.linkText("本行账户"));
				dh.click();
				// 选择账号明细 
				WebElement mx = driver.findElement(By.linkText("明细"));
				// 隐患：万一多张卡 拿到的就是多个明细
				mx.click();
				// 选择日期 
				WebElement startDate = driver.findElement(By.id("startDate"));
				List<Map<String, Object>> data = BcmLogin.yuefen();
				// 隐患：万一多张卡 拿到的就是多个明细
				startDate.clear();
				startDate.sendKeys(data.get(0).get("begin").toString());
				WebElement btn_query = driver.findElement(By.id("btn_query"));
				Thread.sleep(2000);
				btn_query.click();
				
				Element trs = null;
				Elements tr = null;
				String[] sp = null;
				if(DriverUtil.visibilityById("AccountTradeDetailTable", driver, 15)){
					List<Object> list = new ArrayList<Object>();
						
					// 拿到开户行 以及卡号 
					WebElement label = driver.findElement(By.className("label"));
					// 卡号 
					sp = label.toString().split("|");
					// 开始解析 
					Document docs = Jsoup.parse(driver.getPageSource());
					trs = docs.getElementById("AccountTradeDetailTable");
					tr = trs.select("tr");
					
					
						
					for (int i = 0; i < tr.size(); i++) {
						Map<String, Object> map = new HashMap<String, Object>();
						Elements td = tr.get(i).select("td");
						for (int j = 0; j < td.size(); j++) {
							if (j == 0) {
								 //交易时间 
								map.put("dealTime", td.get(j).text());
								
							}
							if (j == 1) {
								 //交易金额 
								
								if (td.get(j).text().contains("+")) {
									map.put("incomeMoney", td.get(j).text());
									
								} else {
									map.put("expendMoney", td.get(j).text());
								}
							}
							if (j == 2) {
								 //本次余额 
								map.put("balanceAmount", td.get(j).text());
							}
							if (j == 3) {
								//对方信息 姓名加卡号 
							}
							if (j == 4) {
								// 交易类型 
								
							}
							if (j == 5) {
								// 交易渠道 
								map.put("dealDitch", td.get(j).text());
								
							}
							if (j == 6) {
								// 交易摘要 
								map.put("dealReferral", td.get(j).text());
								
							}
							map.put("oppositeSideName", "");
							map.put("oppositeSideNumber", "");
							map.put("currency", "");
							list.add(map);
						}
					}
					
					
					params.clear();
					headers.clear();
					headers.put("accountType", ""); //账号状态 
					headers.put("openBranch", sp[2]);// 开户网点 
					headers.put("openTime", ""); //开户日期 
					
					params.put("billMes", list);
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
				}else{
					PushSocket.push(status, UUID, "7000","网页异常，数据获取失败");
					PushState.state(idCard, "savings", 200,"网页异常，数据获取失败");
					status.put("errorCode","0001");//异常处理
		           	status.put("errorInfo","网页异常,数据获取失败");
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

		
}