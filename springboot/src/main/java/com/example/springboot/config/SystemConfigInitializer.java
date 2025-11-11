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
        seedIfMissing("FEE_NORMAL", "0.00", "Default fee for normal slot");
        seedIfMissing("FEE_EXPERT", "0.00", "Default fee for expert slot");
        seedIfMissing("FEE_VIP", "0.00", "Default fee for VIP slot");
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


