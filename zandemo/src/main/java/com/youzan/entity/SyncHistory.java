package com.youzan.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="sync_history")
public class SyncHistory {
    private Long id;

    private Date updateTime;

    private Integer updateOrderNum;

    private String result;

    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getUpdateOrderNum() {
        return updateOrderNum;
    }

    public void setUpdateOrderNum(Integer updateOrderNum) {
        this.updateOrderNum = updateOrderNum;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result == null ? null : result.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}