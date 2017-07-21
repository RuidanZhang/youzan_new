package com.youzan.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youzan.dao.SyncHistoryDao;
import com.youzan.dao.TenantDao;
import com.youzan.entity.SyncHistory;
import com.youzan.entity.Tenant;
import com.youzan.model.Buyer;
import com.youzan.model.DealsModel;
import com.youzan.model.Line;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanTradesSoldGetResult.FansInfo;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanTradesSoldGetResult.TradeDetailV2;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanTradesSoldGetResult.TradeOrderV2;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanUsersWeixinFollowerGetResult.CrmWeixinFans;
import com.youzan.service.ApiService;
import com.youzan.service.IntegrationService;
import com.youzan.service.YouZanTradeService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service("integrationService")
public class IntegrationServiceImpl implements IntegrationService{

	protected Logger logger = Logger.getLogger(IntegrationServiceImpl.class);
	
	@Autowired
	private YouZanTradeService youZanTradeService;
	
	@Autowired
	private ApiService apiService;
	
	@Autowired
	private TenantDao tenantDao;
	
	@Autowired
	private SyncHistoryDao syncHistoryDao;
	
	@Override
	public Map<String,String> integrationProcess(Long tenantId,String path,Date nowTime) {
		/*
		 *1.查询所有订单
		 *2.遍历订单List，获取卖家id
		 *3.查询微信用户信息，非微信用户用有赞用户
		 *4.调用dealgroup接口，推送订单信息
		 *5.返回推送成功数量
		 */
		Map<String,String> map = new HashMap<String,String>();
		int syncCount= 0 ;
		int tradeListSize = 0;
		try {
			Tenant tenant = tenantDao.getTenant(tenantId);
//			String token = tenant.getToken();
//			Date expireTime = tenant.getExpiredtime();
//			String PEER_APP_ID = tenant.getYouzanAppId();
//			String PEER_APP_SECRET = tenant.getYouzanAppSecret();
//			String kdtId = tenant.getKdtid();
			
			Map<String,String> tokenMap = checkYouZanToken(tenant, nowTime);
			if(!"success".equals(tokenMap.get("code"))){
				return tokenMap;
			}
			
			String token = tokenMap.get("token");
			Date lastUpdate = tenant.getLastUpdate();
			
			List<TradeDetailV2> list = youZanTradeService.getYouZanTrade(lastUpdate,nowTime,token);
			if(list == null || list.size()<=0){
				logger.info("tradeList is blank");
				map.put("code", "NoTrade");
				return map;
			}
			logger.info("tradeList size :"+list.size()); 
			tradeListSize = list.size();
			
			syncCount = syncDeal(list, token, tenant, path);
			
			map.put("code", "success");
			map.put("errormsg", "Failed");
			if(syncCount == tradeListSize){
				map.put("errormsg", "Successful");
			}
			map.put("syncCount", String.valueOf(syncCount));
		} catch (Exception e) {
			logger.info("integrationProcess error");
			logger.error(e.getMessage(),e);
		}
		return map;
	}
	
	
	public int syncDeal(List<TradeDetailV2> list,String token,Tenant tenant,String path ){
		logger.info("syncDeal begin");
		int syncCount = 0;
		String LOCAL_APP_ID = tenant.getConvertAppId();
		String LOCAL_APP_SECRET = tenant.getConvertAppSecret();
		String API_ENTRY = path;
		for (TradeDetailV2 trade : list) {
			try {
				String openid = "";
				FansInfo  fans = trade.getFansInfo();
				
				String mobile = trade.getReceiverMobile();
				Buyer buyer = new Buyer();
				buyer.setUserName(fans.getFansNickname());
				buyer.setMobile(mobile);
				buyer.setMobileVerified("true");
				
				Long type = fans.getFansType();
				if(type == 1){
					logger.info("微信用户");
					Long fansid = fans.getFansId();
					logger.info("fans id :"+fansid);
					CrmWeixinFans weixinfans = youZanTradeService.getWeiXinFansInfo(String.valueOf(fansid),token);
					openid = weixinfans.getWeixinOpenid();
					logger.info("微信openid："+openid);
					buyer.setUserName(weixinfans.getNick());
					buyer.setType("wechat");
				}else{
					buyer.setType("有赞");
				}
				
				DealsModel deal = new DealsModel();
				deal.setName(trade.getTid());
				deal.setShippingAddress(trade.getReceiverAddress());
				deal.setAmountPaid(String.valueOf(trade.getPayment()));
				deal.setContactName(trade.getReceiverName());
				deal.setContactTel(trade.getReceiverMobile());
				deal.setDateOrder(getFormatDateTime(trade.getPayTime()));
				deal.setProductQuantity(trade.getNum().toString());
				deal.setSalesChannel("有赞");
				String shopname = youZanTradeService.getShopname(token);
				if(shopname == null || "".equals(shopname)){
					continue;
				}
				logger.info("店铺名称："+shopname);
				deal.setStore(shopname);
				
				List<Line> lines = new ArrayList<Line>();
				TradeOrderV2[] orders = trade.getOrders();
				for (TradeOrderV2 order : orders) {
					Line line = new Line();
					line.setPriceUnit(order.getPrice().toString());
					line.setProductId(order.getOuterItemId());
					line.setQty(order.getNum().toString());
					line.setPriceSubTotal(order.getTotalFee().toString());
					line.setProductName(processProductTag(order,token));
					lines.add(line);
					
				}
				deal.setLines(lines);
				JSONObject json = new JSONObject();
				json.put("deal", deal);
				json.put("buyer", buyer);
				JSONArray arr = new JSONArray();
				arr.add(json);
				logger.info("send deal group:"+arr.toString());
				String dealRes = apiService.dealGroup(arr, LOCAL_APP_ID, LOCAL_APP_SECRET, API_ENTRY);
				if("error".equals(dealRes)){
					logger.info("send deal group error");
					continue;
				}
				syncCount++;
				if(StringUtils.isBlank(openid)){
					logger.info("openid is blank");
					continue;
				}
				
				//根据收货人手机号查询客户信息
				String customerArr = apiService.getCustomer(mobile);
				if(StringUtils.isBlank(customerArr)){
					logger.info("为查询到客户信息或信息查询出错");
					continue;
				}
				String customerId = JSONObject.fromObject(customerArr).getString("id");
				logger.info("customerId："+customerId);
				
				//根据微信Openid查询是否存在已绑定其他用户
				String custoemrRes = apiService.searchCustomer(openid);
				if(StringUtils.isBlank(custoemrRes)){
					logger.info("当前openid 没有绑定的customerId  add wechat identity");
					//添加微信身份
					apiService.addIdentity(openid, customerId);
					continue;
				}
				
				JSONObject customer = JSONArray.fromObject(custoemrRes)
						.getJSONObject(0);
				String cusId = customer.getString("customerId");
				if(cusId.equals(customerId)){
					logger.info("当前openid与当前custId已有绑定关系");
					continue;
				}
				//迁移微信身份
				logger.info("move wechat identity");
				apiService.migrateIdentity(openid, customerId);
			} catch (Exception e) {
				logger.error("推送有赞订单异常");
				logger.error(e.getMessage(),e);
				continue;
			}
		}
		logger.info("syncDeal end count:"+syncCount);
		return syncCount;
	}
	
