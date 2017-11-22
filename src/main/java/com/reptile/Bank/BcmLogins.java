package com.reptile.Bank;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.ParseException;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import com.reptile.util.CrawlerUtil;
import com.reptile.util.JiaoTongKeyMap;
import com.reptile.util.MyCYDMDemo;
import com.reptile.util.PushSocket;
import com.reptile.util.Resttemplate;
import com.reptile.util.application;

import org.apache.log4j.Logger;

/**
 * 
 * @author Bigyoung
 * @version v1.0
 * @deprecated 交通信用卡
 * @date 2017年11月16日10:51:14
 */
public class BcmLogins {

	private Logger logger = Logger.getLogger(BcmLogins.class);

	Resttemplate resttemplate = new Resttemplate();
	 public Map<String,Object> BankLogin(String UserNumber,String UserPwd,String UserCard,HttpServletRequest request,String UUID) throws InterruptedException{
		 	HttpSession session=request.getSession();
		 	List<String> list=new ArrayList<String>();
		 	Map<String,Object> map=new HashMap<String,Object>();
		 	Map<String,Object> data=new HashMap<String,Object>();
	    	System.setProperty("webdriver.chrome.driver", "D:/ie/chromedriver.exe");
		    ChromeOptions options = new ChromeOptions();
		        //设置浏览器大小避免截图错乱
		        options.addArguments("start-maximized");
		        ChromeDriver driver = new ChromeDriver(options);
	    	try {

	        //开始执行任务
	        driver.get("https://creditcardapp.bankcomm.com/idm/sso/login.html?service=https://creditcardapp.bankcomm.com/member/shiro-cas");
	        try {
		        driver.findElement(By.className("close_overlay")).click();
			} catch (NoSuchElementException e) {
				// TODO: handle exception
			}

	        driver.findElementById("cardNo").sendKeys(UserNumber);
	        driver.findElement(By.id("cardpassword")).click();
	        WebElement element = driver.findElement(By.className("key-pop"));
	        String icbcImg1 = getICBCImg1(element, driver);//返回键盘中数字
	        String[] split = icbcImg1.split("");//将数字字符串分割为数组
	        List<WebElement> li = element.findElements(By.tagName("li"));
	        //返回识别的字符串 
	        String pwd=UserPwd;
	        String[] pwdArry = pwd.split("");
	        for (int i=0;i<pwdArry.length;i++){   //读取密码的每一位数字，循环找出键盘中对应的数字下标
	            String num=pwdArry[i];
	            for(int j=0;j<split.length;j++){
	                if(num.equals(split[j])){  //数字部分
	                    li.get(j).click();
	                    Thread.sleep(500);
	                    break;
	                }
	                if(j==split.length-1){   //字符部分
	                     int integer = JiaoTongKeyMap.map.get(num);
	                    li.get(integer).click();
	                    Thread.sleep(500);
	                    break;
	                }

	            }
	        }
	        //触发登录按钮
	        driver.findElementById("cardNo").click();
	        Thread.sleep(3000);
	    	JavascriptExecutor jss = (JavascriptExecutor) driver;
	    	jss.executeScript("$('#cardLogin').click();", "");
	 
//	        driver.findElement(By.id("loginBtn")).click();
	        Thread.sleep(4000);
	       //监听是否有错误信息
	        List<WebElement> msg=driver.findElements(By.className("errormsg"));
	        if(msg.size()==0){
	 
//	        	if(driver.findElement(By.id("moibleMessages"))!=null){
	        //识别是否需要发送验证码
	        	if(driver.getPageSource().contains("moibleMessages")){
	        		//需要发送验证码
	        		System.out.println("开始发送验证码");
//	        		WebElement webElement= driver.findElement(By.id("send_Button"));
//	        		WebElement webElements= driver.findElement(By.id("moibleMessages"));
//	        		WebElement sub= driver.findElement(By.id("submit"));
//	        		webElement.click();
//	                Alert alt = driver.switchTo().alert();
//	                alt.accept();//验证码发送完毕监听信息框 确定
//	                //获得浏览器当前句柄
//	                String windoshandle=driver.getWindowHandle();
//	                String sessid=CrawlerUtil.getUUID();
//	                String driverid=CrawlerUtil.getUUID();
//	                session.setAttribute(sessid,windoshandle);//句柄
//	                session.setAttribute(driverid,driver);//客户端
////
	        		map.put("whetherCode", "no");
//	                map.put("sessid", sessid);
//	                map.put("driverid",driverid);
//	                map.put("whetherCode", "yes");
	        		logger.warn(UserCard+"此帐号在登录时需要验证码，可能帐号出现异常 需要自行登录 才可认证");
	        		map.put("whetherCode", "no");
	        		map.put("errorCode","0004");//认证失败
	    			map.put("errorInfo","帐号认证异常，请你先尝试在官网登录");
	            	return map;
	        	//不需要发送验证码
	        	}else if(driver.getPageSource().contains("查看我的买单吧")){
	        		PushSocket.push(map, UUID, "0000");
	        	    driver.executeScript("javascript:gotToLink('/member/member/service/billing/detail.html');",0);
	                WebDriverWait wait = new WebDriverWait(driver, 20);        
	                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bill_date")));
	              	for (int i = 0; i < 5; i++) {
	          	    Select sel = new Select(driver.findElement(By.id("bill_date")));
	                sel.selectByIndex(i);
	                WebElement elements = driver.findElement(By.xpath("//*[@id='bill_content']/p/a"));
	                elements.click();
	                System.out.println(driver.getPageSource());
	                Thread.sleep(3000);
	                list.add((driver.getPageSource()));
	                WebElement goback = driver.findElement(By.className("goback"));
	                goback.click();
	                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bill_date")));
	               
	        	}
	  
	        
	    	}else{
	    		map.put("errorCode","0001");//认证失败
    			map.put("errorInfo","账号密码错误");
    	         driver.quit();
    			return map;
	    	}
	            data.put("html", list);
	            
	            data.put("backtype","BCM");
	            data.put("idcard",UserCard);
	            map.put("data", data);
//	            map= resttemplate.SendMessage(map, "http://192.168.3.16:8089/HSDC/BillFlow/BillFlowByreditCard",UserCard);    
	            map= resttemplate.SendMessage(map,  application.sendip+"/HSDC/BillFlow/BillFlowByreditCard",UserCard);  
	            
	           
                map.put("whetherCode", "no");
	            driver.quit();
	        	return map;
	          }else{
	     
	              
	       		map.put("errorCode","0001");//认证失败
    			map.put("errorInfo",msg.get(1).getText());
    		    driver.quit();
    		  	return map;
	     
	          }

		} catch (Exception e) {
			e.printStackTrace();
			map.put("errorCode","0001");
			map.put("errorInfo","网络异常");
	        driver.quit();
		}
		 
		return map;
		 
	 }
	 
