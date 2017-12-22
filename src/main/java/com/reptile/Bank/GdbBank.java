package com.reptile.Bank;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.sf.json.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Augmenter;

import com.hoomsun.keyBoard.SendKeys;
import com.reptile.util.CYDMDemo;
import com.reptile.util.CrawlerUtil;
import com.reptile.util.KeysPress;
import com.reptile.util.Resttemplate;


/**
 * 
 * @author Bigyoung
 * @version v1.0
 * @deprecated 广发银行信用卡
 * @date 2017年11月16日10:51:14
 */
public class GdbBank {
	private static CYDMDemo cydmDemo=new CYDMDemo();
	
	public static void main(String[] args) throws Exception {
		
		Map<String,Object> params=new HashMap<String, Object>();
		Map<String,String> headers=new HashMap<String, String>();
		  Map status=new HashMap();
		WebDriver driver=KeysPress.OpenUrl("ie", "https://pbank.95559.com.cn/personbank/logon.jsp#","D:/ie/IEDriverServer.exe");
		JavascriptExecutor js = (JavascriptExecutor) driver;
		String userName = "18220834780";
		String userPwd = "weizai9529";
		//获得账号输入框
		WebElement username=driver.findElement(By.id("alias"));

		WebElement login=driver.findElement(By.id("login"));
		
		//输入账号
		username.sendKeys(userName);
		//按下Tab
		SendKeys.sendTab();
		//输入密码
		SendKeys.sendStr(userPwd);
	
		
		WebElement element= driver.findElement(By.className("captchas-img-bg"));
		System.out.println(element.getAttribute("src"));
		//判断是否有验证码
		 if(element.getAttribute("src")!=null){
				WebElement input_captcha=driver.findElement(By.id("input_captcha"));
				String imgtext= downloadImgs(driver,element);
				input_captcha.sendKeys(imgtext);
//		
		 }
			login.click();
		Thread.sleep(3000);
//		//此处判断是否登陆成功
//		 WebElement element2= driver.findElement(By.className("lanse-12-b"));
		boolean flgs= ElementExist(driver,By.className("lanse-12-b"));//错误表示
		boolean flgb= ElementExist(driver,By.id("captchaErrMsg"));//JS错误提示
		
		
		 if(flgb==true&&!driver.findElement(By.id("captchaErrMsg")).getText().equals("")){
			 System.out.println("验证码输出错误");
		      driver.quit();

		 }else if(flgs==true){
				System.out.println(driver.findElement(By.className("lanse-12-b")).getText());
			  driver.quit();
		 }else{
				
				boolean flg= ElementExist(driver,By.id("btnConf1"));
				//判断是否有登陆确认信息
				if(flg==true){
					WebElement btnConf1=driver.findElement(By.id("btnConf1"));
					btnConf1.click();
				}
				
				//执行JS去点击账单查询
				try {
				
					String zd="Util.changeMenu('P001000');";
					
					js.executeAsyncScript(zd);

				} catch (Exception e) {
					// TODO: handle exception
				}
				//切入ifrmae

				
				
		        driver.switchTo().frame("frameMain");
		        driver.switchTo().frame("tranArea");
		  
               List<Map> lists=yuefen();
//					//点击明细   
				WebElement mx= driver.findElement(By.linkText("明细"));
				mx.click();
				//开始设置日期
				System.out.println(lists.get(0).get("begin").toString());
				js.executeScript("$('#startDate_show').val('"+lists.get(0).get("begin").toString()+"');$('#endDate_show').val('"+lists.get(5).get("end").toString()+"');$('#btnQry2').click()","");
				//开始解析
//				System.out.println(driver.getPageSource());
		        Document docs = Jsoup.parse(driver.getPageSource());   
			 	   Elements trs = docs.getElementsByClass("form-table");
			 	   System.out.println(trs.html());
				   Elements tr=trs.select("tr");
			    	  List list=new ArrayList();
				   for (int i = 1; i < tr.size(); i++) {
					   Map<String,Object>map=new HashMap<String, Object>();
	
					   Elements td=tr.get(i).select("td");
					   for (int j = 0; j < td.size(); j++) {
						 System.out.println(td.get(j).text()+j+"- - -");
						 if(j==0){
					          //交易时间
							 if(td.get(j).text().equals("查询")){
								 
						 	}else if(td.get(j).text().contains("保存文件格式")){
						 		
						 	}else{
						 		  map.put("dealTime",td.get(j).text() );
						 	}
						
						 }
						 if(j==1){
							 //交易方式
							   map.put("dealReferral", td.get(j).text());//业务摘要
						 }
						 if(j==2){
							 //交易币种
							   map.put("currency", td.get(j).text());//业务摘要
						
						 }
						 if(j==3){
							   map.put("dealAmount", td.get(j).text());//交易金额
							 //支出金额
						 }
						 if(j==4){
							 //收入金额
						 }
						 if(j==5){
							 //收入余额
							   map.put("balanceAmount", td.get(j).text());//余额
						 }
						   
						 if(j==6){
							 //交易地点
							   map.put("dealDitch", td.get(j).text());//交易渠道
						 }
						 
						
					}
					   map.put("oppositeSideName", "");
					   map.put("oppositeSideNumber", "");
					   map.put("currency", "");
					   list.add(map);
					   
				   }
				   
				   params.clear();
				   headers.clear();
				   headers.put("accountType","");
				   headers.put("openBranch","");
				   headers.put("openTime","");

				    params.put("billMes", list);
				    params.put("baseMes", headers);
				    params.put("IDNumber", "123");
				    params.put("cardNumber", userName);
				    params.put("userName", "张三");
//				  System.out.println(JSONObject.fromObject(params));
			
					Resttemplate resttemplate=new Resttemplate();
					status=	resttemplate.SendMessageCredit(JSONObject.fromObject(params), "http://192.168.3.4:8081/HSDC/savings/authentication");
					System.out.println(status);
				   
		 }
		 

		
	
      
	}
	

