package com.reptile.winio;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.naming.directory.NoSuchAttributeException;
import javax.servlet.http.HttpSession;

import org.apache.http.ParseException;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.SessionNotFoundException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.reptile.util.CYDMDemo;
import com.reptile.util.CountTime;
import com.reptile.util.CrawlerUtil;
import com.reptile.util.DriverUtil;
import com.reptile.util.KeysPress;
import com.reptile.util.PushSocket;
import com.reptile.util.PushState;
import com.reptile.util.Resttemplate;
import com.reptile.util.SimpleHttpClient;
import com.reptile.util.application;
import com.sun.jna.NativeLibrary;

@Component
@SuppressWarnings("deprecation")
@Service("virtualKeyBoard")
public class VirtualKeyBoard {
	public static final WinIo32 winIo32 = WinIo32.INSTANCE;
	private static CYDMDemo cydmDemo = new CYDMDemo();
	Resttemplate resttemplate = new Resttemplate();
	Logger logger = Logger.getLogger(VirtualKeyBoard.class);
	static {
		if (!WinIo32.INSTANCE.InitializeWinIo()) {
			System.err.println("Cannot Initialize the WinIO");
			System.exit(1);
		}
	}

	public static void KeyDown(int key) throws Exception {
		User32Util.KBCWait4IBE();
		winIo32.SetPortVal(WinIo32.CONTROL_PORT, 0xd2, 1);
		User32Util.KBCWait4IBE();
		winIo32.SetPortVal(WinIo32.DATA_PORT, key, 1);
	}

	public static void KeyUp(int key) throws Exception {
		User32Util.KBCWait4IBE();
		winIo32.SetPortVal(WinIo32.CONTROL_PORT, 0xd2, 1);
		User32Util.KBCWait4IBE();
		winIo32.SetPortVal(WinIo32.DATA_PORT, (key | 0x80), 1);
	}

	public static void KeyPress(char key) throws Exception {
		KeyPress(VKMapping.toVK("" + key));
	}

	public static void KeyPresss(String key) throws Exception {
		KeyPress(VKMapping.toVK("" + key));
	}

	public static void KeyPress(int vk) throws Exception {
		int scan = User32.INSTANCE.MapVirtualKey(vk, 0);
		KeyDown(scan);
		KeyUp(scan);
	}

	public static void KeyPress1(int vk) throws Exception {
		int scan = User32.INSTANCE.MapVirtualKey(vk, 0);
		KeyDown(scan);
	}

	public static void KeyPress2(int vk) throws Exception {
		int scan = User32.INSTANCE.MapVirtualKey(vk, 0);
		KeyUp(scan);
	}

	/* 名声银行 */

