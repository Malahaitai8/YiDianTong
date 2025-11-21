package com.example.springboot.config;

import com.example.springboot.entity.SystemConfig;
import com.example.springboot.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class SystemConfigInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigInitializer.class);

    @Resource
    private SystemConfigService systemConfigService;

    @Override
    public void run(ApplicationArguments args) {
        seedIfMissing("FEE_NORMAL", "15.00", "普通号挂号费");
        seedIfMissing("FEE_EXPERT", "50.00", "专家号挂号费");
        seedIfMissing("FEE_VIP", "100.00", "特需VIP号挂号费");
        seedIfMissing("STUDENT_REIMBURSEMENT_RATE", "0.95", "学生挂号费报销比例");
        seedIfMissing("TEACHER_REIMBURSEMENT_RATE", "0.90", "教师挂号费报销比例");
        seedIfMissing("CANCELLATION_FREE_HOURS", "24", "就诊前多久取消可免费退号，否则记录爽约（单位：小时）");
    }

    private void seedIfMissing(String key, String value, String desc) {
        SystemConfig existing = systemConfigService.selectByKey(key);
        if (existing == null) {
            SystemConfig sc = new SystemConfig();
            sc.setKey(key);
            sc.setValue(value);
            sc.setDescription(desc);
            systemConfigService.insert(sc);
            log.info("Seeded system_config key={} value={}", key, value);
        }
    }
}


