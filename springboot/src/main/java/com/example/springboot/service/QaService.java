package com.example.springboot.service;

import com.example.springboot.dto.QaAskRequest;
import com.example.springboot.dto.QaMatchedFaqDTO;
import com.example.springboot.dto.QaTopQuestionDTO;
import com.example.springboot.entity.SystemConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.util.*;
import java.util.stream.Collectors;

@Service
public class QaService {

    private static final String KEY_FAQS = "qa.faqs";   // JSON 数组: [{question, answer}]
    private static final String KEY_STATS = "qa.stats"; // JSON 对象: { "question": count, ... }

    @Resource
    private SystemConfigService systemConfigService;
    @Resource
    private DeepSeekService deepSeekService;
    @Resource
    private QuestionLogService questionLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<QaTopQuestionDTO> getTopQuestions(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        List<String> q_list = questionLogService.getRecentQuestionsForAnalysis(limit);
        System.out.println(q_list.get(1));

        String prompt = "请分析以下问题列表，统计出频率最高的前3个问题。要求：\n" +
                "1. 合并语义相同或类似的问题\n" +
                "3. 统计每个类别的出现频率\n" +
                "3. 返回严格的JSON格式，不要包含其他内容\n\n" +
                "问题列表：\n" +
                String.join("\n", q_list) + "\n\n" +
                "请返回以下JSON格式：\n" +
                "[\n" +
                "  {\n" +
                "    \"Question\": \"代表性问法\",\n" +
                "    \"count\": 出现次数\n" +
                "  }\n" +
                "]";

        String response = deepSeekService.chat(prompt);
        System.out.println(response);
        List<QaTopQuestionDTO> list = processDpString(response);

        return list;

//        if (limit == null || limit <= 0) {
//            limit = 10;
//        }
//        SystemConfig sc = systemConfigService.selectByKey(KEY_STATS);
//        if (sc == null || !StringUtils.hasText(sc.getValue())) {
//            return Collections.emptyList();
//        }
//        try {
//            Map<String, Integer> map = objectMapper.readValue(
//                    sc.getValue(),
//                    new TypeReference<Map<String, Integer>>() {}
//            );
//            return map.entrySet().stream()
//                    .sorted((a, b) -> Integer.compare(
//                            Optional.ofNullable(b.getValue()).orElse(0),
//                            Optional.ofNullable(a.getValue()).orElse(0)))
//                    .limit(limit)
//                    .map(e -> {
//                        QaTopQuestionDTO dto = new QaTopQuestionDTO();
//                        dto.setQuestion(e.getKey());
//                        dto.setCount(Optional.ofNullable(e.getValue()).orElse(0));
//                        return dto;
//                    })
//                    .collect(Collectors.toList());
//        } catch (Exception e) {
//            return Collections.emptyList();
//        }




    }





    private List<QaTopQuestionDTO> processDpString(String response){
        try {
            // 提取纯JSON字符串
            String jsonStr = extractPureJson(response);

            // 使用ObjectMapper解析JSON
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> resultList = objectMapper.readValue(jsonStr,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));

            List<QaTopQuestionDTO> list = new ArrayList<>();

            for (Map<String, Object> item : resultList) {
                String question = (String) item.get("Question");
                Integer count = (Integer) item.get("count");

                // 创建DTO对象并添加到列表
                QaTopQuestionDTO dto = new QaTopQuestionDTO(question, count);
                list.add(dto);
            }

            return list;

        } catch (Exception e) {
            e.printStackTrace();
            // 如果解析失败，返回空列表
            return new ArrayList<>();
        }
    }

    private String extractPureJson(String response) {
        // 方法1: 尝试直接解析
        try {
            new ObjectMapper().readTree(response);
            return response;
        } catch (Exception e) {
            // 忽略，尝试其他方法
        }

        // 方法2: 提取 ```json ``` 之间的内容
        if (response.contains("```json")) {
            String[] parts = response.split("```json");
            if (parts.length > 1) {
                String jsonPart = parts[1].split("```")[0].trim();
                return jsonPart;
            }
        }

        // 方法3: 提取 [ ... ] 之间的内容
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');

        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }

        // 方法4: 返回原始响应，让解析器处理
        return response;
    }

    public List<QaMatchedFaqDTO> keywordMatch(String query, Integer topK) {
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }
        if (topK == null || topK <= 0) {
            topK = 5;
        }
        SystemConfig sc = systemConfigService.selectByKey(KEY_FAQS);
        if (sc == null || !StringUtils.hasText(sc.getValue())) {
            return Collections.emptyList();
        }
        try {
            List<Map<String, String>> faqs = objectMapper.readValue(
                    sc.getValue(),
                    new TypeReference<List<Map<String, String>>>() {}
            );
            String q = query.trim().toLowerCase(Locale.ROOT);
            List<QaMatchedFaqDTO> scored = new ArrayList<>();
            for (Map<String, String> item : faqs) {
                String question = Optional.ofNullable(item.get("question")).orElse("");
                String answer = Optional.ofNullable(item.get("answer")).orElse("");
                String text = (question + " " + answer).toLowerCase(Locale.ROOT);
                double score = simpleKeywordScore(q, text);
                if (score > 0) {
                    QaMatchedFaqDTO dto = new QaMatchedFaqDTO();
                    dto.setQuestion(question);
                    dto.setAnswer(answer);
                    dto.setScore(score);
                    scored.add(dto);
                }
            }
            return scored.stream()
                    .sorted(Comparator.comparingDouble(QaMatchedFaqDTO::getScore).reversed())
                    .limit(topK)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private double simpleKeywordScore(String query, String text) {
        // 朴素相似度：包含词数/长度加权 + 子串匹配奖励
        String[] tokens = query.split("\\s+");
        int hits = 0;
        for (String t : tokens) {
            if (t.length() == 0) continue;
            if (text.contains(t)) hits++;
        }
        double base = tokens.length == 0 ? 0.0 : (double) hits / (double) tokens.length;
        if (text.contains(query)) {
            base += 0.2;
        }
        return Math.min(base, 1.0);
    }
}


