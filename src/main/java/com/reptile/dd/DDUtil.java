package com.reptile.dd;

/**
 * 
 * @ClassName: DDUtil  
 * @Description: TODO(DD工具类)
 * @author: xuesongcui
 * @date 2017年12月19日  
 *
 */
public class DDUtil {
	/**
	 * 摁下tab键
	 * @throws InterruptedException 
	 */
	public static void DD_tab() throws InterruptedException{
		Thread.sleep(500);
		DD.INSTANCE.DD_key(300, 1);
		DD.INSTANCE.DD_key(300, 2);
	}
	
	/**
	 * 输入密码
	 * @throws InterruptedException 
	 */
	public static void DD_str(String password) throws InterruptedException{
		Thread.sleep(500);
		DD.INSTANCE.DD_key(601, 1);DD.INSTANCE.DD_key(601, 2); 
		DD.INSTANCE.DD_str(password); 
	}
}
