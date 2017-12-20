package com.reptile.Bank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoomsun.KeyBoard.SendKeys;
import com.reptile.util.DriverUtil;
import com.reptile.util.KeysPress;
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
				String userpwd, String UUID, String card){
			int numCount=0;//打码次数
			logger.warn("-----------农业储蓄卡-----------登陆开始----------身份证号："+card);
			Map<String, Object> status = new HashMap<String, Object>();
			PushSocket.push(status, UUID, "1000","农业储蓄卡登录中");
			PushState.state(card, "savings", 100);
			WebDriver driver = null;
			try{
				Map<String, Object> params = new HashMap<String, Object>();
				Map<String, String> headers = new HashMap<String, String>();
				 //打开此网页 
				driver = DriverUtil.getDriverInstance("ie");
				driver.get("https://perbank.abchina.com/EbankSite/startup.do");
				 //判断是否加载页面 
				DriverUtil.waitByTitle(driver.getTitle(), driver, 10);
				// 键入账号 
				WebElement element = driver.findElement(By.id("username"));
				element.sendKeys(username);
				SendKeys.sendTab();
				Thread.sleep(500);
				// 特殊字符处理,输入密码  
				SendKeys.sendStr(userpwd);
				 //输入验证码 
				Thread.sleep(1000);
				WebElement elements = driver.findElement(By.id("vCode"));
				WebElement code = driver.findElement(By.id("code"));
				String imgtext = BcmLogin.downloadImgs(driver, elements, 10, 10);
				code.sendKeys(imgtext);
				
				 //登陆 
				WebElement logo = driver.findElement(By.id("logo"));
				logo.click(); 
				if(DriverUtil.visibilityById("powerpass_ie_dyn_Msg", driver, 2) || DriverUtil.visibilityById("username-error", driver, 0) || (DriverUtil.waitByClassName("logon-error", driver, 1)&&!driver.findElement(By.className("logon-error")).getAttribute("title").isEmpty())){
					String text = "";
					if(DriverUtil.visibilityById("username-error", driver, 2)){
						text = driver.findElement(By.id("username-error")).getText();
					}else if(DriverUtil.visibilityById("powerpass_ie_dyn_Msg", driver, 2)){
						text = driver.findElement(By.id("powerpass_ie_dyn_Msg")).getText();
					}else{
						text = driver.findElement(By.className("logon-error")).getAttribute("title");
					}
					PushState.state(card, "savings", 200);
					PushSocket.push(status, UUID, "3000",text);
					status.put("errorCode", "0001");// 异常处理	
					status.put("errorInfo", text);
				}else if(DriverUtil.waitByTitle("中国农业银行个人网银首页", driver, 10)){
					logger.warn("-----------农业储蓄卡-----------登陆成功----------身份证号："+card);
					// 登陆成功 
					
					PushSocket.push(status, UUID, "2000","农业储蓄卡登陆成功");// 开始执行推送登陆成功
					Thread.sleep(2000);
					driver.switchTo().frame("contentFrame");
					PushSocket.push(status, UUID, "5000","农业储蓄卡数据获取中");
					// 拿到姓名 
					WebElement custName = driver
							.findElement(By.id("show-custName"));
					String cusname = custName.getText();
		
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
					if(DriverUtil.visibilityById("AccountTradeDetailTable", driver, 15)){
						// 拿到开户行 以及卡号 
						WebElement label = driver.findElement(By.className("label"));
						// 卡号 
						String[] sp = label.toString().split("|");
						// 开始解析 
						Document docs = Jsoup.parse(driver.getPageSource());
						Element trs = docs.getElementById("AccountTradeDetailTable");
						Elements tr = trs.select("tr");
						List<Object> list = new ArrayList<Object>();
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
						params.put("IDNumber", card); //身份证 
						params.put("cardNumber", sp[1]); //用户卡号 
						params.put("userName", cusname); //用户姓名 
						PushSocket.push(status, UUID, "6000","农业银行储蓄卡数据获取成功");

//						Resttemplate resttemplate = new Resttemplate();
//						status = resttemplate.SendMessage(params, application.sendip+ "/HSDC/savings/authentication", card);
						status = new Resttemplate().SendMessage(params, application.sendip+"/HSDC/savings/authentication");  //推送数据
	    			    if(status!= null && "0000".equals(status.get("errorCode").toString())){
	    		           	 PushState.state(card, "savings", 300);
	    		           	PushSocket.push(status, UUID, "8000","认证成功");
	    		           	status.put("errorInfo","推送成功");
	    		           	status.put("errorCode","0000");
    		           }else{
	    		           	PushState.state(card, "savings", 200);
	    		           	PushSocket.push(status, UUID, "9000",status.get("errorInfo").toString());
	    		           	status.put("errorCode",status.get("errorCode"));//异常处理
	    		           	status.put("errorInfo",status.get("errorInfo"));
    		           }
					}else{
						PushSocket.push(status, UUID, "7000","网页异常，数据获取失败");
						PushState.state(card, "savings", 200);
						status.put("errorCode","0001");//异常处理
    		           	status.put("errorInfo","网页异常,数据获取失败");
						throw new Exception();
					}
				
				}else{
					numCount=numCount+1;
					driver.quit();
					if(numCount>5) {
						PushSocket.push(status, UUID, "3000","网页异常,登录失败");
    		           	status.put("errorCode","0001");//异常处理
    		           	status.put("errorInfo","网页异常,登录失败");
						return status;
					}
					status = doGetDetail(username, userpwd, UUID, card);
				}
					
			}catch(Exception e){
				logger.warn("-------------农业银行储蓄卡------------获取详情失败-------------", e);
				status.put("errorCode", "0002");// 异常处理
				status.put("errorInfo", "网络异常，请重试！");
				PushSocket.push(status, UUID, "9000","网页异常，认证失败");
				PushState.state(card, "savings", 200);
			}finally{
				DriverUtil.close(driver);
			}
			logger.warn("-----------农业储蓄卡-----------查询结束----------返回信息为："+status.toString());
			return status;
		}
}