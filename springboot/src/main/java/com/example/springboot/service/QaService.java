package com.example.springboot.service;

import com.example.springboot.dto.QaMatchedFaqDTO;
import com.example.springboot.dto.QaTopQuestionDTO;
import com.example.springboot.entity.SystemConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<QaTopQuestionDTO> getTopQuestions(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        SystemConfig sc = systemConfigService.selectByKey(KEY_STATS);
        if (sc == null || !StringUtils.hasText(sc.getValue())) {
            return Collections.emptyList();
        }
        try {
            Map<String, Integer> map = objectMapper.readValue(
                    sc.getValue(),
                    new TypeReference<Map<String, Integer>>() {}
            );
            return map.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(
                            Optional.ofNullable(b.getValue()).orElse(0),
                            Optional.ofNullable(a.getValue()).orElse(0)))
                    .limit(limit)
                    .map(e -> {
                        QaTopQuestionDTO dto = new QaTopQuestionDTO();
                        dto.setQuestion(e.getKey());
                        dto.setCount(Optional.ofNullable(e.getValue()).orElse(0));
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
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


