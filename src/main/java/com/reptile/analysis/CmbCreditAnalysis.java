package com.reptile.analysis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CmbCreditAnalysis {
	 public static JSONObject listanalysis(String endinfo) {
		 
		 JSONObject datemap=new JSONObject();
		 JSONObject bankListmap=new JSONObject();
		 JSONArray payRecord=new JSONArray();
		 Document doc = Jsoup.parse(endinfo);
		 
		 Elements loopBand = doc.getElementById("fixBand29").getElementById("loopBand2").select("tr"); 
		 Element time = doc.getElementById("fixBand6");  
		 Element back = doc.getElementById("fixBand8");  
		 String backdate = back.select("td").select("tr").select("td").get(2).text();
		 //账单周期2018/02/04
		 String times = time.select("td").select("tr").select("td").get(2).text();
		 //拼接交易日
		 String qiantime=times.substring(0, 10);//2017/12/17
		 String houtime=times.substring(11);//2018/01/16
		 for (int i = 6; i < loopBand.size(); i++) {
			 Element tr =loopBand.get(i);
			 Document trcontent = Jsoup.parse(tr.html());
			 Elements trr = trcontent.getElementById("fixBand15").select("tr").get(1).select("td");
			 
			 String tran_date=trr.get(2).text();//交易时间0104  1218
			 String tran_desc=trr.get(3).text();//交易描述
			 String post_amt=trr.get(4).text().replace("￥", "").replace(",", "").replace(" ", "");//交易金额
			 System.out.println("post_amt=="+post_amt);
			 String qiantime_yue=qiantime.substring(5,7);
			 String tran_date_yue=tran_date.substring(0,2);
			 if(tran_date_yue.equals(qiantime_yue)) {
				 String qiantime_year=qiantime.substring(0,4);
				 tran_date=qiantime_year+tran_date;
			 }else{
				 String houtime_year=houtime.substring(0,4);
				 tran_date=houtime_year+tran_date;
			 }
			 tran_date=dayBefore(tran_date);
			 datemap.put("post_amt", post_amt);
			 datemap.put("tran_desc", tran_desc);
			 datemap.put("tran_date", tran_date);
			 payRecord.add(datemap);
			 i+=7;
		}
		 Element time22 = doc.getElementById("fixBand6");  
		 Element time33 = doc.getElementById("fixBand7");  
		 
		 String CreditLimit = time22.select("td").select("tr").select("td").get(4).text()
				 .replace("￥", "").replace(",", "");
		 String RMBCurrentAmountDue = time33.select("td").select("tr").select("td").get(2).text()
				 .replace("￥", "").replace(",", "");
		 String RMBMinimumAmountDue = time33.select("td").select("tr").select("td").get(4).text()
				 .replace("￥", "").replace(",", "");
		 String PaymentDueDate=backdate.replace("/", "");//"到期还款日"
		 String StatementDate=houtime.replace("/", "");//"账单日";
		 JSONObject AccountSummary=new JSONObject();
			AccountSummary.put("PaymentDueDate", PaymentDueDate);
			AccountSummary.put("RMBCurrentAmountDue", RMBCurrentAmountDue);
			AccountSummary.put("StatementDate",StatementDate);
			AccountSummary.put("RMBMinimumAmountDue",RMBMinimumAmountDue);
			AccountSummary.put("CreditLimit",CreditLimit);
	       bankListmap.put("payRecord", payRecord);
	       bankListmap.put("AccountSummary", AccountSummary);
		 return bankListmap;
		}
	/**
	 * 获取日期的前一天 
	 * @param day
	 * @return
	 * @throws ParseException
	 */
public static String dayBefore(String day) {
	Calendar c = Calendar.getInstance();  
    Date date = null;  
        try {
			date = new SimpleDateFormat("yyyyMMdd").parse("20180104");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    c.setTime(date);  
    int dayt = c.get(Calendar.DATE);  
    c.set(Calendar.DATE, dayt - 1);  
    String dayBefore = new SimpleDateFormat("yyyyMMdd").format(c.getTime()); 
        return dayBefore;
}
 
	    /**
			 * 解析table
			 * @param xml
			 * @return
			 */
			 private static List<List<String>>  table(String xml){ 
				 
				 Document doc = Jsoup.parse(xml);
				 Elements trs = doc.select("table").select("tr");  
				 
				 List<List<String>> list = new ArrayList<List<String>>();
				 for (int i = 0; i < trs.size(); i++) {
					 Elements tds = trs.get(i).select("td");
					 List<String> item = new ArrayList<String>();
					 for (int j = 0; j < tds.size(); j++){
						 String txt = tds.get(j).text().replace(" ", "").replace(" ", "").replace("元", "");
						 item.add(txt);
					 }  
					 list.add(item);
				 }
				return list;	
		    } 
}
