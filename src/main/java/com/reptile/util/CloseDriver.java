package com.reptile.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mysql.jdbc.Driver;

/**
 * 
 * @author Bigyoung
 * @version v1.0
 * @deprecated 通用 错误处理
 * @date 2017年11月16日10:51:14
 */
public class CloseDriver {
	
	/**
	 * 网络错误处理
	 * @param Ttitle 网站标题
	 * @param driver 
	 * @param time 多长时间关闭 单位 秒
	 */
	public static void DriverClose(String Ttitle,WebDriver driver,int time){
      WebDriverWait wite=new WebDriverWait(driver,time);
      try {
    	  wite.until(ExpectedConditions.titleContains(Ttitle));
	} catch (Exception e) {
		Close(driver);
	}
    
	}
	
	/**
	 * 关闭所有进程
	 * @param driver
	 */
	public static void Close(WebDriver driver){
		
		driver.quit();
	}
	
	

}
