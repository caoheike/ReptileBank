package com.reptile.util;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reptile.winio.User32;
import com.reptile.winio.User32Util;
import com.reptile.winio.VKMapping;
import com.reptile.winio.VirtualKeyBoard;
import com.reptile.winio.WinIo32;

/**
 * 
 * @author Bigyoung
 * @version V1.0
 * @Description winio
 * @date 2017-11-13 2:50
 *
 */
public class KeysPress {
	public static final WinIo32 winIo32 = WinIo32.INSTANCE;
	private  static Logger logger = LoggerFactory.getLogger(KeysPress.class);
	
	Resttemplate resttemplate = new Resttemplate();
	application application = new application();
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

	// 输入密码
	public static void SenStr(String pwd) throws Exception {
		for (int i = 0; i < pwd.length(); i++) {
			KeyPress(pwd.charAt(i));
			Thread.sleep(50);
		}

	}

	/**
	 * 输入密码
	 * 
	 * @param pwd
	 * @throws Exception
	 */
	public static void sendPassWord(String password) throws Exception {
		for (int i = 0; i < password.length(); i++) {
			Thread.sleep(50);
			String number1 = String.valueOf(password.charAt(i));
			if (StringUtils.isNumeric(number1)) // 如果是数字直接输出
			{
				KeyPress(password.charAt(i));
				logger.warn(password.charAt(i) + "数字");
			} else {
				if (number1.equals("@")) {
					KeyPress1(VKMapping.toVK("Shift"));
					Thread.sleep(50);
					KeyPresss("2");
					KeyPress2(VKMapping.toVK("Shift"));
				} else {
					if (Character.isUpperCase(password.charAt(i))) // 判断是否是大写
					{
						logger.warn(password.charAt(i) + "大写");
						KeyPress1(VKMapping.toVK("Shift"));
						Thread.sleep(50);
						KeyPress(number1.toLowerCase().charAt(0));

						KeyPress2(VKMapping.toVK("Shift"));
					} else {
						logger.warn(password.charAt(i) + "小写");
						Thread.sleep(50);
						KeyPress(password.charAt(i));
					}
				}
			}
		}

	}

	// 按下tab
	public static void SendTab(String Tab) throws Exception {
		Thread.sleep(900);
		KeyPresss(Tab);

	}

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
