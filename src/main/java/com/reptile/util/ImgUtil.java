package com.reptile.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Augmenter;

import com.gargoylesoftware.htmlunit.html.HtmlImage;


/**
 * 
 * @ClassName: ImgUtil  
 * @Description: TODO (图片工具类)
 * @author: xuesongcui
 * @date 2017年12月29日  
 *
 */
public class ImgUtil {
		
	/**
	 * 保存验证码图片,返回图片本地存储绝对路径+图片名
	 * @param htmlImg 图片流
	 * @param prefix  图片名称前缀
	 * @param verifyImagesPath 图片保存的绝对路径
	 * @param suffix 图片名称后缀 如png
	 * @return
	 * @throws IOException
	 */
	public static String saveImg(HtmlImage htmlImg,String prefix, String verifyImagesPath,String suffix) throws IOException{
	  
		File file = new File(verifyImagesPath + File.separator);
       	if (!file.exists()) {
       		file.mkdir();
       	}
	   	ImageReader imgReader = htmlImg.getImageReader();
	    BufferedImage bufferedImage  = ImageIO.read((ImageInputStream)imgReader.getInput());
		String fileName = prefix + System.currentTimeMillis()+"."+suffix;
		ImageIO.write(bufferedImage, suffix, new File(file,fileName));
	   	String filePath = verifyImagesPath + File.separator + fileName;
		return filePath;
	}
	/**
	 * 保存验证码图片,返回浏览器可访问到图片的地址
	 * @param htmlImg 图片流
	 * @param prefix  图片名称前缀
	 * @param verifyImagesPath 图片在项目中的相对路径 如：/verifyImages
	 * @param suffix 图片名称后缀 如png
	 * @param request 
	 * @return
	 * @throws IOException
	 */
	public static String saveImg(HtmlImage htmlImg,String prefix, String suffix,HttpServletRequest request) throws IOException{
	    String verifyImages = request.getSession().getServletContext().getRealPath("verificationImg");
		File file = new File(verifyImages + File.separator);
		if (!file.exists()) {
			file.mkdir();
		}
		ImageReader imgReader = htmlImg.getImageReader();
		BufferedImage bufferedImage  = ImageIO.read((ImageInputStream)imgReader.getInput());
		String fileName = prefix + System.currentTimeMillis()+"."+suffix;
		ImageIO.write(bufferedImage, suffix, new File(file,fileName));

		String filePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + verifyImages + "/" + fileName;
		return filePath;
	}
	
	
	/**
	 * 返回打码后的验证码
	 * @param element
	 * @param driver
	 * @param HttpServletRequest request
	 * @param prefix  图片名称前缀
	 * @param suffix 图片名称后缀 png
	 * @return
	 * @throws Exception
	 */
	public static String saveImg(WebElement element, WebDriver driver,String prefix,String suffix) throws Exception{
		String verifyImagePath = "c://verificationImg";
		File file = new File(verifyImagePath + File.separator);
		if (!file.exists()) {
			file.mkdir();
		}
        BufferedImage bufferedImage = createElementImages(driver, element);
		String fileName = prefix + System.currentTimeMillis()+"."+suffix;
		ImageIO.write(bufferedImage, suffix, new File(file,fileName));
		
		//读取图片验证码
		String code = CYDMDemo.getcode(file + "/" +fileName);
		return code;
	}
		
	
	  /**
	   * 截取验证码图片
	   * @param driver
	   * @param webElement
	   * @return
	   * @throws IOException
	   */
	  private static BufferedImage createElementImages(WebDriver driver,WebElement webElement)  
	      throws IOException {  
	    // 获得webElement的位置和大小。  
	    Point location = webElement.getLocation();  
	    Dimension size = webElement.getSize();  
	    // 创建全屏截图。  
	    BufferedImage originalImage =  ImageIO.read(new ByteArrayInputStream(takeScreenshot(driver)));  
	    // 截取webElement所在位置的子图。  
	    BufferedImage croppedImage = originalImage.getSubimage(location.getX(),
				location.getY(), size.getWidth(), size.getHeight());	 
	    return croppedImage;  
	  }
	  
	  
	  private static byte[] takeScreenshot(WebDriver driver) throws IOException {  
	      WebDriver augmentedDriver = new Augmenter().augment(driver);  
	    return ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.BYTES);  
      } 
	
}
