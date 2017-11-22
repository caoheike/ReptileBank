package com.reptile.contorller;

import io.swagger.annotations.ApiOperation;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.reptile.Bank.BcmLogin;
import com.reptile.Bank.BcmLogins;
import com.reptile.Bank.AbcBank;
import com.reptile.service.MobileService;
import com.reptile.winio.CmbBank;
import com.reptile.winio.VirtualKeyBoard;


@Controller
@RequestMapping("interface")
/**
 * @author Bigyoung
 * @version v1.0
 * @deprecated 银行爬虫接口
 * @date 2017年11月16日10:51:14
 */
public class InterfaceController  {

	@Resource 
	private MobileService mobileService;
	
   	@ResponseBody
   	@RequestMapping(value="BankLogin",method=RequestMethod.POST)
   	@ApiOperation(value="银行登陆", notes="银行登陆")//设置标题描述
   	public Map<String,Object>Login(HttpServletRequest request,HttpServletResponse response,@RequestParam("number") String numbe,@RequestParam("pwd") String pwd,@RequestParam("BankType") String BankType) throws Exception {
  		VirtualKeyBoard bank = new  VirtualKeyBoard();
  		CmbBank banks = new  CmbBank();
  		BcmLogin BcmLogin=new BcmLogin();
  		BcmLogins JiaoTong=new BcmLogins();
  		Map<String,Object> map=new HashMap<String,Object>();
   		 HttpSession session=request.getSession();
   		 String userCard=request.getParameter("userCard");
   		 String UUID=request.getParameter("UUID");
        synchronized (this)
        {
        	if(BankType.equals("CMB")){//招商银行
            	map=bank.Login(numbe,pwd, session);
        	}else if(BankType.equals("CCB")){//浦发银行

        	}else if(BankType.equals("BOC")){
        		
        	}else if(BankType.equals("CMBC")){//民生
        		map=	bank.CMBCLogin(numbe, pwd, BankType,userCard,UUID);
        	}else if(BankType.equals("GDB")){//广发银行
        		map=bank.GDBLogin(numbe, pwd,userCard,UUID);
        	}else if(BankType.equals("CXCMB")){//招商储蓄卡
        		map=banks.CMBLogin(numbe, pwd, request,userCard);
        	}else if(BankType.equals("BCM")){//交通银行
        		map=BcmLogin.BcmLogins(numbe, pwd,UUID);
        	}else if(BankType.equals("X-BCM")){//交通银行 信用卡
        		map=JiaoTong.BankLogin(numbe, pwd, userCard, request,UUID);
        	}else if(BankType.equals("ABC")){//农业银行
        		map=AbcBank.NongYe(numbe, pwd,UUID,userCard);
        	}
   
       
    		return map;
         }

   

   	}
 	@ResponseBody
   	@RequestMapping(value="QueryInfo",method=RequestMethod.POST)
 	@ApiOperation(value="银行查询", notes="测试伙伴们，这个里面需要的数据 在登陆返回的里面取")//设置标题描述
   	public Map<String,Object>  QueryInfo(HttpServletRequest request,HttpServletResponse response,@RequestParam("code") String code,@RequestParam("sessid") String sessid,@RequestParam("ClientNo") String ClientNo,@RequestParam("idCard") String idCard,@RequestParam("UUID") String UUID) throws Exception {
 		System.out.println("heeli man");
 		HttpSession session = request.getSession();
   		
  return mobileService.Queryinfo(session, response,code,sessid,ClientNo,idCard,UUID);
   		
   	}
 	/**
 	 * 储蓄卡查询
 	 * @param request
 	 * @param response
 	 * @param code
 	 * @param sessid
 	 * @param ClientNo
 	 * @param idCard
 	 * @return
 	 * @throws Exception
 	 */
 	@ResponseBody
 	@RequestMapping(value="CmbQueryInfo",method=RequestMethod.POST)
 	@ApiOperation(value="银行查询", notes="测试伙伴们，这个里面需要的数据 在登陆返回的里面取")//设置标题描述
 	public Map<String,Object>  CmbQueryInfo(HttpServletRequest request,HttpServletResponse response,@RequestParam("code") String code,@RequestParam("sessid") String sessid,@RequestParam("ClientNo") String ClientNo,@RequestParam("idCard") String idCard,@RequestParam("UUID") String UUID,@RequestParam("Sendcode") String Sendcode) throws Exception {
 		System.out.println("heeli man");
 		HttpSession session = request.getSession();
 		
 		return mobileService.CmbQueryInfo(code,sessid,ClientNo,idCard,request,UUID,Sendcode);
 		
 	}


   	


   	
   	
}
