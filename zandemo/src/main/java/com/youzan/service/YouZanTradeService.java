package com.youzan.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.youzan.open.sdk.gen.v3_0_0.model.YouzanTradesSoldGetResult.TradeDetailV2;
import com.youzan.open.sdk.gen.v3_0_0.model.YouzanUsersWeixinFollowerGetResult.CrmWeixinFans;

public interface YouZanTradeService {
	
	/**
	 * 分页查询有赞订单
	 * @param lastUpdate
	 * @return 
	 */
	public List<TradeDetailV2> getYouZanTrade(Date lastUpdate,Date currentTime,String token);
	
	/**
	 * 查询微信用户信息
	 * @param weixinUserId
	 * @return 
	 */
	public CrmWeixinFans getWeiXinFansInfo(String weixinUserId,String token);

	/**
	 * 查询商铺名称
	 * @param appid
	 * @param secret
	 * @return
	 */
	public String getShopname(String token);
	
	
	/**
	 * 查询商品标签
	 * @param token
	 * @return
	 */
	public Map<Long,String> getItemTagMap(String token);
	
	/**
	 * 查询订单中商品的分组标签
	 * @param token
	 * @param itemNum
	 * @return
	 */
	public List<String> getYouZanItem(String token ,Long itemNum);
	
	
	public String getYouzanToken(String appid,String secrct,String kdtId);

	/**
	 * 获取授权access_token
	 * @param code
	 * @param appid
	 * @param secret
	 * @return
	 */
	public String getYouzanAccessToken(String code,String appid,String secret);
	
	/**
	 * 刷新AccessToken
	 * @param refreshToken
	 * @param clientId
	 * @param clientSecret
	 * @return
	 */
	public String refreshAccessToken(String refreshToken,String clientId,String clientSecret);

	
	public String updateAppToken(String param,String id) throws Exception;
}
