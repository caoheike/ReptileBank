package com.reptile.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import com.hoomsun.keyBoard.HttpWatchUtil;
import com.hoomsun.keyBoard.SendKeys;
import com.reptile.util.CountTime;
import com.reptile.util.DriverUtil;
import com.reptile.util.ImgUtil;
import com.reptile.util.JsonUtil;
import com.reptile.util.PushSocket;
import com.reptile.util.PushState;
import com.reptile.util.Resttemplate;
import com.reptile.util.SimpleHttpClient;
import com.reptile.util.application;

/**
 * 
 * @ClassName: CmbcCreditService
 * @Description: TODO (民生信用卡)
 * @author: xuesongcui
 * @date 2018年1月2日
 *
 */
@Service("cmbcCreditService")
public class CmbcCreditService {

	Logger logger = Logger.getLogger(CmbcCreditService.class);


	/**
	 * 民生信用卡
	 * 
	 * @param number
	 * @param pwd
	 * @param banktype
	 * @param idcard
	 * @param UUID
	 * @param timeCnt
	 * @return
	 * @throws Exception
	 */
	public synchronized Map<String, Object> doLogin(HttpServletRequest request,String number, String pwd,
			String banktype, String idcard, String uuid, String timeCnt)
			throws Exception {
		
		boolean isok = CountTime.getCountTime("2018-01-02");
		if (isok == true) {
			PushState.state(idcard, "bankBillFlow", 100);
		}
		
		Map<String, Object> map = new HashMap<String, Object>(16);
		PushSocket.push(map, uuid, "1000", "民生银行登录中");
		WebDriver driver = null;
		try {
			WebElement errorinfo = null;
			try {
				logger.warn("----------------民生信用卡-------------登陆开始-----------------用户名："
						+ number + "密码：" + pwd);
				driver = DriverUtil.getDriverInstance("ie");
				Thread.sleep(1000);
				
				//打开登录也页面
				driver.get("https://nper.cmbc.com.cn/pweb/static/login.html");
				
				Boolean flag = DriverUtil.waitByTitle("中国民生银行个人网上银行", driver, 15);
				if(flag){
					
					Thread.sleep(1000);
					/* 获得输入元素 */
					WebElement elements = driver.findElement(By.id("writeUserId"));
					elements.sendKeys(number);
					/* 执行换号 */
					Thread.sleep(1000);
//					HttpWatchUtil.sendTab();
//					HttpWatchUtil.sendStr(pwd);
					// SendKeys.sendStr(1180, 380 - 5, pwd);
					SendKeys.sendStr(1180, 380+60, pwd);//本地
					Thread.sleep(1000);

					WebElement loginButton = driver.findElement(By.id("loginButton"));
					WebElement webElement = driver.findElement(By.id("_tokenImg"));

					String src = "src";
					if (webElement.getAttribute(src) == null
							|| "".equals(webElement.getAttribute(src))) {
						/* 不需要验证码直接提交 */
					} else {
						/* 需要验证码进行打码 */
						logger.warn("----------------需要打印验证码----------------");

						WebElement imgEle = driver.findElement(By.id("_tokenImg"));
//						String code = ImgUtil.saveImg(imgEle, driver, "msc", ".png");
		                String imageCode=CmbSavingsService.imageGet(imgEle, driver,request );
						driver.findElement(By.id("_vTokenName")).sendKeys(imageCode);
					}
					
					loginButton.click();
					//调出httpwatch
					HttpWatchUtil.openHttpWatch();
					errorinfo = driver.findElement(By.className("alert-heading"));
					
				}else{
					throw new Exception();
				}
				
			} catch (Exception e) {
				logger.warn(e + "网络异常，登录失败");
				PushSocket.push(map, uuid, "3000", "网络异常，登录失败");
				if (isok == true) {
					PushState.state(idcard, "bankBillFlow", 200);
				}
				map.put("errorCode", "0001");
				map.put("errorInfo", "网络错误");
				DriverUtil.close(driver);
				return map;
			}
			if (!"".equals(errorinfo.getText())) {
				PushSocket.push(map, uuid, "3000", errorinfo.getText());
				if (isok == true) {
					PushState.state(idcard, "bankBillFlow", 200);
				}
				map.put("errorCode", "0001");
				map.put("errorInfo", errorinfo.getText());
				driver.quit();
				return map;
			} else {
				Boolean flag = DriverUtil.waitByTitle("中国民生银行个人网银", driver, 15);
				if(flag){
					
					String title = "中国民生银行个人网银";
					if (driver.getTitle().contains(title)) {
						PushSocket.push(map, uuid, "2000", "民生银行登陆成功");

						logger.warn("----------------民生信用卡-------------登陆成功-----------------用户名："+ number);
						Thread.sleep(2000);
						PushSocket.push(map, uuid, "5000", "民生银行数据获取中");
						
						Map<String, Object> data = new HashMap<String, Object>(16);
						data.put("html", this.getDetail());
						data.put("backtype", "CMBC");
						data.put("idcard", idcard);
						data.put("userAccount", number);
						
						map.put("data", data);
						map.put("isok", isok);
						map = new Resttemplate().SendMessageX(map, application.sendip + "/HSDC/BillFlow/BillFlowByreditCard", idcard,uuid);

					} else {
						PushSocket.push(map, uuid, "3000", "网络异常，登陆失败");
						if (isok == true) {
							PushState.state(idcard, "bankBillFlow", 200);
						}
						map.put("errorCode", "0001");
						map.put("errorInfo", "失败");
					}
				}else{
					throw new Exception();
				}
			}
		} catch (Exception e) {
			logger.error("民生信用卡查询失败", e);
			PushSocket.push(map, uuid, "7000", "网页数据没有找到");
			if (isok == true) {
				PushState.state(idcard, "bankBillFlow", 200);
			}
			map.put("errorCode", "0001");
			map.put("errorInfo", "网络错误");
		} finally {
			DriverUtil.close(driver);
		}
		logger.warn("------------民生信用卡-----------查询结束----------------返回信息为：" + map.toString() + "-------------");
		return map;
	}

	
	
	
	/**
	 * 通过发包的形式获取账单
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<List<String>> getDetail() throws Exception {

		List<List<String>> list = new ArrayList<List<String>>(16);

		String jsession = HttpWatchUtil.getCookie("JSESSION");
		Map<String, String> headers = new HashMap<String, String>(16);
		headers.put("Cookie", jsession);
		headers.put("Referer", "https://nper.cmbc.com.cn/pweb/static/main.html");
		headers.put("Host", "nper.cmbc.com.cn");

		Map<String, Object> params = new HashMap<String, Object>(16);
		// 请求1
		String response = SimpleHttpClient.post("https://nper.cmbc.com.cn/pweb/CreditCheckBillQry.do", params,headers);
		logger.warn("------请求1------response：" + response);
		params.put("CreditAcType", "0010");
		// 请求2
		response = SimpleHttpClient.post(
				"https://nper.cmbc.com.cn/pweb/CreditCardServiceTypeQry.do",
				params, headers);
		logger.warn("------请求2------response：" + response);
		String billDay = (String) JsonUtil.getJsonValue1(response, "BillDay");
		int month = (int) JsonUtil.getJsonValue1(response, "Month");
		int year = (int) JsonUtil.getJsonValue1(response, "Year");
		String recentDate = "";
		if(month<9) {
			recentDate = String.valueOf(year).substring(2) + "0"+month;
		}else {
			recentDate = String.valueOf(year).substring(2) + month;
		}
		

		params.clear();
		params.put("CreditAcType", "0010");
		params.put("CurrencyFlag", "L");
		params.put("BillDate", recentDate);
		params.put("BillDay", billDay);
		// 请求3
		response = SimpleHttpClient.post(
				"https://nper.cmbc.com.cn/pweb/CreditBillTitleQry.do", params,
				headers);
		logger.warn("------请求3------response：" + response);
		String acNo = (String) JsonUtil.getJsonValue1(response, "AcNo");

		int total = 6;
		for (int j = 0; j < total; j++) {

			List<String> item = new ArrayList<String>(16);

			params.clear();
			params.put("CreditAcType", "0010");
			params.put("CurrencyFlag", "L");
			params.put("BillDate", recentDate);
			params.put("billDay", billDay);
			params.put("uri", "/pweb/CreditBillQry.do");
			params.put("currentIndex", 0);
			params.put("BillFlag", "1");
			params.put("AcNo", acNo);
			// 请求4
			response = SimpleHttpClient.post(
					"https://nper.cmbc.com.cn/pweb/CreditBillQry.do", params,
					headers);
			logger.warn("------请求4------response：" + response);
			if (response.equals("")) {
				break;
			}
			List<Map<String, Object>> infosList = new ArrayList<Map<String, Object>>();//页面获取信息List
			List<Map<String, Object>> getInfosList = new ArrayList<Map<String, Object>>();//解析后存放信息List
			infosList = (List<Map<String, Object>>) JsonUtil.getJsonValue1(response,
					"List");
//			getInfosList = getInfos(infosList,getInfosList);
//			item.add(response);
			int pageNumber = (int) JsonUtil.getJsonValue1(response,
					"pageNumber");
			int begin = 2;
			if (pageNumber > 1) {
				int pageSize = (int) JsonUtil.getJsonValue1(response,
						"pageSize");
				int recordNumber = (int) JsonUtil.getJsonValue1(response,
						"recordNumber");
				params.put("recordNumber", recordNumber);

				for (int i = begin; i <= pageNumber; i++) {
					params.put("currentIndex", (i - 1) * pageSize);
					params.put("pageNo", i);
					// 请求5
					response = SimpleHttpClient.post(
							"https://nper.cmbc.com.cn/pweb/CreditBillQry.do",
							params, headers);
					logger.warn("------请求5------response：" + response);
					infosList = (List<Map<String, Object>>) JsonUtil.getJsonValue1(response,
							"List");
//					getInfosList = getInfos(infosList,getInfosList);
				}
			}
			recentDate = this.lastDate(recentDate);
		}
		return list;
	}
	public List<Map<String, Object>> getInfos(List<Map<String, Object>> list,List<Map<String, Object>> getLists){
		Map<String, Object> info = new HashMap<String, Object>();
		/*
		 * "MonthSeq":"333",
            "JnlNo":"258151",
            "TransAmt":22.5,
            "LargeBuyFlag":"0",
            "RecordDate":"2017-09-08",
            "TransAmtMrk":"+",
            "AcNoRearFour":"3902",
            "ConsumeDate":"0908",
            "CancelFlag":"0",
            "AuthCode":"155870",
            "TransDescribe2":"",
            "TransDescribe1":"北京钱袋宝支付技术有限公司",
            "TransDescribe":"北京钱袋宝支付技术有限公司 ",
            "ConsumeTime":"11073479",
            "Currency":"CNY",
            "TransDate":"2017-09-08"
		 */
		for (Map<String, Object> map : getLists) {
			
		}
		
		
		
		
		return getLists;
	}
	/**
	 * 获取上个月
	 * 
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public String lastDate(String date) throws ParseException {

		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		
		// date格式为yyMM，在前面加上20才能变成完整的年月
		String currentDate = "20" + date;
		Date d = format.parse(currentDate);

		Calendar c = Calendar.getInstance();
		c.setTime(d);

		c.add(Calendar.MONTH, -1);

		String time = format.format(c.getTime());
		return time.substring(2);
	};

	
}