	private String processProductTag(TradeOrderV2 product,String token){
		Long itemId = product.getItemId();
		List<String> tags = youZanTradeService.getYouZanItem(token, itemId);
		if(tags == null || tags.size()<=0){
			return product.getTitle();
		}
		StringBuffer buffer = new StringBuffer(product.getTitle());
		buffer.append("#FG#");
		for (String tag : tags) {
			buffer.append(tag);
			buffer.append("#XX#");
		}
		String proname = buffer.toString();
		proname = proname.substring(0,(proname.length() - 4));
		return proname;
	}
	
	//刷新有赞Token
	public String updateToken(String code ,String tenantId){
		try {
			Tenant t = tenantDao.getTenant(Long.parseLong(tenantId));
			String clientId = t.getYouzanAppId();
			String clientSecret = t.getYouzanAppSecret();
			String res = "";
			if(StringUtils.isBlank(code)){
				logger.info("自用型应用");
				String kdtId = t.getKdtid();
				res = youZanTradeService.getYouzanToken(clientId, clientSecret, kdtId);
			}else{
				res = youZanTradeService.getYouzanAccessToken(code, clientId, clientSecret);	
			}
			
			if("error".equals(res)){
				logger.info("重新获取token异常");
				return res;
			}
			String token = youZanTradeService.updateAppToken(res, tenantId);
			return token;
		} catch (Exception e) {
			logger.error("刷新YouzanToken异常");
			logger.error(e.getMessage(),e);
		}
		return "error";
	}
	
