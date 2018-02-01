package com.reptile.analysis;

import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CmbcCreditAnalysis {
	/**
	 * 账单数据解析   按月
	 * @param banklist 每个月的账单数据
	 * @return
	 */
	public static JSONArray getInfos(Map<String, String> banklist){
		
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
		String repayLimitDate = response3Object.get("RepayLimitDate").toString();
		String currentperiodBillAmt = response3Object.get("CurrentperiodBillAmt").toString();
		String billDate = response3Object.get("BillDate").toString();
		String minRepayLimit = response3Object.get("MinRepayLimit").toString();
		
		JSONObject accountSummary=new JSONObject();
		accountSummary.put("PaymentDueDate", repayLimitDate);
		accountSummary.put("RMBCurrentAmountDue", currentperiodBillAmt);
		accountSummary.put("StatementDate",billDate);
		accountSummary.put("RMBMinimumAmountDue",minRepayLimit);
		//信用额度
		accountSummary.put("CreditLimit","60000");
		//每个月里的list
		for (int i = 0; i < payRecordlistArr.size(); i++) {
			JSONObject payRecordlistli = JSONObject.fromObject(payRecordlistArr.get(i));
			JSONArray List = JSONArray.fromObject(payRecordlistli.get("List"));
			JSONObject payRecordDateil=new JSONObject();
			for (int j = 0; j < List.size(); j++) {
				JSONObject payRecordlistArrlist = JSONObject.fromObject(List.get(j));
				String lastPeriodBillAmts=payRecordlistArrlist.get("TransAmt").toString();
				String transDescribe1=payRecordlistArrlist.get("TransDescribe1").toString();
				String transDate=payRecordlistArrlist.get("TransDate").toString();
			
				//每一条具体的记录信息
				payRecordDateil.put("post_amt", lastPeriodBillAmts);
				payRecordDateil.put("tran_desc", transDescribe1);
				payRecordDateil.put("tran_date",transDate);
				//每个月总的记录
				payRecord.add(payRecordDateil);
			}
		}
		//每个月总的数据
        bankList.put("payRecord", payRecord);
        bankList.put("AccountSummary", accountSummary);
		bankListarr.add(bankList);
		return bankListarr;
	}
	
}