	 public static List<Map> yuefen() {
			List list = new ArrayList();

			for (int i = -5; i < 1; i++) {

				SimpleDateFormat matter = new SimpleDateFormat("yyyyMM");
				SimpleDateFormat mattery = new SimpleDateFormat("yyyy");
				SimpleDateFormat matterm = new SimpleDateFormat("MM");
				
				Calendar calendar = Calendar.getInstance();
				// 将calendar装换为Date类型
				Date date = calendar.getTime();
				// 将date类型转换为BigDecimal类型（该类型对应oracle中的number类型）
				BigDecimal time01 = new BigDecimal(matter.format(date));
				// 获取当前时间的前6个月
				calendar.add(Calendar.MONTH, i);
				Date date02 = calendar.getTime();
				BigDecimal time02 = new BigDecimal(matter.format(date02));
				String s1 = mattery.format(date02);
				String s2 = matterm.format(date02);
				String end = getLastDayOfMonth(Integer.valueOf(s1), Integer.valueOf(s2));
				Map map = new HashMap();
				map.put("begin", s1+"-"+s2+"-01");
				map.put("end", end);
				list.add(map);

			}
			return list;

		}
    public static boolean ElementExist(WebDriver driver,By locator)
    {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException  e) {
            return false;
        }

    }
	 public static String downloadImgs(WebDriver driver,WebElement keyWord) throws IOException{
  
		 String src =  keyWord.getAttribute("src");  
		 String filename=new CrawlerUtil().getUUID();
		 BufferedImage inputbig = createElementImages(driver,keyWord);  
		 ImageIO.write(inputbig, "png", new File("C://"+filename+".png"));
		 String codenice= cydmDemo.getcode(filename,1006); //识别yanzhengma
		 return codenice;
		 
	 }
	 
		public static String getLastDayOfMonth(int year, int month) {
			Calendar cal = Calendar.getInstance();
			// 设置年份
			cal.set(Calendar.YEAR, year);
			// 设置月份
			cal.set(Calendar.MONTH, month - 1);
			// 获取某月最大天数
			int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			// 设置日历中月份的最大天数
			cal.set(Calendar.DAY_OF_MONTH, lastDay);
			// 格式化日期
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String lastDayOfMonth = sdf.format(cal.getTime());

			return lastDayOfMonth;
		}
	    public static BufferedImage createElementImages(WebDriver driver,WebElement webElement)  
	    		throws IOException {  
	    	// 获得webElement的位置和大小。  
	    	Point location = webElement.getLocation();  
	    	Dimension size = webElement.getSize();  
	    	// 创建全屏截图。  
	    	BufferedImage originalImage =  
	    			ImageIO.read(new ByteArrayInputStream(takeScreenshot(driver)));  
	    	// 截取webElement所在位置的子图。  
	    	BufferedImage croppedImage = originalImage.getSubimage(  
	    			location.getX(),
	    			location.getY(),  
	    			size.getWidth(),  
	    			size.getHeight());  
	    	return croppedImage;  
	    }

	    public static byte[] takeScreenshot(WebDriver driver) throws IOException {  
	        WebDriver augmentedDriver = new Augmenter().augment(driver);  
	      return ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.BYTES);  
	  
	        } 
}
