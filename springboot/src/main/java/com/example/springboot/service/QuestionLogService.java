package com.example.springboot.service;

import com.example.springboot.entity.QuestionLog;
import com.example.springboot.mapper.QuestionLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class QuestionLogService {

    private final QuestionLogMapper questionLogMapper;

    public QuestionLogService(QuestionLogMapper questionLogMapper) {
        this.questionLogMapper = questionLogMapper;
    }

    /**
     * 记录用户问题
     */
    public void logQuestion(Long userId, String questionContent) {
        try {
            QuestionLog questionLog = new QuestionLog(userId, questionContent);
            questionLogMapper.insert(questionLog);
            log.info("记录用户问题成功: userId={}, question={}", userId, questionContent);
        } catch (Exception e) {
            log.error("记录用户问题失败: userId={}, question={}", userId, questionContent, e);
        }
    }

    /**
     * 获取最近的问题用于高频分析
     */
    public List<String> getRecentQuestionsForAnalysis(int limit) {
        return questionLogMapper.findRecentQuestions(limit);
    }

    /**
     * 获取用户的问题历史
     */
    public List<QuestionLog> getUserQuestionHistory(Long userId) {
        return questionLogMapper.findByUserId(userId);
    }

    /**
     * 统计问题频率
     */
    public Map<String, Integer> getQuestionFrequency(int limit) {
        List<String> recentQuestions = getRecentQuestionsForAnalysis(limit);
        Map<String, Integer> frequencyMap = new HashMap<>();

        for (String question : recentQuestions) {
            frequencyMap.put(question, frequencyMap.getOrDefault(question, 0) + 1);
        }

        return frequencyMap;
    }
}