	 public Map<String,Object> CodeLogin(HttpServletRequest request,String sessid,String dirverid,String code,String card) throws InterruptedException{
	        List<String> list=new ArrayList();
	        Map<String,Object> map=new HashMap<String,Object>();
	        Map<String,Object> data=new HashMap<String,Object>();
		 HttpSession session=request.getSession();
		String sessids=session.getAttribute(sessid).toString();
		 ChromeDriver driver = (ChromeDriver) session.getAttribute(dirverid);
		 driver.switchTo().window(sessids);
 		WebElement sub= driver.findElement(By.id("submit"));
 		sub.click();
 		Thread.sleep(4000);
 	      List<WebElement> msg=driver.findElements(By.className("errormsg"));
 	     if(msg.size()==0){
 	    	 
     	    driver.executeScript("javascript:gotToLink('/member/member/service/billing/detail.html');",0);
            WebDriverWait wait = new WebDriverWait(driver, 20);        
          wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bill_date")));
      
          	for (int i = 0; i < 5; i++) {
      	    Select sel = new Select(driver.findElement(By.id("bill_date")));
                sel.selectByIndex(i);
                WebElement elements = driver.findElement(By.xpath("//*[@id='bill_content']/p/a"));
                elements.click();
                System.out.println(driver.getPageSource());
                Thread.sleep(3000);
                list.add((driver.getPageSource()));
                WebElement goback = driver.findElement(By.className("goback"));
                goback.click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bill_date")));
                data.put("html", list);
                
                data.put("backtype","BCM");
                data.put("idcard",card);
                map.put("data", data);
                map= resttemplate.SendMessage(map, "http://192.168.3.16:8089/HSDC/BillFlow/BillFlowByreditCard");
                System.out.println(map.toString());
           
    	}
 	    	 
 	     }else{
 	        System.out.println(msg.get(1).getText());
            driver.quit();
 	     }
		 return null;
		 
		 
		 
	 }
		public static StringBuffer Setcookie(WebDriver driver)		{
			  //获得cookie用于发包
			Set<Cookie> cookies = driver.manage().getCookies();  
			   StringBuffer tmpcookies = new StringBuffer();
	   
			   for (Cookie cookie : cookies) {
				   tmpcookies.append(cookie.toString()+";");
	 	   
	 	  
				
			}
			return tmpcookies; 	
			
		}
		public static StringBuffer Setcookies(WebDriver driver)		{
			//获得cookie用于发包
			Set<Cookie> cookies = driver.manage().getCookies();  
			StringBuffer tmpcookies = new StringBuffer();
		
			for (Cookie cookie : cookies) {
				if(cookie.getName().equals("JSESSIONID")||cookie.getName().equals("member_session_sticky")||cookie.getName().equals("_tcs")||cookie.getName().equals("t0")||cookie.getName().equals("IDMAUTH")||cookie.getName().equals("loginType")||cookie.getName().equals("CHANNEL")){
					tmpcookies.append(cookie.toString());
				}
		
				
				
				
			}
			return tmpcookies; 	
			
		}
	    public static String getICBCImg(WebElement element, WebDriver driver) {
	        if (element == null) throw new NullPointerException("图片元素失败");
	        WrapsDriver wrapsDriver = (WrapsDriver) element; //截取整个页面
	        File scrFile = ((TakesScreenshot) wrapsDriver.getWrappedDriver()).getScreenshotAs(OutputType.FILE);
	        String code = "";
	        try {
	            BufferedImage img = ImageIO.read(scrFile);
	            ImageIO.write(img,"png",new File("C:\\img",System.currentTimeMillis()+".png"));
	            int screenshotWidth = img.getWidth();
	            org.openqa.selenium.Dimension dimension = driver.manage().window().getSize(); //获取浏览器尺寸与截图的尺寸
	            double scale = (double) dimension.getWidth() / screenshotWidth;
	            int eleWidth = element.getSize().getWidth();
	            int eleHeight = element.getSize().getHeight();
	            Point point = element.getLocation();
	            int subImgX = (int) (point.getX() / scale)+12; //获得元素的坐标
	            int subImgY = (int) (point.getY() / scale)+6;
	            int subImgWight = (int) (eleWidth / scale)-3 ; //获取元素的宽高
	            int subImgHeight = (int) (eleHeight / scale) -6; //精准的截取元素图片，
	            BufferedImage dest = img.getSubimage(subImgX, subImgY, subImgWight, subImgHeight);
	            ImageIO.write(dest, "png", new File("C:\\img",System.currentTimeMillis()+".png"));

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return code;
	    }

	    //截取键盘图片
	    public static String getICBCImg1(WebElement element, WebDriver driver) {
	    	
	        if (element == null) throw new NullPointerException("图片元素失败");
	        WrapsDriver wrapsDriver = (WrapsDriver) element; //截取整个页面
	        File scrFile = ((TakesScreenshot) wrapsDriver.getWrappedDriver()).getScreenshotAs(OutputType.FILE);
	        String code = "";
	        try {
	            BufferedImage img = ImageIO.read(scrFile);
	            int screenshotWidth = img.getWidth();
	            org.openqa.selenium.Dimension dimension = driver.manage().window().getSize(); //获取浏览器尺寸与截图的尺寸
	            double scale = (double) dimension.getWidth() / screenshotWidth;
	            int eleWidth = element.getSize().getWidth();
	            int eleHeight = element.getSize().getHeight();
	            Point point = element.getLocation();
	            int subImgX = (int) (point.getX() / scale)+10; //获得元素的坐标
	            int subImgY = (int) (point.getY() / scale)+28;
	            int subImgWight = (int) (eleWidth / scale)-100; //获取元素的宽高
	            int subImgHeight = (int) (eleHeight / scale)-110 ; //精准的截取元素图片，
	            BufferedImage dest = img.getSubimage(subImgX, subImgY, subImgWight, subImgHeight);
	            String imageName=System.currentTimeMillis()+"JTKeyBoard.png";
	            ImageIO.write(dest, "png", new File("C:\\img",imageName));
	            
	             Map<String,Object> map= MyCYDMDemo.Imagev("C:\\img\\"+imageName);
	             code = map.get("strResult").toString();
	             

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return code;
	    }

//	    public static void main(String[] args) throws InterruptedException, ParseException, IOException {
//	        getImage();
////	        String pwd="930229";
////	        String[] pwdArry = pwd.split("");
////	        for (int i=0;i<pwdArry.length;i++){
////	            System.out.println(pwdArry[i]);
////	        }
//	    }
//	
}
