package com.youzan.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.youzan.service.IntegrationService;
import com.youzan.utils.HttpClientUtils;

import net.sf.json.JSONObject;

@RestController
@RequestMapping("/integration")
public class IntegrationController {
	
	protected Logger logger = Logger.getLogger(IntegrationController.class);
	
	private static final String redirectUri = "http://tea.tunnel.qydev.com/integration_backend/requestToken";
	private static final String tradeUrl = "http://tea.tunnel.qydev.com/integration_backend/trade";
	
	
	@Autowired
	private IntegrationService integrationService;
	
	
	@RequestMapping(value = "/trade",method= RequestMethod.POST,produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String takeTrade(HttpServletRequest request ,HttpServletResponse response,@RequestBody String data) throws IOException{
		Map<String,String> result = new HashMap<String,String>();
		
		String tenantId = "";
		String path = "";
		if(StringUtils.isBlank(data) || JSONObject.fromObject(data).isEmpty()){
			result.put("code", "error");
			result.put("errmsg", "入参为空");
			return JSONObject.fromObject(result).toString();
		}
		
		logger.info("takeTrade data:"+data);
		JSONObject json = JSONObject.fromObject(data);
		tenantId = json.getString("id");
		path = json.getString("path");
		
		logger.info("tenantId :"+tenantId);
		logger.info("path:"+path);
		Date nowTime = new Date();
		result = integrationService.integrationProcess(Long.parseLong(tenantId),path,nowTime);
		
		String rescode= result.get("code");
		if(("tokenExpired").equals(rescode) || "blankToken".equals(rescode)){
			logger.info("有赞 token为空或过期，需重新获取");
			String client_id = result.get("PEER_APP_ID");
			String secret = result.get("PEER_APP_SECRET");
			request.getSession().setAttribute("PEER_APP_ID", client_id);
			request.getSession().setAttribute("PEER_APP_SECRET", secret);
			response.sendRedirect(getYouZanAccessTokenUrl(client_id,tenantId,path));
			return null;
		}
		
		/*code = success 时
		 * 需要同时返回 
		 * 	1.本次应同步订单总数以及实际同步总数
		 *  2.本次同步时间
		 * */
		//记录本次同步历史
		//更新lastupdate
		if("success".equals(rescode)){
			integrationService.updateAndRecord(result, nowTime, tenantId);
		}
		
		String returnStr = JSONObject.fromObject(result).toString();
		logger.info("integrationProcess result:"+returnStr);
		return returnStr;
	}
	
	@RequestMapping("/requestToken")
	public void requestYouZanAccesstoken(HttpServletRequest request,HttpServletResponse response){
		String code = request.getParameter("code");
		/*
		 * 1.调用有赞API获取accessToken 
		 * 2.从session中取出id，更新token，过期时间以及refreshToken
		 * 3.将id和path拼装json，调用trade接口
		 * */
		if(StringUtils.isBlank(code)){
			logger.info("code 为空");
			return;
		}
		String id = request.getParameter("id");
		String path = request.getParameter("path");
		String res = integrationService.updateToken(code, id);
		if("error".equals(res)){
			logger.info("刷新有赞Token异常");
			return;
		}
		logger.info("刷新后的token："+res);
		//重新调用trade接口
		JSONObject reqParam = new JSONObject();
		reqParam.put("id", id);
		reqParam.put("path",path);
		String tradeRes = HttpClientUtils.post(tradeUrl, reqParam.toString());
		logger.info("tradeRes:"+tradeRes);
	}
	
	
	private String getYouZanAccessTokenUrl(String clientId,String id,String path){
		return String.format("https://open.youzan.com/oauth/authorize?client_id=%s&response_type=code&state=convertlab&redirect_uri=%s",
				clientId,URLEncoder.encode(redirectUri+"?id="+id+"&path="+path));
	}
}
