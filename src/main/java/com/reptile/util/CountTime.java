package com.reptile.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.reptile.winio.CmbBank;

public class CountTime {
	private static Logger logger = Logger.getLogger(CmbBank.class);

	public static boolean getCountTime(String timeCnt) throws ParseException {
		System.out.println("认证时间**************************"+timeCnt);
		logger.warn("认证时间**************************"+timeCnt);

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String newTime = sdf.format(date);
		Date newDate = sdf.parse(newTime);
		Date cntDate = sdf.parse(timeCnt);
		int days = (int) ((newDate.getTime()-cntDate.getTime())/(1000*60*60*24));
		if(days>30) {
			return true;
		}else {
			return false;
		}
		
	}
	public static String currentTime(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日  HH:mm:ss");
	    String mon = format.format(new Date());
		return mon;
	}
}
