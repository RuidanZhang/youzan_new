package com.youzan.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="tenant")
public class Tenant {
    private Long id;

    private String userName;

    private String youzanAppId;

    private String youzanAppSecret;

    private String convertAppId;

    private String convertAppSecret;

    private Date lastUpdate;

    private String token;

    private Date expiredtime;

    private String refreshtoken;
    
    private String kdtid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getYouzanAppId() {
        return youzanAppId;
    }

    public void setYouzanAppId(String youzanAppId) {
        this.youzanAppId = youzanAppId == null ? null : youzanAppId.trim();
    }

    public String getYouzanAppSecret() {
        return youzanAppSecret;
    }

    public void setYouzanAppSecret(String youzanAppSecret) {
        this.youzanAppSecret = youzanAppSecret == null ? null : youzanAppSecret.trim();
    }

    public String getConvertAppId() {
        return convertAppId;
    }

    public void setConvertAppId(String convertAppId) {
        this.convertAppId = convertAppId == null ? null : convertAppId.trim();
    }

    public String getConvertAppSecret() {
        return convertAppSecret;
    }

    public void setConvertAppSecret(String convertAppSecret) {
        this.convertAppSecret = convertAppSecret == null ? null : convertAppSecret.trim();
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public Date getExpiredtime() {
        return expiredtime;
    }

    public void setExpiredtime(Date expiredtime) {
        this.expiredtime = expiredtime;
    }

    public String getRefreshtoken() {
        return refreshtoken;
    }

    public void setRefreshtoken(String refreshtoken) {
        this.refreshtoken = refreshtoken == null ? null : refreshtoken.trim();
    }

	public String getKdtid() {
		return kdtid;
	}

	public void setKdtid(String kdtid) {
		this.kdtid = kdtid;
	}
}