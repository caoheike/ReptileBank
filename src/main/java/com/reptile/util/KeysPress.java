package com.reptile.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

/**
 * 
 * @author Bigyoung
 * @version V1.0
 * @Description winio
 * @date 2017-11-13 2:50
 *
 */
public class KeysPress {
	

	// 打开浏览器

	public static WebDriver OpenUrl(String driverType, String url,
			String driverPath) throws Exception {
		WebDriver driver = null;
		if (driverType.equals("ie")) {
			System.setProperty("webdriver.ie.driver", driverPath);
			driver = driver = new InternetExplorerDriver();
			driver.get(url);
			return driver;
		}
		return driver;

	}

}
