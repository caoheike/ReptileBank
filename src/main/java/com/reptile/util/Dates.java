package com.reptile.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
/**
 * 
 * @author Administrator
 *
 */
public class Dates {
	/**
	 * 获得上个月
	 * @param year
	 * @param month
	 * @return
	 */

	public static  String beforMonth(int  a){
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
	    c.add(Calendar.MONTH, -a);
	    Date m = c.getTime();
	    String mon = format.format(m);
		return mon;
	}
	
	/**
	 * 获得上个月
	 * @param year
	 * @param month
	 * @return
	 */

	public static  String beforMonthM(int  a){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
	    c.add(Calendar.MONTH, -a);
	    Date m = c.getTime();
	    String mon = format.format(m);
		return mon;
	}
	
	/**
	 * 获取当前时间（年月日时分秒）
	 * @return
	 */
	public static String currentTime(){
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
	    String mon = format.format(new Date());
		return mon;
	}
	
	/**
	 * 获取当前时间（年月日时分秒）
	 * @return
	 */
	public static String currentTimeM(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	    String mon = format.format(new Date());
		return mon;
	}
	
	/**
	 * 获取n年前为某年
	 * @return
	 */
	public static String beforeYear(int n){
		SimpleDateFormat format = new SimpleDateFormat("yyyy");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.YEAR, -n);
		Date date = c.getTime();
		String year = format.format(date);
		return year;
	}
	
	/**
	 * 获取当前时间，单位秒
	 * @return
	 */
	public static long getCurrentTime(){
		return new Date().getTime();
	}
	
	/**
	 * 获取三个月之前的时间，单位秒
	 * @return
	 */
	public static long getBeforeTime(){
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.MONTH, -6);
		return c.getTime().getTime();
	}
	
	
	public static void main(String[] args) {
		System.out.println(getBeforeTime());
	}
}
