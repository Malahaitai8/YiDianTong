package com.example.springboot.dto;

import java.util.List;

/**
 * 批量查询候补人数请求
 */
public class WaitlistCountRequest {

    private List<Long> scheduleIds;

    public List<Long> getScheduleIds() {
        return scheduleIds;
    }

    public void setScheduleIds(List<Long> scheduleIds) {
        this.scheduleIds = scheduleIds;
    }
}



