package com.youzan.service.impl;

import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youzan.dao.TenantDao;
import com.youzan.open.sdk.client.auth.Token;
import com.youzan.open.sdk.client.core.DefaultYZClient;
import com.youzan.open.sdk.client.core.YZClient;
import com.youzan.open.sdk.gen.v3_0_0.api.YouzanItemGet;
import com.youzan.open.sdk.gen.v3_0_0.api.YouzanItemcategoriesTaglistSearch;
import com.youzan.open.sdk.gen.v3_0_0.api.YouzanShopGet;
import com.youzan.open.sdk.gen.v3_0_0.api.YouzanTradesSoldGet;
import com.youzan.open.sdk.gen.v3_0_0.api.YouzanUsersWeixinFollowerGet;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanItemGetParams;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanItemGetResult;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanItemGetResult.GroupOpenModel;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanItemGetResult.ItemDetailOpenModel;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanItemcategoriesTaglistSearchParams;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanItemcategoriesTaglistSearchResult;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanItemcategoriesTaglistSearchResult.GoodsTag;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanShopGetParams;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanShopGetResult;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanTradesSoldGetParams;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanTradesSoldGetResult;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanTradesSoldGetResult.TradeDetailV2;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanUsersWeixinFollowerGetParams;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanUsersWeixinFollowerGetResult.CrmWeixinFans;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanUsersWeixinFollowerGetResult;
import com.youzan.service.YouZanTradeService;
import com.youzan.utils.HttpClientUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service("youZanTradeService")
public class YouZanTradeServiceImpl implements YouZanTradeService{
	
	protected Logger logger = Logger.getLogger(YouZanTradeServiceImpl.class);
	
	private static final Long pageSize = 40L;
	private static final String redirectUri = "http://tea.tunnel.qydev.com/integration_backend/requestToken";

	
	@Autowired
	private TenantDao tenantDao;
	
	@Override
	public List<TradeDetailV2> getYouZanTrade(Date lastUpdate,Date currentTime,String token) {
		List<TradeDetailV2> list = new ArrayList<TradeDetailV2>();
		try {
			YZClient client = new DefaultYZClient(new Token(token)); //new Sign(appKey, appSecret)
			boolean hasnext = true;
			Long i =1l;
			while (hasnext){

				YouzanTradesSoldGetParams youzanTradesSoldGetParams = new YouzanTradesSoldGetParams();
				//设置入参
				youzanTradesSoldGetParams.setPageSize(pageSize);
				youzanTradesSoldGetParams.setPageNo(i);
				youzanTradesSoldGetParams.setStartUpdate(lastUpdate);
				youzanTradesSoldGetParams.setEndUpdate(currentTime);
				youzanTradesSoldGetParams.setUseHasNext(true);

				YouzanTradesSoldGet youzanTradesSoldGet = new YouzanTradesSoldGet();
				youzanTradesSoldGet.setAPIParams(youzanTradesSoldGetParams);
				YouzanTradesSoldGetResult result = client.invoke(youzanTradesSoldGet);
				logger.info("trades sold get result:"+result);
				TradeDetailV2[] trades = result.getTrades();
				for (TradeDetailV2 trade : trades) {
					list.add(trade);
				}
				
				hasnext = result.getHasNext();
				logger.info("trades sold get hasnext:"+hasnext);
				i++;
			}
		} catch (Exception e) {
			logger.error("get youzanTrade error");
			logger.error(e.getMessage(),e);
			return null;
		}
		return list;
	}

