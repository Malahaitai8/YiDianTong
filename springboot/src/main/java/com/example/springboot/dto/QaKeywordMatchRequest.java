package com.example.springboot.dto;

import lombok.Data;

@Data
public class QaKeywordMatchRequest {
    /**
     * 用户输入的查询内容
     */
    private String query;
    /**
     * 返回的匹配条目数量上限，默认 5
     */
    private Integer topK;
}


