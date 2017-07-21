package com.youzan.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import com.youzan.entity.SyncHistory;

@Mapper
public interface SyncHistoryDao extends BaseDao<SyncHistory>{

	
	@Insert("insert into sync_history(update_time,update_order_num,`result`) values(#{updateTime},#{updateOrderNum},#{result})")
	public void saveSyncHistory(SyncHistory his);
	
}
