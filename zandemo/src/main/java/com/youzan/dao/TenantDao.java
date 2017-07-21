package com.youzan.dao;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.youzan.entity.Tenant;

@Mapper
public interface TenantDao extends BaseDao<Tenant>{

	@Results({
		@Result(property="id",column="id"),
		@Result(property="userName",column="user_name"),
		@Result(property="youzanAppId",column="youzan_app_id"),
		@Result(property="youzanAppSecret",column="youzan_app_secret"),
		@Result(property="convertAppId",column="convert_app_id"),
		@Result(property="convertAppSecret",column="convert_app_secret"),
		@Result(property="lastUpdate",column="last_update"),
		@Result(property="token",column="token"),
		@Result(property="expiredtime",column="expiredTime"),
		@Result(property="refreshtoken",column="refreshToken"),
		@Result(property="kdtid",column="kdtid")
		
	})
	
	
	@Select("select * from tenant where id = #{id}")
	public Tenant getTenant(Long id	);
	
	@Update("update tenant set token = #{token},expiredTime=#{time},refreshToken=#{refreshtoken} where id=#{id}")
	public void updateTenant(Map<String,Object> param);
	
	@Update("update tenant set last_update = #{updatetime} where id = #{id}")
	public void updateTenantUpdatetime(Map<String,Object> param);
	
}
