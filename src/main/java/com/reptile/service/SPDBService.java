package com.reptile.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hoomsun.keyBoard.SendKeys;
import com.reptile.util.PushSocket;
import com.reptile.util.PushState;
import com.reptile.util.Resttemplate;
import com.reptile.util.application;
@Service
public class SPDBService {
  private final static String SPDBLOGIN="https://ebank.spdb.com.cn/nbper2017/popInnerLogin.do?Reserve=";//浦发银行登陆界面
  private final static String GETDETAIL_URL="https://ebank.spdb.com.cn/nbper2017/PreQueryBalance.do?_viewReferer=default,account/QueryBalance&selectedMenu=menu1_1_1";
  private static Logger logger= LoggerFactory.getLogger(SPDBService.class);
	/**
	 * 
	 * @param userCard 身份证号码
	 * @param passWord 密码
	 * @return
	 */
	 public  Map<String,Object>  login(HttpServletRequest request,String userCard,String passWord,String UUID){
		 
		  Map<String,Object> map=new HashMap<String,Object>();
		  PushSocket.push(map, UUID, "1000","浦发银行登录中");
		  PushState.state(userCard, "savings",100);
		  logger.warn("浦发银行");
		  WebDriver driver=  this.getDriver(SPDBLOGIN);
		  driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		// new WebDriverWait(driver, 25).until(ExpectedConditions.presenceOfElementLocated(By.name("LoginId")));
		  try {
			Thread.sleep(6000);
		    WebElement account=  driver.findElement(By.id("LoginId"));//输入身份证框
			Thread.sleep(300);
		    account.sendKeys(userCard);
		   /* KeyPresss("Tab");//定位到控件
*/		   
		    SendKeys.sendTab();
		    SendKeys.sendStr(passWord);//输入密码
		  } catch (Exception e) {
		 	logger.warn("浦发银行",e);
			map.put("errorCode", "0001");
            map.put("errorInfo", "网络连接异常!");
            PushSocket.push(map, UUID, "3000","网络连接异常，登录失败");
            return map;
		  }
		  WebElement loginButton=  driver.findElement(By.id("LoginButton"));
		  loginButton.click();
		  try {//登陆失败
			    Alert alert=  driver.switchTo().alert();
			    logger.warn("浦发银行",alert.getText());
			    String tip=alert.getText().toString();
				map.put("errorCode", "0001");
	            map.put("errorInfo", tip);
	            PushSocket.push(map, UUID, "3000",tip);
	            alert.accept(); 
	            return map;
			   } catch (org.openqa.selenium.NoAlertPresentException e) {//没有弹窗时判断是否登陆成功，是否需要验证码
				   if(driver.getPageSource().contains("先生")||driver.getPageSource().contains("女士")){
				   PushSocket.push(map, UUID, "2000","浦发银行登录成功");
				  logger.warn("浦发银行，登陆成功");
				  
				  String name=this.getName(driver);//获取用户名
				  driver.get(GETDETAIL_URL); 
				  try {
					Thread.sleep(2000);
					PushSocket.push(map, UUID, "5000","浦发银行数据获取中");
					WebElement  detail=driver.findElement(By.className("detail"));
					detail.click();
					Thread.sleep(1000);
					 logger.warn("浦发银行，详单获取中...");					 
					WebElement acc= driver.findElement(By.id("AcctNo"));
					String cardNumber=acc.getAttribute("value").toString();//卡号
					driver.findElement(By.id("changeDate4")).click();
					driver.findElement(By.className("button")).click();
					List<Map<String, String>> billMes=new ArrayList<Map<String,String>>();//存放流水
					Map<String, String> baseMes=new HashMap<String, String>();//存放基本信息
					Thread.sleep(3000);					
					billMes=this.forParseBillMes(driver, billMes);//流水
					baseMes=this.parseBaseMes(baseMes);//基本信息					
				    map.put("bankName", "浦发银行储蓄卡");//银行名称
				    map.put("cardNumber", cardNumber);//卡号
				    map.put("IDNumber", userCard);
				    map.put("userName", name);//用户名
					map.put("billMes", billMes);
					map.put("baseMes", baseMes);
					PushSocket.push(map, UUID, "6000","浦发银行数据获取成功");
					map = new Resttemplate().SendMessage(map, application.sendip+"/HSDC/savings/authentication");  //推送数据
    			    if(map!=null&&"0000".equals(map.get("errorCode").toString())){
    		           	 PushState.state(userCard, "savings", 300);
    		           	PushSocket.push(map, UUID, "8000","浦发银行认证成功");
    		           	map.put("errorInfo","推送成功");
    		           	map.put("errorCode","0000");
    		           }else{
    		           	PushState.state(userCard, "savings", 200);
    		           	PushSocket.push(map, UUID, "9000",map.get("errorInfo").toString());
    		           	map.put("errorInfo","推送失败");
    		           	map.put("errorCode","0001");
    		           }
    			    
				} catch (Exception e1) {
					PushSocket.push(map, UUID, "9000","网络连接异常，认证失败");
					PushState.state(userCard, "savings",200);
					map.put("errorCode", "0001");
		            map.put("errorInfo", "网络连接异常!");
					logger.warn("浦发银行",e);
				}
			}else{//用户名或密码错误
			    WebElement tip=	driver.findElement(By.className("loginErr"));
				logger.warn("----浦发银行登陆失败----失败原因："+tip.getText());
				PushState.state(userCard, "savings",200);
				PushSocket.push(map, UUID, "3000",tip.getText());
				map.put("errorCode", "0001");
	            map.put("errorInfo", tip.getText());
	       
			}
	  }finally{
		  try {
			driver.quit();
		} catch (Exception e) {
			logger.warn("-----浦发银行浏览器进程关闭失败-----",e);
		}

	  }	  
		  return map; 
	 }
	  
	
	    
