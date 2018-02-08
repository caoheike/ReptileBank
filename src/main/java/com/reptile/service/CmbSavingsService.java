package com.reptile.service;

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

import org.apache.http.ParseException;
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
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hoomsun.keyBoard.HttpWatchUtil;
import com.hoomsun.keyBoard.SendKeys;
import com.reptile.util.CYDMDemo;
import com.reptile.util.Dates;
import com.reptile.util.DriverUtil;
import com.reptile.util.JsonUtil;
import com.reptile.util.PushSocket;
import com.reptile.util.PushState;
import com.reptile.util.Resttemplate;
import com.reptile.util.SimpleHttpClient;
import com.reptile.util.application;

@Service
public class CmbSavingsService {
	private final static String CMBlogin="https://nper.cmbc.com.cn/pweb/static/login.html";//民生银行登陆界面
    private Logger logger= LoggerFactory.getLogger(CmbSavingsService.class);

/**
 * 民生银行储蓄卡
 * @param request
 * @param response
 * @param userCard
 * @param passWord
 * @return@RequestParam("flag") boolean flag
 * @throws InterruptedException 
 */
    public    Map<String,Object> login(HttpServletRequest request,HttpServletResponse response,String userCard,String passWord,String idCard,String UUID,boolean flag) {
        Map<String,Object> map=new HashMap<String,Object>();
        Map<String,Object> data=new HashMap<String,Object>();
        PushSocket.push(map, UUID, "1000","民生储蓄卡登录中");
        PushState.stateByFlag(idCard, "savings", 100,flag);
        WebDriver driver =null; 
    	try {
    		logger.warn("########【民生信用卡########登陆开始】########【用户名：】"
					+ userCard + "【密码：】" + passWord+"【身份证号：】"+idCard);			
			driver=DriverUtil.getDriverInstance("ie");	
			System.setProperty("java.awt.headless", "false");
			driver.get(CMBlogin);			
			driver.manage().window().maximize();
			
			driver.navigate().refresh();
			//driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);//显示等待
		    WebElement element=	driver.findElement(By.id("writeUserId"));
		    element.sendKeys(userCard);//输入账号
		    Thread.sleep(1000);

		    SendKeys.sendStr(1180, 380+15, passWord);
//		    SendKeys.sendStr(1180, 380+60, passWord);//本地
		    
		
			Thread.sleep(1000);
		    //判断是否需要图形验证码
			
			WebElement element2=driver.findElement(By.tagName("form"));	
			WebElement element3=element2.findElements(By.tagName("div")).get(2);
			WebElement loginButton = driver.findElement(By.id("loginButton"));
			WebElement webElement= driver.findElement(By.id("_tokenImg"));
			if(webElement.getAttribute("src")==null||"".equals(webElement.getAttribute("src"))){
				//不需要验证码直接提交		
				
				Thread.sleep(1000);
				loginButton.click();//点击登陆
			}else{
				logger.warn("########【需要图形验证码】########【身份证号：】"+idCard);
				//System.out.println("需要图形验证码");
				WebElement element4=element2.findElement(By.id("_tokenImg"));//图形验证码
                String imageCode=imageGet(element4, driver,request );
                Thread.sleep(2000);
				WebElement element5=element2.findElement(By.id("_vTokenName"));//验证码输入框
				Thread.sleep(2000);
				element5.sendKeys(imageCode);	
				
				loginButton.click();//点击登陆
			}		
			//调出httpwatch
			HttpWatchUtil.openHttpWatch();
//			try {
//				HttpWatchUtil.startHttpWatch(50,575);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

    	} catch (Exception e) {
    		logger.warn("########【民生储蓄未登陆成功，进入try-catch】########【原因：】网络异常，登录失败【身份证号：】"+idCard);
			logger.warn("民生银行",e);
			// driver.quit();
			map.put("errorCode", "0001");
            map.put("errorInfo", "网络连接异常!");
            PushSocket.push(map, UUID, "3000","网络连接异常,登录失败");
			PushState.stateByFlag(idCard, "savings", 200,"网络连接异常,登录失败",flag);
			driver.quit();
			logger.warn("----民生储蓄卡------errorCode："+map.get("errorCode")+"-----errorInfo："+map.get("errorInfo"));
            return map;
			//e.printStackTrace();
		
		}
			
			
			WebElement element6=null;
			WebElement element7=null;
            try{
            	element6=driver.findElement(By.id("transView"));
            	if(element6!=null&&element6.getText().contains("个人网上银行首次登录")){
            		logger.warn("########【未登陆成功】########【原因：】您为第一次登陆网上银行，请先登陆官网设置您的登陆名和登陆密码【身份证号：】"+idCard);
            		logger.warn("--------------民生储蓄卡------------首次登陆------------身份证号："+idCard);
    				//首次登陆
    				//System.out.println("您为第一次登陆网上银行，请先登陆官网设置您的登陆名和登陆密码");
    				map.put("errorCode", "0001");
    	            map.put("errorInfo", "您为第一次登陆网上银行，请先登陆官网设置您的登陆名和登陆密码");
    	            PushSocket.push(map, UUID, "3000","您为第一次登陆网上银行，请先登陆官网设置您的登陆名和登陆密码");
    	            driver.quit();
    	            logger.warn("----民生储蓄卡------errorCode："+map.get("errorCode")+"-----errorInfo："+map.get("errorInfo"));
    	            return map;
    			}else{
    				//登陆成功
    				logger.warn("--------------民生储蓄卡------------登陆成功------------身份证号："+idCard);
    				PushSocket.push(map, UUID, "2000","民生储蓄卡登陆成功");
    				logger.warn("########【登陆成功】########【身份证号：】"+idCard);
    				try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
    				String userName="";
    				try {
    				WebDriverWait wait = new WebDriverWait(driver, 20);
    			    wait.until(ExpectedConditions.titleContains("中国民生银行个人网银"));
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.className("v-binding")));                  
    				List<WebElement> ss = driver.findElements(By.className("v-binding"));
    				PushSocket.push(map, UUID, "5000","民生储蓄卡数据获取中");
    				logger.warn("########【开始获取数据】########【身份证号：】"+idCard);
    				//wait.until(ExpectedConditions.elementToBeClickable(ss.get(0)));
    				userName=	ss.get(0).getText().split("好，")[1];//用户名
    				
    			    
 
					} catch (Exception e) {
						logger.warn("民生银行",e);
						PushState.stateByFlag(idCard, "savings", 200,"系统繁忙，数据获取失败",flag);
						PushSocket.push(map, UUID, "7000","系统繁忙，数据获取失败");
						
						map.put("errorCode", "0002");
			            map.put("errorInfo", "系统繁忙请稍后有再试!");
			            driver.quit();
			            logger.warn("----民生储蓄卡------errorCode："+map.get("errorCode")+"-----errorInfo："+map.get("errorInfo"));
	    	            return map;
					}
    				logger.warn("--------------民生储蓄卡------------民生银行详单获取中...------------身份证号："+idCard);
////    				//开始解析账户详情
    				Map<String, Object>    baseMes=new HashMap<String, Object>();//存放基本信息
    				baseMes.put("openBranch", "");	//开户网点
    			    baseMes.put("openTime", "");	//	 开户日期
    			    baseMes.put("accountType", "");	//账号状态
    			    List<Map<String, Object>>    billMes=new ArrayList<Map<String,Object>>();  //存放交易明细
    			    //交易明细解析
    			    try {
						billMes=this.parseBillMes(driver, billMes,userCard,userName);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.warn("########【交易明细解析方法执行中：进入try-catch  原因：网络连接异常，数据获取失败】########【身份证号：】"+idCard);
						PushSocket.push(map, UUID, "7000","网络连接异常，数据获取失败");
    			    	PushState.stateByFlag(idCard, "savings", 200,"网络连接异常，数据获取失败",flag);
    			    	map.put("errorCode", "0002");
    			    	map.put("errorInfo", "网络连接异常!");
    			    	driver.quit();
    			    	logger.warn("----民生储蓄卡------errorCode："+map.get("errorCode")+"-----errorInfo："+map.get("errorInfo"));
        	            return map;
					}    			   
    			    map.put("bankName","中国民生银行");
    			    map.put("userName", userName.trim());//用户名
    			    map.put("cardNumber", userCard);//卡号
    			    map.put("IDNumber", idCard);//身份证号码
    			    map.put("baseMes", baseMes);//基本信息
    			    map.put("billMes",billMes);//流水
    			    PushSocket.push(map, UUID, "6000","民生储蓄卡数据获取成功");
    			    logger.warn("########【获取数据成功】########【身份证号：】"+idCard);
//    			    map = new Resttemplate().SendMessage(map, ConstantInterface.port+"/HSDC/savings/authentication");  //推送数据
    			    logger.warn("########【开始推送】########【身份证号：】"+idCard);
    			    map = new Resttemplate().SendMessage(map, application.sendip+"/HSDC/savings/authentication");  //推送数据
    			    logger.warn("########【推送完成】########【身份证号：】"+idCard+"数据中心返回结果："+map.toString());
    			    if(map!=null&&"0000".equals(map.get("errorCode").toString())){
    		           	 PushState.stateByFlag(idCard, "savings", 300,flag);
    		           	PushSocket.push(map, UUID, "8000","认证成功");
    		           	data.put("errorInfo","推送成功");
    		           	data.put("errorCode","0000");
    		           }else{
    		           	 PushState.stateByFlag(idCard, "savings", 200,map.get("errorInfo").toString(),flag);
    		           	PushSocket.push(map, UUID, "9000",map.get("errorInfo").toString());
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
					logger.warn("########【登陆失败 原因：附加码问题：展示给客户为：网络异常，请刷新重试!】########【身份证号：】"+idCard);
					//System.out.println("网络异常，请刷新重试");
					 map.put("errorInfo", "网络异常，请刷新重试!");
					 
				}else{
					logger.warn("########【登陆失败 原因："+element1.getText()+"########【身份证号：】"+idCard);
					map.put("errorInfo", element1.getText());
					//System.out.println(element1.getText());
				} 
				PushSocket.push(map, UUID, "3000",element1.getText());
				PushState.stateByFlag(idCard, "savings", 200,element1.getText(),flag);
				logger.warn("民生银行",element1.getText());
				driver.quit();
				logger.warn("----民生储蓄卡------errorCode："+map.get("errorCode")+"-----errorInfo："+map.get("errorInfo"));
	            return map;
		}
    	
		
            driver.quit();
            logger.warn("----民生储蓄卡------errorCode："+map.get("errorCode")+"-----errorInfo："+map.get("errorInfo"));
		return map;
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
     * @throws IOException 
     * @throws ParseException 
     * @throws InterruptedException 
     */
    public static Map<String, Object> parseBaseMes(WebDriver driver,Map<String, Object> baseMes){
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
  		
  		 baseMes.put("errorCode", "0002");
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
    public  List<Map<String, Object>> parseBillMes(WebDriver driver,List<Map<String, Object>> billMes,String userCard,String userName) throws Exception{
    	
    	String jsession = HttpWatchUtil.getCookie("JSESSIONID");
    	System.out.println("-----------------jsession=:"+jsession);
    	Map<String, Object> params = null;
		Map<String, String> headers = new HashMap<String, String>(16);
		headers.put("Referer", "https://nper.cmbc.com.cn/pweb/static/main.html");
		headers.put("Host", "nper.cmbc.com.cn");
		headers.put("Cookie", jsession);
		params = new HashMap<String, Object>(16);
		// 请求1  明细
		String response = SimpleHttpClient.get("https://nper.cmbc.com.cn/pweb/static/ActTrsQry/ActTrsQryPre_new.html",headers);
		// 请求1  近三个月
		params.put("AcNo", userCard);
		params.put("BankAcType", "03");
		
		String EndDate = Dates.currentTimeM();
        System.out.println("***************************************获取前三个月月份");
        String BeginDate = Dates.beforMonthM(3);
        
		params.put("BeginDate", BeginDate);	//开始时间	
		params.put("EndDate", EndDate);//结束时间
		params.put("AcName", userName);
		params.put("Remark", "-");
		params.put("Fee", "0.00");
		params.put("FeeRemark", "-");
		params.put("SubAcSeq", "0001");
		params.put("currentIndex", 0);
		params.put("uri", "/pweb/ActTrsQry.do");
		
		headers.put("Cookie", jsession);
    	
		// 请求2   近三个月
		response = SimpleHttpClient.post("https://nper.cmbc.com.cn/pweb/ActTrsQry.do",params, headers);
		Map<String, Object> pageHeader = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();//明细
		
		pageHeader = (Map<String, Object>) JsonUtil.getJsonValue1(response, "_PageHeader");//TotalCount
		String totalCount = (String) pageHeader.get("TotalCount");
		int cCount = (int) pageHeader.get("Ccount");//总页数
		List<Map<String, Object>> getInfo = new ArrayList<Map<String, Object>>();
		if(cCount>1) {
			for(int i=0;i<cCount;i++) {
				list = (List<Map<String, Object>>) JsonUtil.getJsonValue1(response, "List");//明细集合
				billMes = this.parseBill(list,getInfo);
				if(i<cCount-1) {
					params.put("pageNo", i+2);
    				params.put("recordNumber", Integer.parseInt(totalCount));
    				params.put("currentIndex", (i+1)*10);
    				// 请求三   下一页
    				response = SimpleHttpClient.post("https://nper.cmbc.com.cn/pweb/ActTrsQry.do",params, headers);
				}
			}
		}else {
			billMes = this.parseBill(list,getInfo);
		}
    	
		params.clear();
		headers.clear();
		return billMes;
	}   	
		
    	
    /***
     * 明细解析
     * @param driver
     * @param billMes
     * @return
     */
    public  List<Map<String, Object>> parseBill(List<Map<String, Object>> billMes,List<Map<String, Object>> getInfo){
    	logger.warn("--------------行数据："+billMes);
    	for(int i=0;i<billMes.size();i++) {
    		Map<String, Object> datas=new HashMap<String, Object>();
    		String dealTime = (String) billMes.get(i).get("TransDate");
    		dealTime = dealTime.substring(0, 4) + "/" + dealTime.substring(4,6) + "/" + dealTime.substring(6);
    		datas.put("dealTime", dealTime);//交易日期	
    		String flagName = (String) billMes.get(i).get("DCFlagName");
    		if("存".equals(flagName)) {
    			datas.put("incomeMoney", billMes.get(i).get("CAmount"));//收入金额
    			datas.put("expendMoney", "0");//支出金额
    		}else {
    			datas.put("incomeMoney", "0");//收入金额
    			datas.put("expendMoney", billMes.get(i).get("QAmount"));//支出金额
    		}
			
				datas.put("dealDitch",billMes.get(i).get("Channel"));//交易渠道
				datas.put("balanceAmount", billMes.get(i).get("Balance")+"");//余额
				datas.put("dealReferral", billMes.get(i).get("Remark"));//业务摘要
				datas.put("oppositeSideName","");//对方账户名
				datas.put("currency", "");//币种	
				datas.put("oppositeSideNumber", billMes.get(i).get("PayeeAc"));//对方账户				
				getInfo.add(datas);
    	}
    	return getInfo;
    	
    } 
}
