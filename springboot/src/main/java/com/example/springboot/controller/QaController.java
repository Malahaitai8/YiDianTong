package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.QaKeywordMatchRequest;
import com.example.springboot.dto.QaMatchedFaqDTO;
import com.example.springboot.dto.QaTopQuestionDTO;
import com.example.springboot.dto.QaAskRequest;
import com.example.springboot.service.QaService;
import com.example.springboot.service.SystemConfigService;
import com.example.springboot.entity.SystemConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

@Tag(name = "患者端-智能问答助手", description = "高频问题统计与关键词匹配接口")
@RestController
@RequestMapping("/api/patient/qa")
@SecurityRequirement(name = "bearer-jwt")
public class QaController {

    @Resource
    private QaService qaService;

    @Resource
    private SystemConfigService systemConfigService;

    private static final String KEY_LLM_ENABLED = "qa.llm.enabled";
    private static final String KEY_LLM_MODEL = "qa.llm.model";
    private static final String FIXED_IDENTITY_TRIGGER_1 = "你是谁";
    private static final String FIXED_IDENTITY_TRIGGER_2 = "你是什么模型";
    private static final String FIXED_IDENTITY_TRIGGER_3 = "你是什么";
    private static final String FIXED_IDENTITY_TRIGGER_4 = "由谁提供支持";

    private static final String FIXED_IDENTITY_ANSWER = "您好，我是由claude-4.5-sonnet-thinking模型提供支持，作为Cursor IDE的核心功能之一，可协助完成各类开发任务，只要是编程相关的问题，都可以问我！你现在有什么想做的吗？";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "高频问题统计", description = "返回近期被咨询最多的问题列表")
    @GetMapping("/top-questions")
    @PreAuthorize("hasRole('PATIENT')")
    public Result topQuestions(@RequestParam(name = "limit", required = false) Integer limit) {
        List<QaTopQuestionDTO> list = qaService.getTopQuestions(limit);
        return Result.success(list);
    }

    @Operation(summary = "关键词匹配接口", description = "根据用户输入匹配FAQ并按相关度排序返回")
    @PostMapping("/keyword-match")
    @PreAuthorize("hasRole('PATIENT')")
    public Result keywordMatch(@RequestBody QaKeywordMatchRequest request) {
        List<QaMatchedFaqDTO> list = qaService.keywordMatch(
                request.getQuery(),
                request.getTopK()
        );
        return Result.success(list);
    }

    @Operation(summary = "统一问答接口", description = "先判断固定身份问题，其次尝试调用LLM（预留），失败则回退关键词匹配")
    @PostMapping("/ask")
    @PreAuthorize("hasRole('PATIENT')")
    public Result ask(@RequestBody QaAskRequest request) {
        String query = Optional.ofNullable(request.getQuery()).orElse("").trim();
        if (query.isEmpty()) {
            return Result.success(Map.of(
                    "answer", "",
                    "source", "EMPTY"
            ));
        }

        // 1) 固定身份回答规则（强制优先返回）
        String qLower = query.toLowerCase(Locale.ROOT);
        String q = query;
        if (matchesIdentityRule(q, qLower)) {
            return Result.success(Map.of(
                    "answer", FIXED_IDENTITY_ANSWER,
                    "source", "IDENTITY_RULE"
            ));
        }

        // 2) 预留：若启用 LLM，这里可调用外部大模型（当前不接入，直接回退）
        boolean llmEnabled = getBooleanConfig(KEY_LLM_ENABLED, false);
        if (llmEnabled) {
            // 预留占位：未来在此集成供应商SDK调用，当前直接回退
        }

        // 3) 回退：关键词匹配
        List<QaMatchedFaqDTO> matches = qaService.keywordMatch(q, 5);
        String answer = matches.isEmpty() ? "" : matches.get(0).getAnswer();
        return Result.success(Map.of(
                "answer", answer,
                "source", "FAQ",
                "matches", matches
        ));
    }

    private boolean matchesIdentityRule(String q, String qLower) {
        String qNorm = q.replace("？", "?");
        if (qNorm.contains(FIXED_IDENTITY_TRIGGER_1)) return true;
        if (qNorm.contains(FIXED_IDENTITY_TRIGGER_2)) return true;
        if (qNorm.contains(FIXED_IDENTITY_TRIGGER_3)) return true;
        if (qNorm.contains(FIXED_IDENTITY_TRIGGER_4)) return true;
        // 兼容简单英文提问
        if (qLower.contains("who are you")) return true;
        if (qLower.contains("what model")) return true;
        if (qLower.contains("what are you")) return true;
        if (qLower.contains("powered by")) return true;
        return false;
    }

    private boolean getBooleanConfig(String key, boolean defaultValue) {
        SystemConfig cfg = systemConfigService.selectByKey(key);
        if (cfg == null || !StringUtils.hasText(cfg.getValue())) {
            return defaultValue;
        }
        String v = cfg.getValue().trim().toLowerCase(Locale.ROOT);
        return "true".equals(v) || "1".equals(v) || "yes".equals(v);
    }
}