	public void updateAndRecord(Map<String,String> result,Date nowTime,String id){
		SyncHistory his = new SyncHistory();
		his.setResult(result.get("errormsg"));
		his.setUpdateOrderNum(Integer.parseInt(result.get("syncCount")));;
		his.setUpdateTime(nowTime);
		syncHistoryDao.saveSyncHistory(his);
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("id", id);
		param.put("updatetime", nowTime);
		tenantDao.updateTenantUpdatetime(param);
	}
	
	
	private Map<String,String> checkYouZanToken(Tenant tenant,Date nowTime){
		Map<String,String> tokenMap = new HashMap<String,String>();
		try {
			String token = tenant.getToken();
			Date expireTime = tenant.getExpiredtime();
			String PEER_APP_ID = tenant.getYouzanAppId();
			String PEER_APP_SECRET = tenant.getYouzanAppSecret();
			String kdtId = tenant.getKdtid();
			String tenantId = String.valueOf(tenant.getId());
			
			//自用型应用 
			if(StringUtils.isNotBlank(kdtId) && Long.parseLong(kdtId) >0){
				if(StringUtils.isBlank(token) || 
						(StringUtils.isNotBlank(token)) && nowTime.after(expireTime)){
					token = updateToken(null, tenantId);
				}
			}else{
				if(token == null || "".equals(token) ){
					tokenMap.put("code", "blankToken");
					tokenMap.put("PEER_APP_ID", PEER_APP_ID);
					tokenMap.put("PEER_APP_SECRET", PEER_APP_SECRET);
					return tokenMap;
				}
				
				if(nowTime.after(expireTime)){
					logger.info("token 过期");
					String refreshToken = "";
					String refreshRes = youZanTradeService.refreshAccessToken(refreshToken, PEER_APP_ID, PEER_APP_SECRET);
					if("error".equals(refreshRes)){
						logger.info("刷新有赞token异常");
						tokenMap.put("code", "tokenExpired");
						tokenMap.put("PEER_APP_ID", PEER_APP_ID);
						tokenMap.put("PEER_APP_SECRET", PEER_APP_SECRET);
						return tokenMap;
					}
					token = youZanTradeService.updateAppToken(refreshRes,tenantId.toString());
				}
			}		
			
			if("error".equals(token)){
				logger.info("刷新有赞token异常");
				tokenMap.put("code", "tokenRefreshError");
				return tokenMap;
			}
			
			tokenMap.put("code", "success");
			tokenMap.put("token", token);
		}catch (Exception e) {
			logger.error("刷新有赞token异常");
			logger.error(e.getMessage(),e);
			tokenMap.put("code", "tokenRefreshError");
		}
		return tokenMap;
		
	}
	
	
	static String format = "yyyy-MM-dd HH:mm:ss";
	public static String getFormatDateTime(Date date){
		
		String orderDate = new SimpleDateFormat(format).format(date);
		String str = orderDate.replaceAll(" ", "T");
		str = str.substring(0, 19);
		str = str + "Z";
		return str;
	}
	
	public Date parseDate(String time) throws ParseException{
		Date date = new SimpleDateFormat(format).parse(time);
		return date;
	}
}
