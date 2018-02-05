package com.reptile.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

			SendKeys.sendStr(1193+80, 358, pwd);
//			SendKeys.sendStr(1193+80, 358+35, pwd);//本地
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
				
				
				
				
				
				PushSocket.push(map, UUID, "6000","广发银行信用卡数据获取成功");
				logger.warn("########【广发银行信用卡数据获取成功】【身份证号：】"+usercard);
				
				data.put("html", listinfo);
				data.put("backtype", "GDB");
				data.put("idcard", usercard);
				data.put("userAccount", number);
				map.put("data", data);
				map.put("isok", isok);
				Resttemplate ct = new Resttemplate();
				logger.warn("########【广发银行信用卡开始推送数据】【身份证号：】"+usercard);
				flag = 2;
				map = ct.SendMessageX(map, application.sendip
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
	 
	public List<String> getInfos(WebDriver driver,List<String> listinfo,String number,String sid){
		List<String> monthList = new ArrayList<String>();
		monthList = getQueryMonth();
		Set<Cookie> cookies = driver.manage().getCookies();
	    StringBuffer cookie = new StringBuffer();
	    for (Cookie oneCookie : cookies) {
	      cookie = cookie.append(oneCookie.getName()+"="+oneCookie.getValue()+";");
	    }
	    String currentTime = Dates.currentTime();
		Map<String, String> headers = new HashMap<String, String>(16);
		headers.put("Referer", "https://ebanks.cgbchina.com.cn/perbank/html/creditcard/b080102_creditBillResult.htm?HTMVersion="+currentTime);
		headers.put("Host", "ebanks.cgbchina.com.cn");
		headers.put("Cookie", String.valueOf(cookie));
		
		
		for(int i=0;i<6;i++) {
				String response="";
				try {
					response = SimpleHttpClient.get("https://ebanks.cgbchina.com.cn/perbank/CR1080.do?"
							+ "currencyType=&creditCardNo="+number+"&billDate="+monthList.get(i).substring(0, 6)+"&billType=1&abundantFlag=0&"
							+ "terseFlag=0&showWarFlag=0&EMP_SID="+sid,headers);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.warn("--------广发银行登陆------------账单查询：第"+i+1+"次请求具体内容："+response);		
				if (!response.contains("账单尚未生成或不存在，请于账单日后再查询")) {											
					listinfo.add(response);
				}
			
		}
		return listinfo;

	}
	
	
	
	
}
