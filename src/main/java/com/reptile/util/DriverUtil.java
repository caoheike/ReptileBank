package com.reptile.util;

import java.io.IOException;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class  DriverUtil{
	
	/**
	 * 显示等待，通过title来判断元素是否存在
	 * @param title 网站标题
	 * @param driver 
	 * @param time 多长时间关闭 单位 秒
	 */
	public static boolean waitByTitle(String title,WebDriver driver,int time){
      WebDriverWait wite = new WebDriverWait(driver,time);
      try {
    	  wite.until(ExpectedConditions.titleContains(title));
      } catch (Exception e) {
		  return false;
      }
      return true;
    
	}
	
	
	/**
	 * 显示等待，通过className来判断元素是否存在
	 * @param className 
	 * @param driver 
	 * @param time 多长时间关闭 单位 秒
	 */
	public static boolean waitByClassName(String className,WebDriver driver,int time){
		WebDriverWait wite = new WebDriverWait(driver,time);
		try {
			wite.until(ExpectedConditions.presenceOfElementLocated(By.className(className)));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * 显示等待，通过id来判断元素是否存在
	 * @param className 
	 * @param driver 
	 * @param time 多长时间关闭 单位 秒
	 */
	public static boolean waitById(String id,WebDriver driver,int time){
		WebDriverWait wite = new WebDriverWait(driver,time);
		try {
			wite.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 显示等待，通过linkText来判断元素是否存在
	 * @param linkText 
	 * @param driver 
	 * @param time 多长时间关闭 单位 秒
	 */
	public static boolean waitByLinkText(String linkText,WebDriver driver,int time){
		WebDriverWait wite = new WebDriverWait(driver,time);
		try {
			wite.until(ExpectedConditions.presenceOfElementLocated(By.linkText(linkText)));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * 显示等待，通过ID判断元素是否可见，可见代表元素非隐藏，并且宽和高非0
	 * @param id 
	 * @param driver 
	 * @param time 多长时间关闭 单位 秒
	 */
	public static boolean visibilityById(String id,WebDriver driver,int time){
		WebDriverWait wite = new WebDriverWait(driver,time);
		try {
			wite.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * 显示等待，通过className判断元素是否可见，可见代表元素非隐藏，并且宽和高非0
	 * @param className 
	 * @param driver 
	 * @param time 多长时间关闭 单位 秒
	 */
	public static boolean visibilityByClassName(String className,WebDriver driver,int time){
		WebDriverWait wite = new WebDriverWait(driver,time);
		try {
			wite.until(ExpectedConditions.visibilityOfElementLocated(By.className(className)));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 显示等待，通过className判断元素是否可点击
	 * @param className 
	 * @param driver 
	 * @param time 多长时间关闭 单位 秒
	 */
	public static boolean clickByClassName(String className,WebDriver driver,int time){
		WebDriverWait wite = new WebDriverWait(driver,time);
		try {
			wite.until(ExpectedConditions.elementToBeClickable(By.className(className)));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	/**
	 * 显示等待，通过id判断元素是否可点击
	 * @param id 
	 * @param driver 
	 * @param time 多长时间关闭 单位 秒
	 */
	public static boolean clickById(String id,WebDriver driver,int time){
		WebDriverWait wite = new WebDriverWait(driver,time);
		try {
			wite.until(ExpectedConditions.elementToBeClickable(By.id(id)));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	/**
	 * 显示等待，通过linkText判断元素是否可点击
	 * @param linkText 
	 * @param driver 
	 * @param time 多长时间关闭 单位 秒
	 */
	public static boolean clickByLinkText(String linkText,WebDriver driver,int time){
		WebDriverWait wite = new WebDriverWait(driver,time);
		try {
			wite.until(ExpectedConditions.elementToBeClickable(By.linkText(linkText)));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 判断某个元素是否可点击
	 * @param ele
	 * @param driver
	 * @param time
	 * @return
	 */
	public static boolean clickByElement(WebElement ele,WebDriver driver,int time){
		WebDriverWait wite = new WebDriverWait(driver,time);
		try {
			wite.until(ExpectedConditions.elementToBeClickable(ele));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 判断是否有弹窗，如有弹窗则返回弹窗内的text，否则返回空
	 * @param id 
	 * @param driver 
	 * @param time 多长时间关闭 单位 秒
	 */
	public static String alertFlag(WebDriver driver){
		String str = "";
		try {
			
			 Alert alt = driver.switchTo().alert();
			 str = alt.getText();
			 System.out.println(str);
			 alt.accept();
			
		} catch (Exception e) {
			//不做任何处理
		}
		return str;
	}
	
	
	
	/**
	 * 关闭所有进程(针对64位的)，仅支持同步
	 * @param driver
	 * @throws IOException 
	 */
	public static void close(WebDriver driver,String exec) throws IOException{
		if(driver != null){
			driver.close();
			Runtime.getRuntime().exec(exec);
		}
	}
	
	
	/**
	 * 关闭所有进程(针对32位的)
	 * @param driver
	 */
	public static void close(WebDriver driver){
		if(driver != null){
			driver.quit();
		}
	}
	
	
	/**
	 * 
	 * @param type ie 或 chrome
	 * @return
	 */
	public static WebDriver getDriverInstance(String type){
		WebDriver driver = null;
		if(type.equals("ie")){
			System.setProperty("webdriver.ie.driver", "C:/ie/IEDriverServer.exe");
			DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
			ieCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,true);
			ieCapabilities.setCapability(InternetExplorerDriver.BROWSER_ATTACH_TIMEOUT,15000);
			driver = new InternetExplorerDriver(ieCapabilities);
		}
		return driver;
	}
	
}