	 /**
	  * 打开页面并返回driver
	  * @param url 打开页面的URL
	  * @return
	  */
	 public  WebDriver getDriver(String url){
		 System.setProperty("webdriver.ie.driver", "C:\\Program Files\\iedriver\\IEDriverServer.exe");
		 WebDriver driver =new InternetExplorerDriver();
			driver.get(url);
			driver.manage().window().maximize();
			driver.navigate().refresh();
			return driver;
	 }
	 /**
	  * 流水解析
	  * @param driver
	  * @param billMes 存放流水的list
	  * @return
	  */
	 public  List<Map<String, String>> parseBillMes(WebDriver driver,List<Map<String, String>> billMes){
		    String detailPage=	driver.getPageSource();
		    Document  de=	Jsoup.parse(detailPage);
		    Element table=   de.getElementsByClass("table").get(0);
		    Elements tr=table.getElementsByTag("tr");
		    for (int i = 2; i < tr.size(); i++) {
		    	Map<String, String> bill=new HashMap<String, String>();
				Elements td=tr.get(i).getElementsByTag("td");
				bill.put("dealTime", td.get(1).text().trim());//交易时间
				bill.put("dealReferral", td.get(2).text().trim());//业务摘要
				bill.put("incomeMoney", td.get(3).text().trim());//存入金额
				bill.put("expendMoney", td.get(4).text().trim());//转出金额	607209
				bill.put("balanceAmount",td.get(5).text().trim());//l
				bill.put("currency", "人民币");//币种
				bill.put("dealDitch", "");//交易渠道
				bill.put("oppositeSideName", "");//对方账户名
				bill.put("oppositeSideNumber", "");//对方账户
				billMes.add(bill);
			}
			return billMes;
		    
	 }
	 /**
	  * 解析遍历每一页
	  * @param driver
	  * @param billMes
	  * @return
	  * @throws InterruptedException
	  */
	 public  List<Map<String, String>> forParseBillMes(WebDriver driver,List<Map<String, String>> billMes) throws InterruptedException{
		    billMes=this.parseBillMes(driver, billMes);//第n页流水解析
			WebElement inp=driver.findElements(By.className("pageClass2")).get(2);//下一页按钮
			try {
				 inp.getAttribute("disabled").toString();
			} catch (java.lang.NullPointerException e2) {//有下一页
				System.out.println("有下一页");
				inp.click();
				Thread.sleep(3000);
				billMes=forParseBillMes(driver,billMes);
			}	
		
		return billMes;
	 }
	 /**
	  * 返回用户姓名
	  * @param driver
	  * @return
	  */
	 public  String getName(WebDriver driver) {
		 WebElement userName=driver.findElement(By.className("userName"));
			String userNames=userName.getText();
			if(userNames.contains("先生")){
				userNames=userNames.substring(0,userNames.lastIndexOf("先生"));
			}else{
				userNames=userNames.substring(0,userNames.lastIndexOf("女士"));
			}
			return userNames;
	}
	 /**
	  * 基本信息解析
	  * @param baseMes
	  * @return
	  */
	 public  Map<String, String> parseBaseMes(Map<String, String> baseMes){ 
	    baseMes.put("accountType", "");//账号状态
		baseMes.put("openBranch", "");//开户网点
		baseMes.put("openTime", "");//开户日期
		return baseMes;
	 }
}