	public synchronized Map<String, Object> CMBCLogin(String number,
			String pwd, String banktype, String idcard, String UUID,String timeCnt)
			throws Exception {
		boolean isok = CountTime.getCountTime(timeCnt);
		PushState.state(idcard, "bankBillFlow", 100);
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		PushSocket.push(map, UUID, "1000","民生银行登录中");
		WebDriver driver = null;
		try {
			logger.warn("----------------民生信用卡-------------登陆开始-----------------用户名："+number+"密码："+pwd);
			driver = DriverUtil.getDriverInstance("ie");
			driver.get("https://nper.cmbc.com.cn/pweb/static/login.html");
			WebDriverWait wait = new WebDriverWait(driver, 15);
			wait.until(ExpectedConditions.titleContains("中国民生银行个人网上银行"));
			JavascriptExecutor jss = (JavascriptExecutor) driver;

			List list = new ArrayList();
			/* 获得输入元素 */
			WebElement elements = driver.findElement(By.id("writeUserId"));
			elements.sendKeys(number);
			/* 执行换号 */
			Thread.sleep(2000);
			/* 按下Tab */
			KeysPress.SendTab("Tab");
			Thread.sleep(1000);
			/* 输入密码 */
			KeysPress.sendPassWord(pwd);

			WebElement elementss = driver.findElement(By.id("loginButton"));
			WebElement webElement = driver.findElement(By.id("_tokenImg"));
			
			if (webElement.getAttribute("src") == null
					|| "".equals(webElement.getAttribute("src"))) {
				/* 不需要验证码直接提交 */
				elementss.click();
			} else {
				/* 需要验证码进行打码 */
				logger.warn("----------------需要打印验证码----------------");
				String imgtext = downloadImg(driver, "_tokenImg");
				WebElement _vTokenId = driver.findElement(By.id("_vTokenName"));
				_vTokenId.sendKeys(imgtext);
				elementss.click();
			}
			Thread.sleep(2000);
			WebElement errorinfo = driver.findElement(By
					.className("alert-heading"));
			if (!"".equals(errorinfo.getText())) {
				if(isok==true){
					PushState.state(idcard, "bankBillFlow", 200);
				}
				map.put("errorCode", "0001");
				map.put("errorInfo", errorinfo.getText());
				driver.quit();
			} else {
				wait.until(ExpectedConditions.titleContains("中国民生银行个人网银"));
				if (driver.getTitle().contains("中国民生银行个人网银")) {
					PushSocket.push(map, UUID, "2000","民生银行登陆成功");
					if(isok==true){
						PushState.state(idcard, "bankBillFlow", 100);
					}
					logger.warn("----------------民生信用卡-------------登陆成功-----------------用户名："+number);
					Thread.sleep(2000);
					PushSocket.push(map, UUID, "5000","民生银行数据获取中");
					wait.until(ExpectedConditions.presenceOfElementLocated(By
							.className("v-binding")));
					List<WebElement> ss = driver.findElements(By
							.className("v-binding"));
					ss.get(7).click();
					Thread.sleep(2000);
					/*
					 * wait.until(ExpectedConditions.elementToBeClickable(ss.get(
					 * 7)));
					 * wait.until(ExpectedConditions.presenceOfElementLocated
					 * (By.className("ye_tb3")))
					 */

					// String jsv =
					// "var aaa=document.getElementsByClassName('v-binding');aaa[7].click();";
					// jss.executeScript( jsv, "" );

					wait.until(ExpectedConditions.presenceOfElementLocated(By
							.className("ye_tb3"))); /* 可能 */
					List<WebElement> element = driver.findElements(By
							.className("ye_tb3"));
					data.put("fixedEd", element.get(0).getText());
					
					Thread.sleep(3000);
					String js = "var aaa=document.getElementsByTagName('a');aaa[228].click();";

					jss.executeScript(js, "");
					Thread.sleep(3000);

					List<WebElement> ifs = driver.findElements(By
							.className("lanzi1"));
					wait.until(ExpectedConditions.textToBePresentInElement(
							ifs.get(2), "下载明细"));
					/* list.add(driver.getPageSource()); */

					for (int i = 0; i < 6; i++) {
						WebElement wes = driver
								.findElement(By
										.xpath("//*[@id='transView']/div/div/div/div/div/table[1]/tbody/tr/td/div[7]")); /* 先定位顶级元素 */
						wes.click();
						Thread.sleep(2000);
//						driver.manage().timeouts()
//								.implicitlyWait(20, TimeUnit.SECONDS);
						try {
							WebElement we = driver
									.findElement(By
											.xpath("//*[@id='transView']/div/div/div/div/div/table[1]/tbody/tr/td/div[8]//*[@id='"
													+ i + "']"));

							we.click();
//							driver.manage().timeouts()
//									.implicitlyWait(20, TimeUnit.SECONDS);
							Thread.sleep(2000);
							wait.until(ExpectedConditions
									.textToBePresentInElement(ifs.get(2),
											"下载明细"));
							list.add(driver.getPageSource());
						} catch (org.openqa.selenium.ElementNotVisibleException e) {
							PushSocket.push(map, UUID, "7000","民生银行数据获取失败");
							WebElement we = driver
									.findElement(By
											.xpath("//*[@id='transView']/div/div/div/div/div/table[1]/tbody/tr/td/div[8]//*[@id='"
													+ i + "']"));
							logger.warn("---------民生详情查询--------------"+we.getAttribute("title")+"账单选择问题",e);

						}
					}
					PushSocket.push(map, UUID, "6000","民生银行数据获取成功");
					logger.warn("-------------list.toString()"+"----------------------");
					data.put("html", list);
					data.put("backtype", "CMBC");
					data.put("idcard", idcard);
					map.put("data", data);
					map.put("isok", isok);
					map = resttemplate.SendMessageX(map, application.sendip
							+ "/HSDC/BillFlow/BillFlowByreditCard", idcard,UUID);

				} else {
					PushSocket.push(map, UUID, "3000","民生银行登陆失败");
					if(isok==true){
						PushState.state(idcard, "bankBillFlow", 200);
					}
					map.put("errorCode", "0001");
					map.put("errorInfo", "失败");
				}
			}
		} catch (NoSuchElementException e) {
			logger.warn(e + "民生银行出现元素没有找到");
			PushSocket.push(map, UUID, "7000","民生银行数据获取失败");
			if(isok==true){
				PushState.state(idcard, "bankBillFlow", 200);
			}
			map.put("errorCode", "0001");
			map.put("errorInfo", "网络错误");
		} catch (NoSuchFrameException e) {
			logger.warn(e + "民生银行出现iframe没有找到");
			PushSocket.push(map, UUID, "7000","民生银行数据获取失败");
			if(isok==true){
				PushState.state(idcard, "bankBillFlow", 200);
			}
			map.put("errorCode", "0001");
			map.put("errorInfo", "网络错误");
		} catch (NoSuchWindowException e) {
			logger.warn(e + "handle没有找到");
			PushSocket.push(map, UUID, "7000","民生银行数据获取失败");
			if(isok==true){
				PushState.state(idcard, "bankBillFlow", 200);
			}
			map.put("errorCode", "0001");
			map.put("errorInfo", "网络错误");
		} catch (NoSuchAttributeException e) {
			logger.warn(e + "属性错误");
			PushSocket.push(map, UUID, "7000","民生银行数据获取失败");
			if(isok==true){
				PushState.state(idcard, "bankBillFlow", 200);
			}
			map.put("errorCode", "0001");
			map.put("errorInfo", "网络错误");
		} catch (NoAlertPresentException e) {
			logger.warn(e + "没有找到alert");
			PushSocket.push(map, UUID, "7000","民生银行数据获取失败");
			if(isok==true){
				PushState.state(idcard, "bankBillFlow", 200);
			}
			map.put("errorCode", "0001");
			map.put("errorInfo", "网络错误");
		} catch (TimeoutException e) {
			logger.warn(e + "超找元素超时");
			PushSocket.push(map, UUID, "7000","民生银行数据获取失败");
			if(isok==true){
				PushState.state(idcard, "bankBillFlow", 200);
			}
			map.put("errorCode", "0001");
			map.put("errorInfo", "网络错误");
		} catch (Exception e) {
			logger.warn("民生信用卡查询失败", e);
			PushSocket.push(map, UUID, "7000","民生银行数据获取失败");
			if(isok==true){
				PushState.state(idcard, "bankBillFlow", 200);
			}
			map.put("errorCode", "0001");
			map.put("errorInfo", "网络错误");
		} finally {
			DriverUtil.close(driver);
		}
		logger.warn("------------民生信用卡-----------查询结束----------------返回信息为："+map.toString()+"-------------");
		return map;
	}

	
	/**
	 * 招商银行信用卡
	 * @param arg1
	 * @param arg2
	 * @param session
	 * @param UUID
	 * @return
	 * @throws Exception
	 */
	public synchronized Map<String, Object> Login(String arg1, String arg2,
			HttpSession session, String UUID) throws Exception {
		
		WebDriver driver = null;
		SimpleHttpClient httclien = new SimpleHttpClient();
		Map<String, Object> map = new HashMap<String, Object>(); /* 请求头 */
		try {
			logger.warn("----------------招商信用卡-------------登陆开始-----------------用户名："+arg1);
			String sessid = new CrawlerUtil().getUUID(); /* 生成UUid 用于区分浏览器 */
			HttpSession sessions = session;

			driver = DriverUtil.getDriverInstance("ie");
			driver.get("https://pbsz.ebank.cmbchina.com/CmbBank_GenShell/UI/GenShellPC/Login/Login.aspx");
			String ss1 = arg1;
			for (int i = 0; i < ss1.length(); i++) {
				KeyPress(ss1.charAt(i));
				Thread.sleep(10);
			}
			logger.warn("----------------招商信用卡-------------登陆-----------------"+VirtualKeyBoard.class.getResource("/").getPath());
			/*
			 * NativeLibrary.addSearchPath("WinIo32.dll",
			 * VirtualKeyBoard.class.getResource("/").getPath());
			 */
			NativeLibrary.addSearchPath("WinIo32", VirtualKeyBoard.class
					.getResource("/").getPath());
			Thread.sleep(1000);
			/* 按下Tab */
			KeysPress.SendTab("Tab");
			Thread.sleep(1000);
			/* 输入密码 */
			KeysPress.sendPassWord(arg2);

			Thread.sleep(300);
			WebElement elements = driver.findElement(By.id("LoginBtn"));
			elements.click();
			Thread.sleep(5000); /* 提交 */
			StringBuffer tmpcookies = Setcookie(driver); /* 设置cookie */
			map = statbank(driver, tmpcookies, sessid, sessions, elements,
					httclien, UUID);

			if (map.toString().toString().contains("0005")) {
				WebElement keyWord = driver.findElement(By.id("ImgExtraPwd"));
				String src = keyWord.getAttribute("src");
				String filename = new CrawlerUtil().getUUID();
				BufferedImage inputbig = createElementImage(driver, keyWord);
				ImageIO.write(inputbig, "png", new File("C://" + filename
						+ ".png"));
				String codenice = cydmDemo.getcode(filename); /* 识别yanzhengma */
				if (!codenice.equals("") && codenice != null) {
					WebElement keyWords = driver.findElement(By.id("ExtraPwd"));
					keyWords.sendKeys(codenice);
					elements.click();
					map = statbank(driver, tmpcookies, sessid, sessions,
							elements, httclien, UUID);
				}
			}
			
		} catch (Exception e) {
			logger.warn("----------------招商信用卡-------------登陆失败-----------------用户名："+arg1,e);
			map.put("errorInfo", "网络跑偏了,请再尝试一次");
			map.put("errorCode", "0001");
		}finally{
			DriverUtil.close(driver);
		}
		logger.warn("----------------招商信用卡-------------结束------返回信息："+map.toString());
		return (map);
	}
	
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
//		
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> data = new HashMap<String, Object>();
		PushSocket.push(map, UUID, "1000","广发银行信用卡登录中");
		boolean isok = CountTime.getCountTime(timeCnt);
		WebDriver driver = null;
		try {
			driver = DriverUtil.getDriverInstance("ie");
			WebDriverWait wait = new WebDriverWait(driver, 20);
			logger.warn("--------广发银行信用卡------------登陆开始-----------身份证号："+ usercard);
			driver.manage().window().maximize();
			JavascriptExecutor jss = (JavascriptExecutor) driver;
			driver.get("https://ebanks.cgbchina.com.cn/perbank/");
			wait.until(ExpectedConditions.presenceOfElementLocated(By
					.linkText("广发银行官方网站")));
			WebElement elements = driver.findElement(By.id("loginId"));
			elements.click();
			elements.sendKeys(number);
			Thread.sleep(1000);
			/* 按下Tab */
			KeysPress.SendTab("Tab");
			Thread.sleep(1000);
			/* 输入密码 */
			KeysPress.sendPassWord(pwd);

			String imgtext = downloadImgs(driver, "verifyImg");
			logger.warn("--------广发银行信用卡--------登陆-----------验证码打码为："+imgtext+"-------------");
			if (imgtext.contains("超时") || imgtext.equals("")) {
				map.put("errorInfo", "查询失败");
				map.put("errorCode", "0002");
				PushSocket.push(map, UUID, "3000","广发银行信用卡登陆失败");
				if(isok==true){
					PushState.state(usercard, "bankBillFlow", 200);
				}
				logger.warn("--------广发银行信用卡--------------登陆失败---------身份证号："+ usercard+"--------返回信息为："+map);
				return map;
			}
			WebElement _vTokenId = driver.findElement(By.id("captcha"));
			_vTokenId.sendKeys(imgtext);
			WebElement loginButton = driver.findElement(By.id("loginButton"));
			loginButton.click(); /* 点击登陆 */
			//弹窗的内容
			String str = DriverUtil.alertFlag(driver);
			if(!str.isEmpty()){
				if(str.contains("验证码")){
					map = GDBLogin(number, pwd, usercard, UUID,timeCnt);
				}else{
					map.put("errorInfo", str);
					map.put("errorCode", "0001");
					PushSocket.push(map, UUID, "3000","广发银行信用卡登陆失败");
					if(isok==true){
						PushState.state(usercard, "bankBillFlow", 200);
					}
					logger.warn("--------广发银行登陆------------失败-----------用户名："+ number+"--------原因为："+str);
				}
				return map;
			}else if(DriverUtil.waitById("errorMessage", driver, 3)){
				String errorMessage = driver.findElement(By.id("errorMessage")).getText();
				map.put("errorInfo", errorMessage);
				map.put("errorCode", "0001");
				PushSocket.push(map, UUID, "3000","广发银行信用卡登陆失败");
				if(isok==true){
					PushState.state(usercard, "bankBillFlow", 200);
				}
				logger.warn("--------广发银行登陆------------失败-----------用户名："+ number+"--------原因为："+errorMessage);
				return map;
			}else if(DriverUtil.waitByTitle("广发银行个人网上银行", driver, 15)){
				logger.warn("--------广发银行登陆------------成功-----------用户名："+ number+"-----------");
				PushSocket.push(map, UUID, "2000","广发银行信用卡登陆成功");
				if(isok==true){
					PushState.state(usercard, "bankBillFlow", 100);
				}
				Thread.sleep(8000);
				PushSocket.push(map, UUID, "5000","广发银行信用卡数据获取中");
				String jsv = "var aaa=document.getElementsByClassName('node');aaa[15].click();";
				jss.executeScript(jsv, "");
				
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				
				String head = driver.getWindowHandle();
				
				String sid = driver.getPageSource()
						.substring(driver.getPageSource().indexOf("_emp_sid = '"),driver.getPageSource().indexOf("';"))
						.replaceAll("_emp_sid = '", "");
				logger.warn("--------广发银行登陆------------sid为："+sid);
				/*选中第一个*/
				for (int i = 0; i < 11; i++) {
					/* 为了避免查询不到账单 做此次处理 */
					List<WebElement> elements2 = driver.findElements(By
							.tagName("IFRAME"));
					driver.switchTo().frame(elements2.get(0));
					if(driver.getPageSource().contains("上月账单")){
						break;
					}
					//判断creditCardNo是否可点击
					if(DriverUtil.waitById("creditCardNo", driver, 15) && DriverUtil.clickById("creditCardNo", driver, 15)){
						driver.findElement(By.id("creditCardNo")).click();;
					}
					//判断cross是否可点击
					if(DriverUtil.waitByClassName("cross", driver, 15) && DriverUtil.clickByClassName("cross", driver, 15)){
						driver.findElement(By.className("cross")).click();;
					}
					new Select(driver.findElement(By.id("billDate"))).selectByIndex(i);
					driver.findElement(By.linkText("查询")).click();
					Thread.sleep(2000);
					driver.switchTo().window(head);
				}
				
				logger.warn("--------广发银行登陆------------OPEN：开始");
				for (int i = 0; i < application.Getdate().size(); i++) {
					Thread.sleep(1000);
					String win = "window.open('https://ebanks.cgbchina.com.cn/perbank/CR1080.do?currencyType=&creditCardNo="
							+ number
							+ "&billDate="
							+ application.Getdate().get(i)
							+ "&billType=1&abundantFlag=0&terseFlag=0&showWarFlag=0&EMP_SID="
							+ sid + " ');";
					logger.warn("--------广发银行登陆------------OPEN："+i+"    url:"+win);
					jss.executeScript(win, "");
				}
				logger.warn("--------广发银行登陆------------OPEN：结束");
				Set<String> jswin = driver.getWindowHandles();
				logger.warn("--------广发银行登陆------------账单查询：开始");
				List<String> listinfo = new ArrayList<String>();
				for (String item : jswin) {
					if (!item.equals(head)) {
						driver.switchTo().window(item);
						if(DriverUtil.waitByTitle("账单", driver, 8)){
							logger.warn("-----------账单信息：-------------"+driver.getPageSource());
							if (!driver.getPageSource().contains("var billErrMsg = '该月份账单尚未生成，请于账单日后再查询';")) {
								logger.warn("--------广发银行登陆------------账单查询：具体内容："+driver.getPageSource());
								listinfo.add(driver.getPageSource());
							}
						}
						
					}
				}
				PushSocket.push(map, UUID, "6000","广发银行信用卡数据获取成功");
				logger.warn("--------广发银行登陆------------账单查询：结束");
				data.put("html", listinfo);
				data.put("backtype", "GDB");
				data.put("idcard", usercard);
				map.put("data", data);
				map.put("isok", isok);
				Resttemplate ct = new Resttemplate();
				map = ct.SendMessageX(map, application.sendip
						+ "/HSDC/BillFlow/BillFlowByreditCard", usercard,UUID);
				driver.switchTo().window(head);
				}
			
		} catch (Exception e) {
			logger.warn("-----------广发银行查询失败----------",e);
			map.put("errorInfo", "网络异常,请重试！！");
			map.put("errorCode", "0001");
			PushSocket.push(map, UUID, "7000","广发银行信用卡数据获取失败");
			if(isok==true){
				PushState.state(usercard, "bankBillFlow", 200);
			}
		}finally{
			DriverUtil.close(driver);
		}
		logger.warn("--------------广发银行信用卡------------查询结束-----------返回信息为："+map+"---------------");
		return (map);
	}

	/* 执行过程 */
	public Map statbank(WebDriver driver, StringBuffer tmpcookies,
			String sessid, HttpSession sessions, WebElement elements,
			SimpleHttpClient httclien, String UUID) throws ParseException,
			IOException, InterruptedException {
		Map<String, Object> params = new HashMap<String, Object>(); /* 参数 */
		Map<String, String> headers = new HashMap<String, String>(); /* 请求头 */
		Map<String, Object> map = new HashMap<String, Object>(); /* 请求头 */
		Map<String, Object> data = new HashMap<String, Object>(); /* 请求头 */
		if (!driver.getPageSource().contains("使用旧版本登入")) {
			/* 证明登陆成功 */
			WebElement ClientNo = driver.findElement(By.id("ClientNo")); /*
																		 * 银行卡号，
																		 * 需要在页面拿到然后发包
																		 */
			String num = ClientNo.getAttribute("value");

			System.out.println("num" + num);
			if (driver.getTitle().equals("身份验证")) {
				data.put("Verify", "yes");
				params.put("ClientNo", num);
				params.put("PRID", "SendMSGCode");
				/* 设置请求头 */
				headers.put("Request-Line",
						"POST /CmbBank_GenShell/UI/GenShellPC/Login/GenLoginVerifyM2.aspx HTTP/1.1");
				headers.put("Accept", "application/xml, text/xml, */*; q=0.01");
				headers.put("Accept-Encoding", "gzip, deflate");
				headers.put("Accept-Language", "zh-CN");
				headers.put("Cache-Control", "no-cache");
				headers.put("Connection", "Keep-Alive");
				/* headers.put("Content-Length", "82"); */
				headers.put("Content-Type", "application/x-www-form-urlencoded");
				headers.put("Cookie",
						tmpcookies.toString().replaceAll("path=/,", "")
								.replaceAll("path=/", ""));
				headers.put("Host", "pbsz.ebank.cmbchina.com");
				headers.put(
						"Referer",
						"https://pbsz.ebank.cmbchina.com/CmbBank_GenShell/UI/GenShellPC/Login/GenLoginVerifyM2.aspx");
				headers.put(
						"User-Agent",
						"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E)");
				headers.put("x-requested-with", "XMLHttpRequest");
				driver.quit();
				String rest = httclien
						.post("https://pbsz.ebank.cmbchina.com/CmbBank_GenShell/UI/GenShellPC/Login/GenLoginVerifyM2.aspx",
								params, headers); /* 开始发包 */
				if (rest.contains("<code>00</code>")) {
					map.put("errorCode", "0000");
					map.put("errorInfo", "成功");
				
				} else {
					map.put("errorCode", "0001");
					map.put("errorInfo", "验证码发送失败");
				}
			} else {
				data.put("Verify", "no");
				map.put("errorCode", "0000");
				/* map.put("errorinfo", "操作成功"); //原始数据 */
				map.put("errorInfo", "操作成功"); /* 2017年10月12日19:14 刘彬修改 */
			}

			/*  */
			/* 返回cookie 用于查询数据 */
			sessions.setAttribute(
					sessid,
					tmpcookies.toString().replaceAll("path=/,", "")
							.replaceAll("path=/", ""));
			data.put("ClientNo", num);
			data.put("sessids", sessid);
		} else {
			WebElement elements1 = driver.findElement(By
					.className("page-form-item"));
			if (elements1.getText().contains("附加码")) {
				/* map.put("errorInfo","出现验证码了"); */
				map.put("errorInfo", "异常登陆,请重试");
				map.put("errorCode", "0005");
			} else {
				if (elements1.getText().contains("开户地")) {
					map.put("errorInfo", "请重试");
					map.put("errorCode", "0001");
				} else if (elements1.getText().contains("请输入一网通、一卡通、信用卡、存折账号")) {
					map.put("errorInfo", "目前网络较差，请重试");
					map.put("errorCode", "0001");
				} else {
					map.put("errorInfo", elements1.getText());
					map.put("errorCode", "0001");
				}
			}
		}

		map.put("data", data);
		return map;
	}
	
	/* 设置cookie */

	public StringBuffer Setcookie(WebDriver driver) {
		/* 获得cookie用于发包 */
		Set<Cookie> cookies = driver.manage().getCookies();
		StringBuffer tmpcookies = new StringBuffer();

		for (Cookie cookie : cookies) {
			tmpcookies.append(cookie.toString() + ";");
		}
		return (tmpcookies);
	}

	public static BufferedImage createElementImage(WebDriver driver,
			WebElement webElement) throws IOException {
		/* 获得webElement的位置和大小。 */
		Point location = webElement.getLocation();
		Dimension size = webElement.getSize();
		/* 创建全屏截图。 */
		BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(
				takeScreenshot(driver)));
		/* 截取webElement所在位置的子图。 */
		BufferedImage croppedImage = originalImage.getSubimage(location.getX(),
				location.getY(), size.getWidth() - 40, size.getHeight());
		return (croppedImage);
	}

	public static BufferedImage createElementImagepufa(WebDriver driver,
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
		/*
		 * TakesScreenshot takesScreenshot = (TakesScreenshot) driver; return
		 * takesScreenshot.getScreenshotAs(OutputType.BYTES);
		 */
	}

	/*
	 * public static void main(String args[]) throws IOException{
	 * System.setProperty("webdriver.ie.driver", "D:/ie/IEDriverServer.exe");
	 * WebDriver driver = new InternetExplorerDriver();
	 * driver.get("https://sn.ac.10086.cn/login");
	 * 
	 * 
	 * 
	 * } //
	 */

	/* 浦发银行 */
	public synchronized Map<String, Object> pufaLogin(String arg1, String arg2,
			HttpSession session, String UUID) throws Exception {
		WebDriver driver = null;
		SimpleHttpClient httclien = new SimpleHttpClient();
		Map<String, Object> map = new HashMap<String, Object>(); /* 请求头 */
		Map<String, Object> data = new HashMap<String, Object>(); /* 请求头 */
		try {
			String sessid = new CrawlerUtil().getUUID(); /* 生成UUid 用于区分浏览器 */
			HttpSession sessions = session;

			System.setProperty("webdriver.ie.driver",
					"D:/ie/IEDriverServer.exe");
			driver = new InternetExplorerDriver();
			driver.get("https://pbsz.ebank.cmbchina.com/CmbBank_GenShell/UI/GenShellPC/Login/Login.aspx");
			String ss1 = arg1;
			for (int i = 0; i < ss1.length(); i++) {
				KeyPress(ss1.charAt(i));
				Thread.sleep(10);
			}
			NativeLibrary.addSearchPath("WinIo32", VirtualKeyBoard.class
					.getResource("/").getPath());
			Thread.sleep(100);
			String s = "Tab"; /*  */
			KeyPresss(s);
			Thread.sleep(20);
			String ss = arg2; /*  */
			for (int i = 0; i < ss.length(); i++) {
				KeyPress(ss.charAt(i));
				Thread.sleep(10);
			}
			KeyPresss(s);
			Thread.sleep(300);
			WebElement elements = driver.findElement(By.id("LoginBtn"));
			elements.click();
			Thread.sleep(5000); /* 提交 */
			StringBuffer tmpcookies = Setcookie(driver); /* 设置cookie */
			map = statbank(driver, tmpcookies, sessid, sessions, elements,
					httclien, UUID);

			if (map.toString().toString().contains("0005")) {
				WebElement keyWord = driver.findElement(By.id("ImgExtraPwd"));
				String src = keyWord.getAttribute("src");
				String filename = new CrawlerUtil().getUUID();
				BufferedImage inputbig = createElementImage(driver, keyWord);
				ImageIO.write(inputbig, "png", new File("C://" + filename
						+ ".png"));
				String codenice = cydmDemo.getcode(filename); /* 识别yanzhengma */
				if (!codenice.equals("") && codenice != null) {
					WebElement keyWords = driver.findElement(By.id("ExtraPwd"));
					keyWords.sendKeys(codenice);
					elements.click();
					map = statbank(driver, tmpcookies, sessid, sessions,
							elements, httclien, UUID);
				}
			}

			try {
				driver.quit();
			} catch (SessionNotFoundException e) {
				/* TODO: handle exception */
			}
		} catch (Exception e) {
			map.put("errorInfo", "网络跑偏了,请再尝试一次");
			map.put("errorCode", "0001");
			driver.quit();
		}

		return (map);
	}

	/* 中国银行 */
	public synchronized Map<String, Object> chinaBank(String arg1, String arg2,
			HttpSession session, String UUID) throws Exception {
		WebDriver driver = null;
		SimpleHttpClient httclien = new SimpleHttpClient();
		Map<String, Object> map = new HashMap<String, Object>(); /* 请求头 */
		Map<String, Object> data = new HashMap<String, Object>(); /* 请求头 */
		try {
			String sessid = new CrawlerUtil().getUUID(); /* 生成UUid 用于区分浏览器 */
			HttpSession sessions = session;

			System.setProperty("webdriver.ie.driver",
					"D:/ie/IEDriverServer.exe");
			driver = new InternetExplorerDriver();
			driver.get("https://pbsz.ebank.cmbchina.com/CmbBank_GenShell/UI/GenShellPC/Login/Login.aspx");
			String ss1 = arg1;
			for (int i = 0; i < ss1.length(); i++) {
				KeyPress(ss1.charAt(i));
				Thread.sleep(10);
			}
			NativeLibrary.addSearchPath("WinIo32", VirtualKeyBoard.class
					.getResource("/").getPath());
			Thread.sleep(100);
			String s = "Tab"; /*  */
			KeyPresss(s);
			Thread.sleep(20);
			String ss = arg2; /*  */
			for (int i = 0; i < ss.length(); i++) {
				KeyPress(ss.charAt(i));
				Thread.sleep(10);
			}
			KeyPresss(s);
			Thread.sleep(300);
			WebElement elements = driver.findElement(By.id("LoginBtn"));
			elements.click();
			Thread.sleep(5000); /* 提交 */
			StringBuffer tmpcookies = Setcookie(driver); /* 设置cookie */
			map = statbank(driver, tmpcookies, sessid, sessions, elements,
					httclien, UUID);

			if (map.toString().toString().contains("0005")) {
				WebElement keyWord = driver.findElement(By.id("ImgExtraPwd"));
				String src = keyWord.getAttribute("src");
				String filename = new CrawlerUtil().getUUID();
				BufferedImage inputbig = createElementImage(driver, keyWord);
				ImageIO.write(inputbig, "png", new File("C://" + filename
						+ ".png"));
				String codenice = cydmDemo.getcode(filename); /* 识别yanzhengma */
				if (!codenice.equals("") && codenice != null) {
					WebElement keyWords = driver.findElement(By.id("ExtraPwd"));
					keyWords.sendKeys(codenice);
					elements.click();
					map = statbank(driver, tmpcookies, sessid, sessions,
							elements, httclien, UUID);
				}
			}

		} catch (Exception e) {
			map.put("errorInfo", "网络跑偏了,请再尝试一次");
			map.put("errorCode", "0001");
			driver.quit();
		}

		return (map);
	}

	public static String downloadImg(WebDriver driver, String id)
			throws IOException {
		WebElement keyWord = driver.findElement(By.id(id));
		String src = keyWord.getAttribute("src");
		String filename = new CrawlerUtil().getUUID();
		BufferedImage inputbig = createElementImagepufa(driver, keyWord);
		ImageIO.write(inputbig, "png", new File("C://" + filename + ".png"));
		String codenice = cydmDemo.getcode(filename); /* 识别yanzhengma */
		return (codenice);
	}

	public static String downloadImgs(WebDriver driver, String id)
			throws IOException {
		WebElement keyWord = driver.findElement(By.id(id));
		String src = keyWord.getAttribute("src");
		String filename = new CrawlerUtil().getUUID();
		BufferedImage inputbig = createElementImages(driver, keyWord);
		ImageIO.write(inputbig, "png", new File("C://" + filename + ".png"));
		String codenice = cydmDemo.getcode(filename); /* 识别yanzhengma */
		return (codenice);
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
	
    /**
     * 向input里输入值str
     * @param str 输入的字符串
     * @throws Exception 
     */
    public static void inputCode(String str) throws Exception{
    	Thread.sleep(200);
    	for (int i = 0; i < str.length(); i++) {
    		   KeyPress(str.charAt(i));
    				Thread.sleep(50);
    			}
    		
    }
}
