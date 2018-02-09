package com.reptile.analysis;

import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CmbcCreditAnalysis {
	/**
	 * 账单数据解析   按月
	 * @param banklist 每个月的账单数据
	 * @param creTLmit 
	 * @return
	 */
	public static JSONArray getInfos(Map<String, String> banklist, String creTLmit){
		
		JSONObject fromObject = JSONObject.fromObject(banklist.toString());
		JSONArray yuemap = JSONArray.fromObject(fromObject.get("banklist"));
		
		//循环   总月的数据
		JSONArray payRecord=new JSONArray();
		JSONObject bankList=new JSONObject();
		JSONArray bankListarr=new JSONArray();
	        //请求3 每个月的汇总信息
			String payRecordlist=JSONObject.fromObject(yuemap.get(0)).toString();
			JSONObject payRecordlistobj = JSONObject.fromObject(payRecordlist);
			JSONArray payRecordlistArr = JSONArray.fromObject(payRecordlistobj.get("payRecordlist"));
			
			//每个月的汇总数据
			String response3=JSONObject.fromObject(yuemap.get(0)).get("response3").toString();
			JSONObject response3Object = JSONObject.fromObject(response3);
			String RepayLimitDate = response3Object.get("RepayLimitDate").toString();
			String CurrentperiodBillAmt = response3Object.get("CurrentperiodBillAmt").toString();
			String BillDate = response3Object.get("BillDate").toString();
			String MinRepayLimit = response3Object.get("MinRepayLimit").toString();
			
			JSONObject AccountSummary=new JSONObject();
			AccountSummary.put("PaymentDueDate", RepayLimitDate);
			AccountSummary.put("RMBCurrentAmountDue", CurrentperiodBillAmt);
			AccountSummary.put("StatementDate",BillDate);
			AccountSummary.put("RMBMinimumAmountDue",MinRepayLimit);
			//信用额度
			AccountSummary.put("CreditLimit",creTLmit);
			//每个月里的list
			for (int i = 0; i < payRecordlistArr.size(); i++) {
				JSONObject payRecordlistli = JSONObject.fromObject(payRecordlistArr.get(i));
				JSONArray List = JSONArray.fromObject(payRecordlistli.get("List"));
				JSONObject payRecordDateil=new JSONObject();
				for (int j = 0; j < List.size(); j++) {
					JSONObject payRecordlistArrlist = JSONObject.fromObject(List.get(j));
					String LastPeriodBillAmts=payRecordlistArrlist.get("TransAmt").toString();
					String TransDescribe1=payRecordlistArrlist.get("TransDescribe1").toString();
					String TransDate=payRecordlistArrlist.get("TransDate").toString();
				
					//每一条具体的记录信息
					payRecordDateil.put("post_amt", LastPeriodBillAmts);
					payRecordDateil.put("tran_desc", TransDescribe1);
					payRecordDateil.put("tran_date",TransDate);
					//每个月总的记录
					payRecord.add(payRecordDateil);
				}
			}
			//每个月总的数据
	        bankList.put("payRecord", payRecord);
	        bankList.put("AccountSummary", AccountSummary);
			bankListarr.add(bankList);
       
		return bankListarr;
	}
	
}
