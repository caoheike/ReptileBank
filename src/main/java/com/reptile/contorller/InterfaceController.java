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

import com.reptile.Bank.AbcBank;
import com.reptile.Bank.BcmLogin;
import com.reptile.Bank.BcmLogins;
import com.reptile.Bank.CMBService;
import com.reptile.analysis.BcmCreditAnalysis;
import com.reptile.service.AbcSavingService;
import com.reptile.service.BcmSavingService;
import com.reptile.service.CmbSavingsService;
import com.reptile.service.CmbcCreditService;
import com.reptile.service.MobileService;
import com.reptile.service.SPDBService;
import com.reptile.util.ConstantInterface;
import com.reptile.util.MessageConstamts;
import com.reptile.util.RedisSourceUtil;
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
public class InterfaceController {

	@Resource
	private MobileService mobileService;
	@Resource 
	private RedisSourceUtil redisSourceUtil;
	@Resource 
	private CmbcCreditService msBank;
	@Resource 
	private BcmCreditAnalysis bcmCreditAnalysis;
	

	@ResponseBody
	@RequestMapping(value = "BankLogin", method = RequestMethod.POST)
	@ApiOperation(value = "银行登陆", notes = "银行登陆")
	
	// 设置标题描述
	public Map<String, Object> Login(HttpServletRequest request,
			HttpServletResponse response, @RequestParam("number") String numbe,
			@RequestParam("pwd") String pwd,
			@RequestParam("BankType") String BankType,
			@RequestParam("flag") boolean flag
			) throws Exception {
		VirtualKeyBoard bank = new VirtualKeyBoard();
		CmbBank banks = new CmbBank();
		BcmLogin BcmLogin = new BcmLogin();
		BcmLogins JiaoTong = new BcmLogins();
		CmbSavingsService cmbs = new CmbSavingsService();
		CMBService CMB=new CMBService();
		Map<String, Object> map = new HashMap<String, Object>();
		HttpSession session = request.getSession();
		String UUID = request.getParameter("UUID");
//		String UUID = "54557451454248+jhfjdkshfdsj";
		String userCard = request.getParameter("userCard");
//		String userCard = "610111199203252021";
		String timeCnt = request.getParameter("timeCnt");
//		String timeCnt = "2017-12-12";
		System.out.println("---*****************-----*****************-------userCard:"+userCard);
		synchronized (this) {			
			if (BankType.equals("CMB")) {// 招商银行
				map = bank.Login(numbe, pwd, session, UUID);
			} else if (BankType.equals("CXCMB")) {// 招商储蓄卡
				map = banks.CMBLogin(numbe, pwd, request, userCard, UUID,flag);
			} else if (BankType.equals("SPDB")) {// 浦发银行
				map = new SPDBService().login(request, userCard, pwd, UUID);
			} else if (BankType.equals("BOC")) {

			} else if (BankType.equals("CMBC")) {// 民生信用卡
				map = msBank.doLogin(request,numbe, pwd, BankType, userCard, UUID,timeCnt);
			} else if (BankType.equals("CMBC2")) {// 民生储蓄卡
				map = cmbs.login(request, response, numbe, pwd,
						userCard, UUID, flag);
			} else if (BankType.equals("GDB")) {// 广发银行信用卡
				map = bank.GDBLogin(numbe, pwd, userCard, UUID,timeCnt);
			} else if (BankType.equals("ABC")) {// 农业银行储蓄卡
				map = AbcSavingService.doGetDetail(numbe, pwd, UUID, userCard,session,request,flag);
			} else if (BankType.equals("BCM")) {// 交通银行
				map = BcmSavingService.BcmLogins(request,numbe, pwd, UUID,userCard,flag);
			} else if (BankType.equals("X-BCM")) {// 交通银行 信用卡
				map = JiaoTong.BankLogin(numbe, pwd, userCard, request, UUID,timeCnt);//jiaotong 
			}
			
			return map;
		}

	}
/**
 * 招商信用卡
 * 
 * @param request
 * @param response
 * @param code
 * @param sessid
 * @param ClientNo
 * @param idCard
 * @param timeCnt
 * @param numbe
 * @return
 * @throws Exception
 */
	@ResponseBody
	@RequestMapping(value = "QueryInfo", method = RequestMethod.POST)
	@ApiOperation(value = "银行查询", notes = "测试伙伴们，这个里面需要的数据 在登陆返回的里面取")
	// 设置标题描述
	public Map<String, Object> QueryInfo(HttpServletRequest request,
			HttpServletResponse response, @RequestParam("code") String code,
			@RequestParam("sessid") String sessid,
			@RequestParam("ClientNo") String ClientNo,
			@RequestParam("idCard") String idCard,
			@RequestParam("timeCnt") String timeCnt, @RequestParam("number") String numbe) throws Exception {
		System.out.println("heeli man");
		HttpSession session = request.getSession();
		String UUID = request.getParameter("UUID");
		return mobileService.Queryinfo(session, response, code, sessid,
				ClientNo, idCard, UUID,timeCnt,numbe);
	}

	
	/**
	 * 农业储蓄卡查询
	 * @param request
	 * @param response
	 * @param code
	 * @param idCard
	 * @param UUID
	 * @param numbe
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "abcQueryInfo", method = RequestMethod.POST)
	@ApiOperation(value = "银行查询", notes = "测试伙伴们，这个里面需要的数据 在登陆返回的里面取")
	// 设置标题描述
	public Map<String, Object> abcQueryInfo(HttpServletRequest request,
			HttpServletResponse response, @RequestParam("code") String code,			
			@RequestParam("idCard") String idCard,
			@RequestParam("UUID") String UUID, @RequestParam("number") String numbe,
			@RequestParam("flag") boolean flag) throws Exception {
		System.out.println("heeli man");
		HttpSession session = request.getSession();

		return AbcSavingService.abcQueryInfo(code, idCard,
				session, UUID,numbe, request,flag);

	}
	/**
	 * 招商储蓄卡数据获取
	 * @param request
	 * @param response
	 * @param code
	 * @param sessid
	 * @param ClientNo
	 * @param idCard
	 * @param UUID
	 * @param Sendcode
	 * @param numbe
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "CmbQueryInfo", method = RequestMethod.POST)
	@ApiOperation(value = "银行查询", notes = "测试伙伴们，这个里面需要的数据 在登陆返回的里面取")
	// 设置标题描述
	public Map<String, Object> CmbQueryInfo(HttpServletRequest request,
			HttpServletResponse response, @RequestParam("code") String code,
			@RequestParam("sessid") String sessid,
			@RequestParam("ClientNo") String ClientNo,
			@RequestParam("idCard") String idCard,
			@RequestParam("UUID") String UUID,
			@RequestParam("Sendcode") String Sendcode,
			@RequestParam("number") String numbe,
			@RequestParam("flag") boolean flag) throws Exception {
		System.out.println("heeli man");
		HttpSession session = request.getSession();

		return mobileService.CmbQueryInfo(code, sessid, ClientNo, idCard,
				request, UUID, Sendcode,numbe,flag);

	}
	
	
	
	/*
	 * 交通储蓄卡发送短信验证码
	 
	
	@ResponseBody
	@RequestMapping(value = "BCMSendCode", method = RequestMethod.POST)
	@ApiOperation(value = "银行查询", notes = "测试伙伴们，这个里面需要的数据 在登陆返回的里面取")
	// 设置标题描述
	public Map<String, Object> BCMSendCode(HttpServletRequest request) throws Exception {

		return BcmLogin.BCMSendCode(request);

	}

	
	 * 交通储蓄卡获取数据
	 
	
	@ResponseBody
	@RequestMapping(value = "BCMQueryInfo", method = RequestMethod.POST)
	@ApiOperation(value = "银行查询", notes = "测试伙伴们，这个里面需要的数据 在登陆返回的里面取")
	// 设置标题描述
	public Map<String, Object> BCMQueryInfo(HttpServletRequest request,
			HttpServletResponse response, @RequestParam("code") String code,
			@RequestParam("userCard") String userCard,
			@RequestParam("Sendcode") String Sendcode,			
			@RequestParam("UUID") String UUID, @RequestParam("number") String numbe
			) throws Exception {

		return BcmLogin.BCMQueryInfo(request,UUID,userCard,Sendcode,numbe);

	}
*/
	 @ApiOperation(value = "人法网开关", notes = "")
	    @ResponseBody
	    @RequestMapping(value = "RenFaSwitch", method = RequestMethod.POST)
	    public Map<String,Object> renFaSwitch(HttpServletRequest request, HttpServletResponse response) {
	      Map<String,Object> map=new HashMap<String,Object>(8);
	      String rest=redisSourceUtil.getValue(ConstantInterface.lawStatus);
	       if(rest.contains(MessageConstamts.OPEN_YES)){
	         map.put("errorCode",MessageConstamts.STRING_0000);
	         map.put("errorInfo", MessageConstamts.STRING_RENFA);
	       }else{
	         map.put("errorCode",MessageConstamts.STRING_0001);
	         map.put("errorInfo",MessageConstamts.STRING_RENFA_CLOSE);
	       }
	       
	          
	      return map;

	      
	      
	    }
	
	
}