	@Override
	public CrmWeixinFans getWeiXinFansInfo(String weixinUserId,String token) {
		try {
			YZClient client = new DefaultYZClient(new Token(token));
			YouzanUsersWeixinFollowerGetParams getParam = new YouzanUsersWeixinFollowerGetParams();
			getParam.setFansId(Long.parseLong(weixinUserId));
			YouzanUsersWeixinFollowerGet get = new YouzanUsersWeixinFollowerGet();
			get.setAPIParams(getParam);
			YouzanUsersWeixinFollowerGetResult result =  client.invoke(get);
			CrmWeixinFans fans =  result.getUser();
			return fans;
		} catch (Exception e) {
			logger.error("get youzan weixin fans error");
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	
	public String getShopname(String token){
		try {
			YZClient client = new DefaultYZClient(new Token(token)); //new Sign(appKey, appSecret)
			YouzanShopGetParams youzanShopGetParams = new YouzanShopGetParams();
			
			YouzanShopGet youzanShopGet = new YouzanShopGet();
			youzanShopGet.setAPIParams(youzanShopGetParams);
			YouzanShopGetResult result = client.invoke(youzanShopGet);
			return result.getName();
		} catch (Exception e) {
			logger.error("查询店铺名称异常");
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	public Map<Long,String> getItemTagMap(String token){
		try {
			YZClient client = new DefaultYZClient(new Token(token)); //new Sign(appKey, appSecret)
			YouzanItemcategoriesTaglistSearchParams youzanItemcategoriesTaglistSearchParams = new YouzanItemcategoriesTaglistSearchParams();

			youzanItemcategoriesTaglistSearchParams.setPageSize(10L);
			youzanItemcategoriesTaglistSearchParams.setPageNo(1L);

			YouzanItemcategoriesTaglistSearch youzanItemcategoriesTaglistSearch = new YouzanItemcategoriesTaglistSearch();
			youzanItemcategoriesTaglistSearch.setAPIParams(youzanItemcategoriesTaglistSearchParams);
			YouzanItemcategoriesTaglistSearchResult result = client.invoke(youzanItemcategoriesTaglistSearch);
			Long count = result.getTotalResults();
			if(count <=0){
				logger.info("getItemTag count:"+count);
				return null;
			}
			GoodsTag[] tags = result.getTags();
			Map<Long,String> tagMap = new HashMap<Long,String>();
			for (GoodsTag tag : tags) {
				tagMap.put(tag.getId(), tag.getName());
			}
			return tagMap;
		} catch (Exception e) {
			logger.error("get YouZan Goods Tag array error");
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	public List<String> getYouZanItem(String token ,Long itemNum){
		try {
			List<String> tags = new ArrayList<String>();
			YZClient client = new DefaultYZClient(new Token(token)); //new Sign(appKey, appSecret)
			YouzanItemGetParams youzanItemGetParams = new YouzanItemGetParams();

			youzanItemGetParams.setItemId(itemNum);

			YouzanItemGet youzanItemGet = new YouzanItemGet();
			youzanItemGet.setAPIParams(youzanItemGetParams);
			String result =  client.execute(youzanItemGet);
			logger.info("getYouZanItemTag :"+result);
			JSONObject response = JSONObject.fromObject(result).getJSONObject("response");
			JSONObject item = response.getJSONObject("item");
			JSONArray tagArr = item.getJSONArray("item_tags");	
			if(tagArr!=null && tagArr.size()>0){
				for (Object obj : tagArr) {
					JSONObject tag = JSONObject.fromObject(obj);
					tags.add(tag.getString("name"));
				}
				return tags;
			}
				
//			YouzanItemGetResult result = client.invoke(youzanItemGet);
//			ItemDetailOpenModel model = result.getItem();
//			GroupOpenModel[] groupmodel = model.getItemTags();
//			if(groupmodel != null && groupmodel.length > 0){
//				for (GroupOpenModel groupOpenModel : groupmodel) {
//					tags.add(groupOpenModel.getName());
//				}
//				return tags;
//			}
		} catch (Exception e) {
			logger.error("get Youzan Item Tags error");
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	//自用型应用获取token的方法
	public String getYouzanToken(String appid,String secrct,String kdtId){
		String time = getFormatTime(new Date());
		StringBuffer buffer = new StringBuffer();
		buffer.append("client_id="+appid);
		buffer.append("&client_secret="+secrct);
		buffer.append("&grant_type=silent");
		buffer.append("&kdt_id="+kdtId);
		String tokenRes = HttpClientUtils.postForYouZan(tokenUrl, buffer.toString());
		logger.info("request youzan token result:"+tokenRes);
		if("error".equals(tokenRes)){
			logger.info("请求 有赞 Token 异常");
		}
		//判断返回值是否为异常信息
		JSONObject resJson = JSONObject.fromObject(tokenRes);
		if(resJson.containsKey("error")){
			logger.info("请求有赞accessToken 异常："+resJson.getString("error_description"));
			return "error";
		}
		resJson.put("requestTime", time);
		return resJson.toString();
	}
	
	//工具型应用获取token的方法
	private static final String tokenUrl="http://open.youzan.com/oauth/token";
	
	public String getYouzanAccessToken(String code,String appid,String secret){
		//2&redirect_uri=http://youzanyun.com/callback
		String time = getFormatTime(new Date());
		StringBuffer buffer = new StringBuffer();
		buffer.append("client_id="+appid);
		buffer.append("&client_secret="+secret);
		buffer.append("&grant_type=authorization_code");
		buffer.append("&code="+code);
		buffer.append("&redirect_uri="+URLEncoder.encode(redirectUri));
		String response = HttpClientUtils.postForYouZan(tokenUrl, buffer.toString());
		logger.info("youzan tokenResponse:"+response);
		if("error".equals(response)){
			logger.info("请求有赞accessToken异常");
		}
		
		//判断返回值是否为异常信息
		JSONObject resJson = JSONObject.fromObject(response);
		if(resJson.containsKey("error")){
			logger.info("请求有赞accessToken 异常："+resJson.getString("error_description"));
			return "error";
		}
		resJson.put("requestTime", time);
		return resJson.toString();
		
	}
	
	public String refreshAccessToken(String refreshToken,String clientId,String clientSecret){
		String time = getFormatTime(new Date());
		StringBuffer buffer = new StringBuffer();
		buffer.append("client_id="+clientId);
		buffer.append("&client_secret="+clientSecret);
		buffer.append("&grant_type=refresh_token");
		buffer.append("&refresh_token="+refreshToken);
		buffer.append("&scope=");
		String response = HttpClientUtils.postForYouZan(tokenUrl, buffer.toString());
		logger.info("youzan refresh tokenResponse:"+response);
		if("error".equals(response)){
			logger.info("有赞accessToken异常");
		}
		
		//判断返回值是否为异常信息
		JSONObject resJson = JSONObject.fromObject(response);
		if(resJson.containsKey("error")){
			logger.info("请求有赞accessToken 异常："+resJson.getString("error_description"));
			return "error";
		}
		resJson.put("requestTime", time);
		return resJson.toString();
	}
	
	public String updateAppToken(String param,String id) throws Exception{
		JSONObject json = JSONObject.fromObject(param);
		if(json.containsKey("error")){
			logger.info("请求或刷新有赞Token异常，return error");
			return "error";
		}
		String accesstoken = json.getString("access_token");
		String time = json.getString("requestTime");
		String refreshToken = "";
		if(json.containsKey("refresh_token")){
			refreshToken = json.getString("refresh_token");
		}
		//将过期时间缩减两小时
		int expires_in = json.getInt("expires_in") - 7200;
		Calendar c = Calendar.getInstance();
		c.setTime(parseDate(time));
		c.add(Calendar.SECOND, expires_in);
		Date expiredTime = c.getTime();	
		//更新tenant
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("id", id);
		params.put("token", accesstoken);
		params.put("refreshtoken", refreshToken);
		params.put("time", expiredTime);
		tenantDao.updateTenant(params);
		return accesstoken;
	}
	
	private String getFormatTime(Date date){
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
	}
	
	
	private Date parseDate(String time) throws ParseException{
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
	}
}
