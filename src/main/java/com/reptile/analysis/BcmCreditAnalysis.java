package com.reptile.analysis;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import com.reptile.util.CountTime;
import com.reptile.util.DriverUtil;
import com.reptile.util.JiaoTongKeyMap;
import com.reptile.util.MyCYDMDemo;
import com.reptile.util.OCRHelper;
import com.reptile.util.PushSocket;
import com.reptile.util.PushState;
import com.reptile.util.Resttemplate;
import com.reptile.util.SimpleHttpClient;
import com.reptile.util.application;

/**
 * 
 * @author Bigyoung
 * @version v1.0
 * @deprecated 交通信用卡
 * @date 2017年11月16日10:51:14
 */
@Service
public class BcmCreditAnalysis {

	private static Logger logger = Logger.getLogger(BcmCreditAnalysis.class);

	Resttemplate resttemplate = new Resttemplate();

	public Map<String, Object> BankLogin(String UserNumber, String UserPwd,
			String UserCard, HttpServletRequest request, String UUID,String timeCnt) {
		int flag = 0;
	
		Map<String, Object> map = new HashMap<String, Object>();
		System.setProperty("webdriver.chrome.driver", "C:/ie/chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		ChromeDriver driver = new ChromeDriver(options);
		options.addArguments("start-maximized");
		boolean isok = true; 
		try {
			isok =CountTime.getCountTime(timeCnt);
			JavascriptExecutor jss = null;
			PushSocket.push(map, UUID, "1000","交通银行信用卡登录中");
			if(isok==true){
				PushState.state(UserCard, "bankBillFlow", 100);
			}
			flag = 1;
			
			logger.warn("--------------交通银行信用卡---------------登陆开始----------------身份证号："+UserCard);
			// 开始执行任务
			driver.get("https://creditcardapp.bankcomm.com/idm/sso/login.html?service=https://creditcardapp.bankcomm.com/member/shiro-cas");
			driver.findElement(By.className("close_overlay")).click();
			

			driver.findElementById("cardNo").sendKeys(UserNumber);
			driver.findElement(By.id("cardpassword")).click();
			Thread.sleep(3000);
			WebElement element = driver.findElement(By.className("key-pop"));
			
			Thread.sleep(500);
//			Map<String, Object> imagev = new HashMap<String, Object>();
			String icbcImg1 = saveImgJ(element, driver);// 返回键盘中数字
//			String icbcImg1 = imagev.get("strResult").toString();// 读取图片验证码
			
			
			logger.warn("-------------键盘打码结果----------------icbcImg1:"+icbcImg1);
			String[] split = icbcImg1.split("");// 将数字字符串分割为数组
			List<WebElement> li = element.findElements(By.tagName("li"));
			// 返回识别的字符串
			String pwd = UserPwd;
			System.out.println("**************"+UserPwd+"*****************");
			String[] pwdArry = pwd.split("");
			logger.warn("-------------输入密码分割的字符串----------------pwdArry:"+pwdArry.toString());
			boolean isNum = true;
			//记录输入密码框中字符个数
			int count = 0;
			for (int i = 0; i < pwdArry.length; i++) { // 读取密码的每一位数字，循环找出键盘中对应的数字下标
				String num = pwdArry[i];				
				for (int j = 0; j < split.length; j++) {
					logger.warn("-------------判断被分割登录密码单个字符是否为数字----------------");
					isNum = BcmCreditAnalysis.isInteger(num);
					if(isNum) {
						logger.warn("-------------登录密码数字部分----------------num:"+num);
						if (num.equals(split[j])) { // 数字部分
							li.get(j).click();
							System.out.println("***********"+li.get(j));
							Thread.sleep(500);
							count = count+1;
							break;
						}
					}else {
//						if (j == split.length - 1) { // 字符部分
							logger.warn("-----------------登录密码字符部分----------------num:"+num);
							int integer = JiaoTongKeyMap.map.get(num);
							li.get(integer).click();
							System.out.println("*********"+li.get(j));
							Thread.sleep(500);
							count = count+1;
							break;
//						}
					}
					
					
				}
			}
			logger.warn("-------------------密码框中输入字符个数count:"+count);
			if(count!=UserPwd.length()) {
				logger.warn("-----------交通信用卡-----------登录失败-----------");
				PushSocket.push(map, UUID, "3000","密码输入中出错,登录失败");
				if(isok==true){
					PushState.state(UserCard, "bankBillFlow", 200,"密码输入中出错,登录失败");
				}else {
					PushState.stateX(UserCard, "bankBillFlow", 200,"密码输入中出错,登录失败");
				}
				map.put("errorCode", "0001");
				map.put("errorInfo", "密码输入中出错,登录失败");
				DriverUtil.close(driver);
				return map;
			}
			// 触发登录按钮
			driver.findElementById("cardNo").click();
			Thread.sleep(3000);
			jss = (JavascriptExecutor) driver;
			jss.executeScript("$('#cardLogin').click();", "");
			// driver.findElement(By.id("loginBtn")).click();
			Thread.sleep(4000);
			
			
			
			String currentWindow = driver.getWindowHandle();
			//监听是否有弹窗
			String alertText = "";
			try {
				alertText = driver.switchTo().alert().getText();
				driver.switchTo().window(currentWindow);
			} catch (Exception e) {
				driver.switchTo().window(currentWindow);
			}
	
			// 监听是否有错误信息
			List<WebElement> msg = null;
			try {
				msg = driver.findElements(By.className("errormsg"));
			} catch (Exception e) {
				msg = new ArrayList<WebElement>();
			}
					
			if (alertText.equals("") && msg.size() == 0) {					
				// if(driver.findElement(By.id("moibleMessages"))!=null){
				// 识别是否需要发送验证码
				if (driver.getPageSource().contains("moibleMessages")) {
					// 需要发送验证码
					System.out.println("开始发送验证码");
					
					map.put("whetherCode", "no");
					
					logger.warn(UserCard + "此帐号在登录时需要验证码，可能帐号出现异常 需要自行登录 才可认证");
					PushSocket.push(map, UUID, "3000","帐号认证异常，请你先尝试在官网登录");
					if(isok==true){
						PushState.state(UserCard, "bankBillFlow", 200,"帐号认证异常，请你先尝试在官网登录");
					}else {
						PushState.stateX(UserCard, "bankBillFlow", 200,"帐号认证异常，请你先尝试在官网登录");
					}
					map.put("whetherCode", "no");
					map.put("errorCode", "0004");// 认证失败
					map.put("errorInfo", "帐号认证异常，请你先尝试在官网登录");
					DriverUtil.close(driver);
					return map;
					// 不需要发送验证码
				} else if (driver.getPageSource().contains("查看我的买单吧")) {
					logger.warn("--------------交通银行信用卡---------------登陆成功----------------身份证号："+UserCard);
					PushSocket.push(map, UUID, "2000","交通银行信用卡登陆成功");
					
					Thread.sleep(2000);
					PushSocket.push(map, UUID, "5000","交通银行信用卡数据获取中");
					flag = 2;
					List<String> list=new ArrayList<String>();
//						list = this.getDetail(driver, UserNumber);
						driver.executeScript(
								"javascript:gotToLink('/member/member/service/billing/detail.html');",
								0);
						WebDriverWait wait = new WebDriverWait(driver, 20);
						wait.until(ExpectedConditions.presenceOfElementLocated(By
								.id("bill_date")));
						for (int i = 0; i < 5; i++) {
							Select sel = new Select(driver.findElement(By
									.id("bill_date")));
							sel.selectByIndex(i);
							WebElement elements = driver.findElement(By
									.xpath("//*[@id='bill_content']/p/a"));
							elements.click();
							Thread.sleep(2000);
							String pageSource = driver.getPageSource();
							if(pageSource.contains("系统忙")) {
								String jsv = "window.location.reload();";
								jss = (JavascriptExecutor) driver;
								jss.executeScript(jsv, "");
								Thread.sleep(1000);
								pageSource = driver.getPageSource();
							}							
							System.out.println(pageSource);							
							list.add(pageSource);
							wait.until(ExpectedConditions
									.presenceOfElementLocated(By.className("goback")));
							WebElement goback = driver.findElement(By
									.className("goback"));
							goback.click();
							Thread.sleep(2000);
							String pageSource1 = driver.getPageSource();
							if(pageSource1.contains("系统忙")) {
								String jsv = "window.location.reload();";
								jss = (JavascriptExecutor) driver;
								jss.executeScript(jsv, "");
								Thread.sleep(1000);
								pageSource = driver.getPageSource();
							}
							wait.until(ExpectedConditions
									.presenceOfElementLocated(By.id("bill_date")));
						}

					
						List<Map<String, Object>> infoData = new ArrayList<Map<String, Object>>();
						PushSocket.push(map, UUID, "6000","交通银行信用卡数据获取成功");
						flag = 3;
			            infoData = getInfos(list,infoData);
			            Map<String, Object> bankList = new HashMap<String, Object>();
			            bankList.put("bankList", infoData);
			            map.put("data", bankList);
						map.put("backtype", "BCM");
						map.put("idcard", UserCard);
						map.put("bankname", "交通");
						map.put("userAccount", UserNumber);
						map.put("isok", isok);
						// map= resttemplate.SendMessage(map,
						// "http://192.168.3.16:8089/HSDC/BillFlow/BillFlowByreditCard",UserCard);
						logger.warn("map :"+map.toString());
						map = resttemplate.SendMessageX(map, application.sendip
								+ "/HSDC/BillFlow/BillFlowByreditCard", UserCard,UUID);

						map.put("whetherCode", "no");
						DriverUtil.close(driver);
					
					
					logger.warn("--------------交通银行信用卡---------------查询成功----------------身份证号："+UserCard);
				} else {
					logger.warn("--------------交通银行信用卡-------------请求数据时错误--------------身份证号："+UserCard+"请求数据时错误");
					PushSocket.push(map, UUID, "3000","请求数据时错误");
					if(isok==true){
						PushState.state(UserCard, "bankBillFlow", 200,"请求数据时错误");
					}else {
						PushState.stateX(UserCard, "bankBillFlow", 200,"请求数据时错误");
					}
					map.put("errorCode", "0001");// 认证失败
					map.put("errorInfo", "请求数据时错误");
					DriverUtil.close(driver);
					return map;
				}
				
			} else if(!alertText.isEmpty()){
				logger.warn("--------------交通银行信用卡---------------登陆失败----------------身份证号："+UserCard+"失败原因："+alertText);
				PushSocket.push(map, UUID, "3000",alertText);
				if(isok==true){
					PushState.state(UserCard, "bankBillFlow", 200,alertText);
				}else {
					PushState.stateX(UserCard, "bankBillFlow", 200,alertText);
				}
				map.put("errorCode", "0001");
				map.put("errorInfo", alertText);
				DriverUtil.close(driver);
			} else if(msg.size() != 0){
				logger.warn("--------------交通银行信用卡---------------登陆失败----------------身份证号："+UserCard+"失败原因："+msg.get(1).getText());
				PushSocket.push(map, UUID, "3000",msg.get(1).getText());
				if(isok==true){
					PushState.state(UserCard, "bankBillFlow", 200,msg.get(1).getText());
				}else {
					PushState.stateX(UserCard, "bankBillFlow", 200,msg.get(1).getText());
				}
				map.put("errorCode", "0001");
				map.put("errorInfo", msg.get(1).getText());
				DriverUtil.close(driver);
			}
		}catch (Exception e) {
			e.printStackTrace();
			logger.warn(e + "网络异常");
			if(flag == 1) {
				logger.warn("--------------flag="+flag+"----------网络异常，登陆失败");
				PushSocket.push(map, UUID, "3000","网络异常");					
			}else if(flag == 2) {
				logger.warn("--------------flag="+flag+"----------网络异常，数据获取失败");
				PushSocket.push(map, UUID, "7000","网络异常");						
			}else if(flag == 3) {
				logger.warn("--------------flag="+flag+"----------网络异常，认证失败");
				PushSocket.push(map, UUID, "9000","网络异常");						
			}
			if(isok==true){
				PushState.state(UserCard, "bankBillFlow", 200,"网络异常");
			}else {
				PushState.stateX(UserCard, "bankBillFlow", 200,"网络异常");
			}
			map.put("errorCode", "0001");
			map.put("errorInfo", "网络错误");
			DriverUtil.close(driver);
			
		}
			
			logger.warn("--------------交通银行信用卡---------------返回信息为："+map);
			return map;

		} 
	/**
	 * 数据中心新接口对接
	 * @param list
	 * @param infoData
	 * @return
	 */
	
	public static List<Map<String, Object>> getInfos(List<String> list,List<Map<String, Object>> infoData){
		for (String info : list) {						
			Map<String, Object> bankList = new HashMap<String, Object>();
			logger.warn("info*****************"+info);
			Map<String, Object> accountSummary = new HashMap<String, Object>();
			Document doc = Jsoup.parse(info);
			String zhangdanri = doc.getElementsByClass("bill-date").get(0).text();
			String StatementDate = zhangdanri.substring(18);
			accountSummary.put("StatementDate", StatementDate);//账单日		
			Element bill = doc.getElementsByClass("bill-list").get(0);
			accountSummary.put("PaymentDueDate", bill.getElementsByTag("tr").get(0).getElementsByTag("td").get(0).text());
			accountSummary.put("RMBCurrentAmountDue", bill.getElementsByTag("tr").get(1).getElementsByTag("td").get(0).text().trim().replace("¥", ""));
			accountSummary.put("RMBMinimumAmountDue", bill.getElementsByTag("tr").get(2).getElementsByTag("td").get(0).text().trim().replace("¥", ""));
			accountSummary.put("CreditLimit", bill.getElementsByTag("tr").get(3).getElementsByTag("td").get(0).text().trim().replace("¥", ""));
			Element billInfo = doc.getElementById("bill-1");
			Elements ddList = billInfo.getElementsByTag("dd");
			List<Object> payRecordList = new ArrayList<Object>();
			if(ddList != null) {
				for (Element element : ddList) {
					Map<String, Object> payRecord = new HashMap<String, Object>();
					payRecord.put("tran_date", element.getElementsByTag("span").get(0).text().replace("/", ""));
					payRecord.put("tran_desc", element.getElementsByTag("span").get(2).text().replace("/", ""));
					payRecord.put("post_amt", element.getElementsByTag("span").get(3).text().replace("/", ""));
					payRecordList.add(payRecord);//每月账单明细
				}
			}
			
			bankList.put("payRecord", payRecordList);
			bankList.put("AccountSummary", accountSummary);
			infoData.add(bankList);
		}
		
		return infoData;
	}
	
	
	public static boolean isInteger(String str) {    
	    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");    
	    return pattern.matcher(str).matches();    
	  }
	/**
	 * 获取账单详情
	 * @param driver
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws InterruptedException 
	 */
	public List<String> getDetail(WebDriver driver ,String userNumber) throws ClientProtocolException, IOException, InterruptedException{
		String cookie = this.getCookie(driver);
		userNumber  = userNumber.substring(0, 4)+"%20****%20****%20"+userNumber.substring(userNumber.length()-4);
		Map<String,String> headers = new HashMap<String, String>();
		headers.put("Cookie", cookie);
		headers.put("Connection", "keep-alive");
		headers.put("Host", "creditcardapp.bankcomm.com");
		String str = SimpleHttpClient.get("https://creditcardapp.bankcomm.com/member/member/service/billing/detail.html?cardNo="+userNumber,headers);
		Thread.sleep(2000);
		List<String> dates = this.parseDates(str);
		List<String> list = new ArrayList<String>();
		
			for (String date:dates) {
				if(date.contains("本期")){
					date = date.replace(" (本期)", "");
				}
				headers.clear();
				headers.put("Cookie", cookie);
				headers.put("Connection", "keep-alive");
				headers.put("Host", "creditcardapp.bankcomm.com");
				Thread.sleep(1000);
				String str1 = SimpleHttpClient.get("https://creditcardapp.bankcomm.com/member/member/service/billing/finished.html?cardNo="+userNumber+"&billDate="+date,headers);
				
				if(str1.contains("系统忙")&&str1.contains("请稍后再试")){
					continue;
				}
				
				list.add(str1);
			}
		return list;
	}	

	
	/**
     * 解析table
     *
     * @param xml
     * @return
     */
    private  List<String> parseDates(String xml) {

        Document doc = Jsoup.parse(xml);
        Elements options = doc.select("select").select("option");

        List<String> list = new ArrayList<String>();
        for (Element item : options) {
            String txt = item.text();
            if (!txt.equals("")) {
                list.add(txt);
            }
        }

        return list;
    }
	

    public  String getCookie(WebDriver driver)		{
		  //获得cookie用于发包
		Set<org.openqa.selenium.Cookie> cookies = driver.manage().getCookies();  
	    StringBuffer tmpcookies = new StringBuffer();

	   	for (org.openqa.selenium.Cookie cookie : cookies) {
	   		String name = cookie.getName();
	   		String value = cookie.getValue();
 			tmpcookies.append(name + "="+ value + ";");
		}
	   	String str = tmpcookies.toString();
	   	if(!str.isEmpty()){
	   		str = str.substring(0,str.lastIndexOf(";"));
	   	}
		return str; 	
	}

	public static StringBuffer Setcookie(WebDriver driver) {
		// 获得cookie用于发包
		Set<Cookie> cookies = driver.manage().getCookies();
		StringBuffer tmpcookies = new StringBuffer();

		for (Cookie cookie : cookies) {
			tmpcookies.append(cookie.toString() + ";");

		}
		return tmpcookies;

	}

	public static StringBuffer Setcookies(WebDriver driver) {
		// 获得cookie用于发包
		Set<Cookie> cookies = driver.manage().getCookies();
		StringBuffer tmpcookies = new StringBuffer();

		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("JSESSIONID")
					|| cookie.getName().equals("member_session_sticky")
					|| cookie.getName().equals("_tcs")
					|| cookie.getName().equals("t0")
					|| cookie.getName().equals("IDMAUTH")
					|| cookie.getName().equals("loginType")
					|| cookie.getName().equals("CHANNEL")) {
				tmpcookies.append(cookie.toString());
			}

		}
		return tmpcookies;

	}

	public static String getICBCImg(WebElement element, WebDriver driver) {
		if (element == null)
			throw new NullPointerException("图片元素失败");
		WrapsDriver wrapsDriver = (WrapsDriver) element; // 截取整个页面
		File scrFile = ((TakesScreenshot) wrapsDriver.getWrappedDriver())
				.getScreenshotAs(OutputType.FILE);
		String code = "";
		try {
			BufferedImage img = ImageIO.read(scrFile);
			ImageIO.write(img, "png",
					new File("C:\\img", System.currentTimeMillis() + ".png"));
			int screenshotWidth = img.getWidth();
			org.openqa.selenium.Dimension dimension = driver.manage().window()
					.getSize(); // 获取浏览器尺寸与截图的尺寸
			double scale = (double) dimension.getWidth() / screenshotWidth;
			int eleWidth = element.getSize().getWidth();
			int eleHeight = element.getSize().getHeight();
			Point point = element.getLocation();
			int subImgX = (int) (point.getX() / scale) + 12; // 获得元素的坐标
			int subImgY = (int) (point.getY() / scale) + 6;
			int subImgWight = (int) (eleWidth / scale) - 3; // 获取元素的宽高
			int subImgHeight = (int) (eleHeight / scale) - 6; // 精准的截取元素图片，
			BufferedImage dest = img.getSubimage(subImgX, subImgY, subImgWight,
					subImgHeight);
			ImageIO.write(dest, "png",
					new File("C:\\img", System.currentTimeMillis() + ".png"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return code;
	}

	// 截取键盘图片
	public static String getICBCImg1(WebElement element, WebDriver driver) {

		if (element == null)
			throw new NullPointerException("图片元素失败");
		WrapsDriver wrapsDriver = (WrapsDriver) element; // 截取整个页面
		File scrFile = ((TakesScreenshot) wrapsDriver.getWrappedDriver())
				.getScreenshotAs(OutputType.FILE);
		String code = "";
		try {
			BufferedImage img = ImageIO.read(scrFile);
			int screenshotWidth = img.getWidth();
			org.openqa.selenium.Dimension dimension = driver.manage().window()
					.getSize(); // 获取浏览器尺寸与截图的尺寸
			double scale = (double) dimension.getWidth() / screenshotWidth;
			int eleWidth = element.getSize().getWidth();
			int eleHeight = element.getSize().getHeight();
			Point point = element.getLocation();
			int subImgX = (int) (point.getX() / scale) + 10; // 获得元素的坐标
			int subImgY = (int) (point.getY() / scale) + 28;
			int subImgWight = (int) (eleWidth / scale) - 100; // 获取元素的宽高
			int subImgHeight = (int) (eleHeight / scale) - 110; // 精准的截取元素图片，
			BufferedImage dest = img.getSubimage(subImgX, subImgY, subImgWight,
					subImgHeight);
			String imageName = System.currentTimeMillis() + "JTKeyBoard.png";
			ImageIO.write(dest, "png", new File("C:\\img", imageName));

			Map<String, Object> map = MyCYDMDemo
					.Imagev("C:\\img\\" + imageName);
			code = map.get("strResult").toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return code;
	}

	/**
	 * 返回打码后的验证码
	 * 
	 * @param element
	 * @param driver
	 * @param path
	 *            图片绝对地址
	 * @param prefix
	 * @param suffix
	 * @return
	 * @throws Exception
	 */
	public static String saveImg(WebElement element, WebDriver driver)
			throws Exception {
		File file = new File("C:\\img" + File.separator);
		if (!file.exists()) {
			file.mkdir();
		}
		BufferedImage bufferedImage = createElementImages(driver, element);
		String fileName = System.currentTimeMillis() + "JTKeyBoard.png";
		ImageIO.write(bufferedImage, "png", new File(file, fileName));

		Map<String, Object> imagev = MyCYDMDemo.Imagev(file + "/" + fileName);
		String code = imagev.get("strResult").toString();// 读取图片验证码
		return code;
	}
	/**
	 * 返回打码后的验证码
	 * 
	 * @param element
	 * @param driver
	 * @param path
	 *            图片绝对地址
	 * @param prefix
	 * @param suffix
	 * @return
	 * @throws Exception
	 */
	public static String saveImgJ(WebElement element, WebDriver driver)
			throws Exception {
		File file = new File("C:\\img" + File.separator);
		if (!file.exists()) {
			file.mkdir();
		}
		BufferedImage bufferedImage = createElementImages(driver, element);
		String fileName = System.currentTimeMillis() + "JTKeyBoard.png";
		ImageIO.write(bufferedImage, "png", new File(file, fileName));
		String imagev = OCRHelper.recognizeImg("C:\\img"+File.separator, fileName);
//		Map<String, Object> imagev = MyCYDMDemo.Imagev(file + "/" + fileName);
//		String code = imagev.get("strResult").toString();// 读取图片验证码
		return imagev;
	}

	/**
	 * 截取验证码图片
	 * 
	 * @param driver
	 * @param webElement
	 * @return
	 * @throws IOException
	 */
	private static BufferedImage createElementImages(WebDriver driver,
			WebElement webElement) throws IOException {
		// 获得webElement的位置和大小。
		Point location = webElement.getLocation();
		Dimension size = webElement.getSize();
		// 创建全屏截图。
		BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(
				takeScreenshot(driver)));
		// 截取webElement所在位置的子图。
		BufferedImage croppedImage = originalImage.getSubimage(
				location.getX(), location.getY(),
				size.getWidth(), size.getHeight());
		return croppedImage;
	}

	private static byte[] takeScreenshot(WebDriver driver) throws IOException {
		WebDriver augmentedDriver = new Augmenter().augment(driver);
		return ((TakesScreenshot) augmentedDriver)
				.getScreenshotAs(OutputType.BYTES);
	}
	// public static void main(String[] args) throws InterruptedException,
	// ParseException, IOException {
	// getImage();
	// // String pwd="930229";
	// // String[] pwdArry = pwd.split("");
	// // for (int i=0;i<pwdArry.length;i++){
	// // System.out.println(pwdArry[i]);
	// // }
	// }
	//
}
