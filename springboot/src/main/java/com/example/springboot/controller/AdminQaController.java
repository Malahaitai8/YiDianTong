package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.SystemConfig;
import com.example.springboot.service.SystemConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "管理员-智能问答管理", description = "FAQ 知识库与统计维护")
@RestController
@RequestMapping("/api/admin/qa")
@SecurityRequirement(name = "bearer-jwt")
public class AdminQaController {

    private static final String KEY_FAQS = "qa.faqs";   // JSON 数组: [{question, answer}]
    private static final String KEY_STATS = "qa.stats"; // JSON 对象: { "question": count, ... }

    @Resource
    private SystemConfigService systemConfigService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "获取 FAQ 列表", description = "返回当前 FAQ 列表（JSON数组）")
    @GetMapping("/faqs")
    @PreAuthorize("hasRole('ADMIN')")
    public Result listFaqs() {
        SystemConfig sc = systemConfigService.selectByKey(KEY_FAQS);
        if (sc == null || !StringUtils.hasText(sc.getValue())) {
            return Result.success(Collections.emptyList());
        }
        try {
            List<Map<String, String>> faqs = objectMapper.readValue(
                    sc.getValue(),
                    new TypeReference<List<Map<String, String>>>() {}
            );
            return Result.success(faqs);
        } catch (Exception e) {
            return Result.error("解析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "新增 FAQ", description = "追加一条问题与答案")
    @PostMapping("/faqs")
    @PreAuthorize("hasRole('ADMIN')")
    public Result addFaq(@RequestBody Map<String, String> faq) {
        List<Map<String, String>> faqs = readFaqs();
        if (faqs == null) {
            faqs = new ArrayList<>();
        }
        Map<String, String> entry = new HashMap<>();
        entry.put("question", faq.getOrDefault("question", ""));
        entry.put("answer", faq.getOrDefault("answer", ""));
        faqs.add(entry);
        writeFaqs(faqs);
        return Result.success();
    }

    @Operation(summary = "更新 FAQ", description = "根据索引更新问题或答案")
    @PutMapping("/faqs/{index}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result updateFaq(@PathVariable int index, @RequestBody Map<String, String> faq) {
        List<Map<String, String>> faqs = readFaqs();
        if (faqs == null || index < 0 || index >= faqs.size()) {
            return Result.error("索引无效");
        }
        Map<String, String> exist = faqs.get(index);
        if (faq.containsKey("question")) {
            exist.put("question", faq.get("question"));
        }
        if (faq.containsKey("answer")) {
            exist.put("answer", faq.get("answer"));
        }
        writeFaqs(faqs);
        return Result.success();
    }

    @Operation(summary = "删除 FAQ", description = "根据索引删除一条 FAQ")
    @DeleteMapping("/faqs/{index}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result deleteFaq(@PathVariable int index) {
        List<Map<String, String>> faqs = readFaqs();
        if (faqs == null || index < 0 || index >= faqs.size()) {
            return Result.error("索引无效");
        }
        faqs.remove(index);
        writeFaqs(faqs);
        return Result.success();
    }

    @Operation(summary = "批量导入 FAQ", description = "覆盖式导入 FAQ 列表")
    @PostMapping("/faqs/import")
    @PreAuthorize("hasRole('ADMIN')")
    public Result importFaqs(@RequestBody Map<String, Object> body) {
        Object listObj = body.get("faqs");
        try {
            List<Map<String, String>> faqs = objectMapper.convertValue(
                    listObj, new TypeReference<List<Map<String, String>>>() {}
            );
            if (faqs == null) {
                faqs = Collections.emptyList();
            }
            writeFaqs(faqs);
            return Result.success();
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    @Operation(summary = "导出 FAQ", description = "导出当前 FAQ 列表（JSON）")
    @GetMapping("/faqs/export")
    @PreAuthorize("hasRole('ADMIN')")
    public Result exportFaqs() {
        SystemConfig sc = systemConfigService.selectByKey(KEY_FAQS);
        if (sc == null || !StringUtils.hasText(sc.getValue())) {
            return Result.success(Collections.emptyList());
        }
        try {
            List<Map<String, String>> faqs = objectMapper.readValue(
                    sc.getValue(),
                    new TypeReference<List<Map<String, String>>>() {}
            );
            Map<String, Object> resp = new HashMap<>();
            resp.put("faqs", faqs);
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error("解析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新高频问题统计", description = "覆盖式更新统计对象")
    @PutMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result updateStats(@RequestBody Map<String, Integer> stats) {
        try {
            String value = objectMapper.writeValueAsString(stats);
            systemConfigService.updateValueByKey(KEY_STATS, value);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    private List<Map<String, String>> readFaqs() {
        SystemConfig sc = systemConfigService.selectByKey(KEY_FAQS);
        if (sc == null || !StringUtils.hasText(sc.getValue())) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(
                    sc.getValue(),
                    new TypeReference<List<Map<String, String>>>() {}
            );
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void writeFaqs(List<Map<String, String>> faqs) {
        try {
            String value = objectMapper.writeValueAsString(faqs);
            systemConfigService.updateValueByKey(KEY_FAQS, value);
        } catch (Exception ignored) {
        }
    }
}


