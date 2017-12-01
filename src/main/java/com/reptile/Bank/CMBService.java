package com.reptile.Bank;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.reptile.util.CYDMDemo;
import com.reptile.util.DriverUtil;
import com.reptile.util.PushSocket;
import com.reptile.util.PushState;
import com.reptile.util.Resttemplate;
import com.reptile.util.application;
import com.reptile.winio.User32;
import com.reptile.winio.User32Util;
import com.reptile.winio.VKMapping;
import com.reptile.winio.WinIo32;

@Service
public class CMBService {
	private final static String CMBlogin="https://nper.cmbc.com.cn/pweb/static/login.html";//民生银行登陆界面
    public static final WinIo32 winIo32 = WinIo32.INSTANCE;
    private Logger logger= LoggerFactory.getLogger(CMBService.class);
	static {
		System.out.println(WinIo32.INSTANCE.InitializeWinIo());
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
/**
 * 民生银行储蓄卡
 * @param request
 * @param response
 * @param userCard
 * @param passWord
 * @return
 */
    public    Map<String,Object> login(HttpServletRequest request,HttpServletResponse response,String userCard,String passWord,String idCard,String UUID){
        Map<String,Object> map=new HashMap<String,Object>();
        Map<String,Object> data=new HashMap<String,Object>();
        WebDriver driver =null; 
    	try {
    		logger.warn("民生银行");
			driver=DriverUtil.getDriverInstance("ie");
			driver.get(CMBlogin);
			driver.manage().window().maximize();
			driver.navigate().refresh();
			//driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);//显示等待
		    WebElement element=	driver.findElement(By.id("writeUserId"));
		    element.sendKeys(userCard);//输入账号
		    String s = "Tab";//
			KeyPresss(s);
		//	new WebDriverWait(driver, 15).until(ExpectedConditions.)
		    Thread.sleep(500);	
			inputCode(passWord);//密码
			KeyPresss("Tab");
			Thread.sleep(2000);
			//new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
		    //判断是否需要图形验证码
			WebElement element2=driver.findElement(By.tagName("form"));	
			WebElement element3=element2.findElements(By.tagName("div")).get(2);
			WebElement loginButton = driver.findElement(By.id("loginButton"));
			WebElement webElement= driver.findElement(By.id("_tokenImg"));
			if(webElement.getAttribute("src")==null||"".equals(webElement.getAttribute("src"))){
				//System.out.println("不要图形验证码");
				//不需要验证码直接提交		
				loginButton.click();//点击登陆
			}else{
				//System.out.println("需要图形验证码");
				WebElement element4=element2.findElement(By.id("_tokenImg"));//图形验证码
                String imageCode=imageGet(element4, driver,request );
                Thread.sleep(2000);
				WebElement element5=element2.findElement(By.id("_vTokenName"));//验证码输入框
				Thread.sleep(2000);
				element5.sendKeys(imageCode);
				loginButton.click();//点击登陆
			}
			//new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.id("transView")));
			Thread.sleep(4000);
			WebElement element6=null;
			WebElement element7=null;
            try{
            	element6=driver.findElement(By.id("transView"));
            	if(element6!=null&&element6.getText().contains("个人网上银行首次登录")){
            		logger.warn("民生银行首次登陆");
    				//首次登陆
    				//System.out.println("您为第一次登陆网上银行，请先登陆官网设置您的登陆名和登陆密码");
    				map.put("errorCode", "0001");
    	            map.put("errorInfo", "您为第一次登陆网上银行，请先登陆官网设置您的登陆名和登陆密码");
    	            driver.quit();
    	            return map;
    			}else{//登陆成功
    				PushSocket.push(map, UUID, "0000");
    				PushState.state(idCard, "savings", 100);
    				logger.warn("民生银行登陆成功");
    				System.out.println("登陆成功");
    				WebDriverWait wait = new WebDriverWait(driver, 20);
    			    wait.until(ExpectedConditions.titleContains("中国民生银行个人网银"));
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.className("v-binding")));
    				List<WebElement> ss = driver.findElements(By.className("v-binding"));
    				//wait.until(ExpectedConditions.elementToBeClickable(ss.get(0)));
    				String userName=	ss.get(0).getText().split("好，")[1];//用户名
    			    wait.until(ExpectedConditions.elementToBeClickable(ss.get(7)));
    			   	ss.get(7).click();//点击账户余额查询
    			   	//Thread.sleep(4000);
    			   	wait.until(ExpectedConditions.presenceOfElementLocated(By.className("byue_0")));
    			   	try {
    			   		WebElement _vTokenId = driver.findElement(By.className("byue_0"));
        			    _vTokenId.click();//点击账户详情
        				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("BenhangKa")));
        			   
        				//Thread.sleep(3000);
					} catch (Exception e) {
						PushState.state(idCard, "savings", 200);
						logger.warn("民生银行",e);
						map.put("errorCode", "0002");
			            map.put("errorInfo", "系统繁忙请稍后有再试!");
			            return map;
					}
    			   	logger.warn("民生银行详单获取中...");
    				//开始解析账户详情
    				Map<String, String>    baseMes=new HashMap<String, String>();//存放基本信息
    				baseMes=this.parseBaseMes(driver, baseMes);
    				
    			    List<Map<String, String>>    billMes=new ArrayList<Map<String,String>>();  //存放交易明细
    			    //交易明细解析
    			    billMes=this.parseBillMes(driver, billMes);
    			    if(billMes.contains("errorCode")){
    			    	PushState.state(idCard, "savings", 200);
    			    	map.put("errorCode", "0003");
    			    	map.put("errorInfo", "网络连接异常!");
    			    	return map;
    			    }
    			    map.put("bankName","中国民生银行");
    			    map.put("userName", userName.trim());//用户名
    			    map.put("cardNumber", userCard);//卡号
    			    map.put("IDNumber", idCard);//身份证号码
    			    map.put("baseMes", baseMes);//基本信息
    			    map.put("billMes",billMes);//流水
//    			    map = new Resttemplate().SendMessage(map, ConstantInterface.port+"/HSDC/savings/authentication");  //推送数据
    			    map = new Resttemplate().SendMessage(map, application.sendip+"/HSDC/savings/authentication");  //推送数据
    			    if(map!=null&&"0000".equals(map.get("errorCode").toString())){
    		           	 PushState.state(idCard, "savings", 300);
    		           	data.put("errorInfo","推送成功");
    		           	data.put("errorCode","0000");
    		           }else{
    		           	 PushState.state(idCard, "savings", 200);
    		           	data.put("errorInfo","推送失败");
    		           	data.put("errorCode","0001");
    		           }
    			    
    			}
         	
			}catch (NoSuchElementException e) {//判断是否登陆成功
				logger.warn("民生银行",e);
				
				//没有就失败
				map.put("errorCode", "0001");
				WebElement element1= driver.findElement(By.id("jsonError"));//错误提示信息（如果验证码错误：附加码）
				
				if(element1.getText().contains("附加码")){
					
					//System.out.println("网络异常，请刷新重试");
					 map.put("errorInfo", "网络异常，请刷新重试!");
				}else{
					
					map.put("errorInfo", element1.getText());
					//System.out.println(element1.getText());
				} 
				logger.warn("民生银行",element1.getText());
				// driver.quit();
				 return map;
		}
		
		} catch (Exception e) {
			logger.warn("民生银行",e);
			// driver.quit();
			map.put("errorCode", "0001");
            map.put("errorInfo", "网络连接异常!");
			//e.printStackTrace();
		
		}finally{
			  driver.close();//关闭浏览器
			  try {
				  Runtime.getRuntime().exec("taskkill /F /IM IEDriverServer.exe");
			} catch (IOException e) {
				map.put("errorCode", "0001");
	            map.put("errorInfo", "网络连接异常!");
				e.printStackTrace();
			}

			 // driver.quit();
		}
		
		return map;
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
    /**
     * 打码结果
     * @param element 图片元素
     * @param driver 
     * @param file  
     * @return
     * @throws IOException
     */
    public  static String  imageGet(WebElement element,WebDriver driver,HttpServletRequest request) throws IOException{
    	
		File file=new File("C:/");
		if(!file.exists()){
			 file.mkdirs();
		}
    	
    	if (element == null) throw new NullPointerException("图片元素失败");
         WrapsDriver wrapsDriver = (WrapsDriver) element; //截取整个页面
          String code = "";
          File scrFile = ((TakesScreenshot) wrapsDriver.getWrappedDriver()).getScreenshotAs(OutputType.FILE);

             BufferedImage img = ImageIO.read(scrFile);
             int screenshotWidth = img.getWidth();
             org.openqa.selenium.Dimension dimension = driver.manage().window().getSize(); //获取浏览器尺寸与截图的尺寸
             double scale = (double) dimension.getWidth() / screenshotWidth;
             int eleWidth = element.getSize().getWidth();
             int eleHeight = element.getSize().getHeight();
             Point point = element.getLocation();
             int subImgX = (int) (point.getX() / scale); //获得元素的坐标
             int subImgY = (int) (point.getY() / scale);
             int subImgWight = (int) (eleWidth / scale) + 10; //获取元素的宽高
             int subImgHeight = (int) (eleHeight / scale) + 10; //精准的截取元素图片，
             BufferedImage dest = img.getSubimage(subImgX, subImgY, subImgWight, subImgHeight);
             String path="QQ"+System.currentTimeMillis()+".png";
             File file1=new File(file,path);
             ImageIO.write(dest, "png", file1);
             System.out.println(file1.getAbsolutePath());
        
         code = CYDMDemo.getcode(path.substring(0, path.indexOf(".")));
         return code;
    }
    
    /**
     * 解析基本信息
     * @param driver
     * @param baseMes 存放基本信息
     * @return
     * @throws InterruptedException 
     */
    public static Map<String, String> parseBaseMes(WebDriver driver,Map<String, String> baseMes) {
    	Document	docs= Jsoup.parse(driver.getPageSource());
  	   try {
		Element	tables	= docs.getElementById("BenhangKa");//获取账户基本信息的table
		new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("p")));
    	//Thread.sleep(500);
    	Elements pText=tables.getElementsByTag("p");//获取含有账户基本的元素
    	System.out.println( pText.get(0).text() );
        String[] baseInfo=pText.get(0).text().split("：");//基本信息
    	
	    baseMes.put("openBranch", baseInfo[2].substring(0,baseInfo[2].lastIndexOf("开户")).trim());	//开户网点
	    baseMes.put("openTime", baseInfo[3].substring(0,baseInfo[3].lastIndexOf("状态")).trim());	//	 开户日期
	    baseMes.put("accountType", baseInfo[baseInfo.length-1]);	//账号状态
  	   } catch (Exception e) {
  		
  		 baseMes.put("errorCode", "0001");
  		 baseMes.put("errorInfo", "网络连接异常!");
  		
 	   }
    	return baseMes;
    	
    }
    
   /** 
    * 解析明细
    * @param driver
    * @param billMes
    * @return
 * @throws Exception 
    */
    public  List<Map<String, String>> parseBillMes(WebDriver driver,List<Map<String, String>> billMes) throws Exception{
    	new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='transView']/div/div/div/div/div/div[1]/table[2]/tbody[1]/tr[1]/td[3]/a[4]")));
    	WebElement _vTokenId1 = driver.findElement(By.xpath("//*[@id='transView']/div/div/div/div/div/div[1]/table[2]/tbody[1]/tr[1]/td[3]/a[4]"));
		_vTokenId1.click();
		new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.className("tab_search")));
		WebElement startDate=	driver.findElement(By.className("riqi"));
		new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("lanzi1")));
		WebElement td=startDate.findElements(By.className("lanzi1")).get(6);
		td.click();
		new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfElementLocated(By.className("fyxg")));
		
		 WebElement	 s=driver.findElement(By.className("fyxg"));
		 if(s!=null){
			
			 String numStr=s.getText();
			 Thread.sleep(500);
			 String numStr1=numStr.split("页")[1];
			 System.out.println(numStr1.split("共")[1]);//总页数
			 Thread.sleep(500);
			 int num=new Integer(numStr1.split("共")[1]);
			 if(num>=1){
				Object[] obj= this.parseBill(driver,billMes);
			 //有多页
		    	 billMes=(List<Map<String, String>>)obj[0];
		    	 driver=(WebDriver) obj[1];
	    	      for (int i = 1; i < num; i++) {
	    	    	 new WebDriverWait(driver, 15).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("a")));
	  				
	 				  List<WebElement> option= s.findElements(By.tagName("a"));
	 				  WebElement click=option.get(1);
	 				  click.click();
	    	    	  Thread.sleep(5000);
	    	    	  obj= this.parseBill(driver,billMes);
	    	    	  billMes=(List<Map<String, String>>)obj[0];
	 		    	  driver=(WebDriver) obj[1];
		    		 // billMes=this.parseBill(driver,billMes);
				}
			 }else{
				//只有一页
				 billMes=(List<Map<String, String>>) this.parseBill(driver,billMes)[0];
					//billMes=this.parseBill(driver,billMes);
				}
			 
		 }
		
  	return billMes;
    	
    }
    /***
     * 明细解析
     * @param driver
     * @param billMes
     * @return
     */
    public  Object[] parseBill(WebDriver driver,List<Map<String, String>> billMes){
    	Object[] obj=new Object[2];
    	Document	docs= Jsoup.parse(driver.getPageSource());
		Element	tables	= docs.getElementById("DataTable");//获取账户交易明细table
		for (int i = 1; i < tables.select("tr").size(); i++) {
			Elements tds = tables.select("tr").get( i ).select( "td" );
			Map<String, String> datas=new HashMap<String, String>();
			datas.put("dealTime", tds.get(1).text().trim());//交易日期	
			datas.put("incomeMoney", tds.get(2).text().substring(2,tds.get(2).text().length()).trim());//收入金额
			datas.put("expendMoney ", tds.get(3).text().substring(2,tds.get(3).text().length()).trim());//支出金额
				datas.put("dealDitch",tds.get(0).text().substring(12,tds.get(0).text().length()).trim() );//交易渠道
				datas.put("balanceAmount", tds.get(4).text().trim());//余额
				datas.put("dealReferral", tds.get(5).text().trim());//业务摘要
				datas.put("oppositeSideName","");//对方账户名
				datas.put("currency", "");//币种	
				datas.put("oppositeSideNumber ", tds.get(6).text().trim());//对方账户
				
				billMes.add(datas);
		}
		obj[0]=billMes;
		obj[1]=driver;
    	return obj;
    	
    } 
}
