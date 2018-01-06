package com.reptile.winio;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Augmenter;

import com.hoomsun.keyBoard.SendKeys;
import com.reptile.util.CYDMDemo;
import com.reptile.util.CrawlerUtil;
import com.reptile.util.DriverUtil;
import com.reptile.util.PushState;
import com.reptile.util.SimpleHttpClient;

public class CmbBank {
	private Logger logger = Logger.getLogger(CmbBank.class);

	private static CYDMDemo cydmDemo = new CYDMDemo();

	/**
	 * 储蓄卡登陆 ：招商银行
	 * 
	 * @throws Exception
	 */

	public Map<String, Object> CMBLogin(String userName, String userPwd,
			HttpServletRequest request, String UserCard, String UUID){
		logger.warn("-----------招商银行储蓄卡-----------登陆开始----------身份证号："+ UserCard);
		SimpleHttpClient httclien = new SimpleHttpClient();
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, String> headers = new HashMap<String, String>();
		Map<String, Object> map = new HashMap<String, Object>();
		String sessid = new CrawlerUtil().getUUID(); // 生成UUid 用于区分浏览器
		HttpSession sessions = request.getSession();
		sessions.setAttribute("UserCard", UserCard);
		WebDriver driver = null;
		try {
			driver = DriverUtil.getDriverInstance("ie");
			driver.manage().window().maximize();
			driver.get("https://pbsz.ebank.cmbchina.com/CmbBank_GenShell/UI/GenShellPC/Login/Login.aspx");
			HttpSession session = request.getSession();
			Thread.sleep(1500);
			SendKeys.sendStr(userName);
			Thread.sleep(1500);
			//KeysPress.SenStr(userName);
			/* 按下Tab */
			/*KeysPress.SendTab("Tab");
			Thread.sleep(1000);*/
//			SendKeys.sendTab();
//			Thread.sleep(1000);
			/* 输入密码 */
//			SendKeys.sendStr(userPwd);
			SendKeys.sendStr(1156+90, 394-25, userPwd);
//			SendKeys.sendStr(1156+90, 394+15, userPwd);//本地
			Thread.sleep(1000);
			//KeysPress.SenStr(userPwd);
			WebElement LoginBut = driver.findElement(By.id("LoginBtn"));
			LoginBut.click();
			Thread.sleep(5000);// 延迟三秒
			logger.warn("判断附加码之前********** ");
			/* 判断是否需要验证码 */
			boolean isHave = DriverUtil.waitByClassName("page-form-item", driver, 1);
			//WebElement elements1 = driver.findElement(By
					//.className("page-form-item"));
			WebElement elements1=null;
			if(isHave) {
				elements1 = driver.findElement(By
						.className("page-form-item"));
				logger.warn("**********"+elements1);
				if (elements1.getText().contains("附加码")) {
					logger.warn("-----------招商银行储蓄卡-----------登陆需要验证码----------");
					/*
					 * 输入验证码处理
					 */

					WebElement ImgExtraPwd = driver.findElement(By
							.id("ImgExtraPwd"));
					WebElement input_captcha = driver
							.findElement(By.id("ExtraPwd"));
					String imgtext = downloadImgs(driver, ImgExtraPwd);
					input_captcha.sendKeys(imgtext);
					LoginBut.click();
					Thread.sleep(5000);// 延迟三秒
				}
			}
			
			logger.warn("获取cookie之前********** ");
			// 获得cookie
			StringBuffer buffer = GetCookie(driver);
			logger.warn("获取cookie之后********** ");
			// 判断是否需要验证码
			// 需要这个加密的银行卡尽心发包
			if (!driver.getPageSource().contains("使用旧版本登入")) {
				logger.warn("*********进入登录页面**************");
				WebElement ClientNo = driver.findElement(By.id("ClientNo"));// 银行卡号，需要在页面拿到然后发包
				
				String num = ClientNo.getAttribute("value");
				if (driver.getTitle().equals("身份验证")) {
					System.out.println("************需要身份验证*************");
					// 短信发包开始-------------------
					params.put("ClientNo", num);
					params.put("PRID", "SendMSGCode");
					// 设置请求头
					headers.put("Request-Line",
							"POST /CmbBank_GenShell/UI/GenShellPC/Login/GenLoginVerifyM2.aspx HTTP/1.1");
					headers.put("Accept",
							"application/xml, text/xml, */*; q=0.01");
					headers.put("Accept-Encoding", "gzip, deflate");
					headers.put("Accept-Language", "zh-CN");
					headers.put("Cache-Control", "no-cache");
					headers.put("Connection", "Keep-Alive");
					// headers.put("Content-Length", "82");
					headers.put("Content-Type",
							"application/x-www-form-urlencoded");
					headers.put(
							"Cookie",
							buffer.toString().replaceAll("path=/,", "")
									.replaceAll("path=/", "")
									.replace("; ;", ";"));
					headers.put("Host", "pbsz.ebank.cmbchina.com");
					headers.put(
							"Referer",
							"https://pbsz.ebank.cmbchina.com/CmbBank_GenShell/UI/GenShellPC/Login/GenLoginVerifyM2.aspx");
					headers.put(
							"User-Agent",
							"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E)");
					headers.put("x-requested-with", "XMLHttpRequest");
					String rest = httclien
							.post("https://pbsz.ebank.cmbchina.com/CmbBank_GenShell/UI/GenShellPC/Login/GenLoginVerifyM2.aspx",
									params, headers);// 开始发包
					if (rest.contains("<code>00</code>")) {
						session.setAttribute(
								sessid,
								buffer.toString().replaceAll("path=/,", "")
										.replaceAll("path=/", "")
										.replace("; ;", ";"));
						params.put("Sendcode", "yes");
						params.put("ClientNo", num);
						params.put("sessid", sessid);
						map.put("errorInfo", "成功");
						map.put("errorCode", "0000");
						map.put("data", params);

					} else {
						map.put("errorInfo", "登录失败");
						map.put("errorCode", "0001");
						PushState.state(UserCard, "savings", 200,"登录失败");
					}
				} else {
					System.out.println("************不需要身份验证*************");
					// 不需要验证码
					session.setAttribute(
							sessid,
							buffer.toString().replaceAll("path=/,", "")
									.replaceAll("path=/", "")
									.replace("; ;", ";"));
					System.out.println("************不需要身份验证*************");
					params.put("ClientNo", num);
					params.put("sessid", sessid);
					params.put("Sendcode", "no");
					map.put("errorInfo", "成功");
					map.put("errorCode", "0000");
					
					map.put("data", params);

				}

			} else {
				if (elements1.getText().contains("附加码")) {
					CMBLogin(userName, userPwd, request, UserCard, UUID);

				}
				map.put("errorInfo", elements1.getText());
				map.put("errorCode", "0001");
				PushState.state(UserCard, "savings", 200, elements1.getText());
			}
		} catch (Exception e) {
			logger.warn("-----------招商银行储蓄卡-----------登陆失败----------身份证号："+UserCard,e);
		}finally{
			DriverUtil.close(driver);
		}
		logger.warn("-----------招商银行储蓄卡----------查询结束-------------返回结果："+map.toString()+"------------");
		return map;

	}

	public static StringBuffer GetCookie(WebDriver driver) {
		// 获得cookie用于发包
		Set<Cookie> cookies = driver.manage().getCookies();
		StringBuffer tmpcookies = new StringBuffer();

		for (Cookie cookie : cookies) {
			tmpcookies.append(cookie.toString() + ";");

		}
		return tmpcookies;

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

	public static byte[] takeScreenshot(WebDriver driver) throws IOException {
		WebDriver augmentedDriver = new Augmenter().augment(driver);
		return (((TakesScreenshot) augmentedDriver)
				.getScreenshotAs(OutputType.BYTES));
	}
}
