package com.reptile.dd;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.reptile.util.DriverUtil;

public class DDTest {

	public static void main(String[] args) throws InterruptedException {
		WebDriver driver = DriverUtil.getDriverInstance("ie");
		driver.get("https://nper.cmbc.com.cn/pweb/static/login.html");
		WebDriverWait wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.titleContains("中国民生银行个人网上银行"));
		JavascriptExecutor jss = (JavascriptExecutor) driver;

		List list = new ArrayList();
		/* 获得输入元素 */
		WebElement elements = driver.findElement(By.id("writeUserId"));
		elements.sendKeys("6226011038013902");
		/* 执行换号 */
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/* 按下Tab */
		DDUtil.DD_tab();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DDUtil.DD_str("wangmeng000");
//
		driver.findElement(By.id("loginButton")).click();
	}
	
}
