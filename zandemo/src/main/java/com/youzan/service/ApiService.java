package com.youzan.service;

import net.sf.json.JSONArray;

public interface ApiService {
	
	/**
	 * 调用批量同步订单接口
	 * @param params
	 * @return
	 */
	public String dealGroup(JSONArray params,String appid,String secret,String path);

	public String searchCustomer(String idenValue);
	
	public String addIdentity(String identityValue,String customerId);
	
	public String migrateIdentity(String idenValue,String toCusId);

	 public String getCustomer(String mobile);
}
