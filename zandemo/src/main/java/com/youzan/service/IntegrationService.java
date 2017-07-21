package com.youzan.service;

import java.util.Date;
import java.util.Map;

public interface IntegrationService {
	
	public Map<String,String> integrationProcess(Long tenantId,String path,Date nowTime);

	
	public String updateToken(String code ,String tenantId);
	
	public void updateAndRecord(Map<String,String> result,Date nowTime,String id);
}
