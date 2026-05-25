package com.io.begstd.dice.services.internal.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.io.begstd.dice.factory.GameRuleFactory;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.repository.RedisConfigRTPRepository;
import com.io.begstd.dice.services.external.PromotionService;
import com.io.begstd.dice.services.internal.StartTestSchedulerService;
import com.io.begstd.dice.utils.ConfigManager;
import com.io.begstd.dice.utils.GameUtils;
import com.io.begstd.dice.config.StartTestProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class StartTestSchedulerServiceImpl implements StartTestSchedulerService {

    @Value(value = "${service.intervalCheckStartTestSeconds:600}")
    private int intervalCheckStartTestSeconds;

    @Autowired
    private RedisConfigRTPRepository redisConfigRTPRepositoryImpl;

    @Autowired
    private StartTestProperty startTestProperty;

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private Environment environment;

    @Autowired
    @Qualifier("objectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private GameRuleFactory gameRuleFactory;

    private String serviceId;

    @Autowired
    private PromotionService promotionService;

    @PostConstruct
    public void init() {
        IDiceMachineConfig configNormal = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
        this.serviceId = configNormal.serviceId();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(this::checkConfigRepo, 0, intervalCheckStartTestSeconds, SECONDS);
    }

    private void checkConfigRepo() {
        try {

            String rtp = redisConfigRTPRepositoryImpl.getRtp(serviceId);
            log.info("Current level {}", translateRTPLevel(rtp));
            if (!StringUtils.isEmpty(rtp) && !rtp.equals(startTestProperty.getRtp())) {
                startTestProperty.setRtp(rtp);
                GameUtils.updateRtpConfig(rtp, environment, objectMapper, gameRuleFactory, configManager);
            }
        } catch (Exception e) {
            log.error(String.format("Failed to check redis for startTest, serviceId: %s, interval: %ss, exception: %s",
                    serviceId, intervalCheckStartTestSeconds, e.getMessage()), e);
        }
    }

    private String translateRTPLevel(String rtp) {

        switch (rtp) {
            case "99": return "A";
            case "98": return "B";
            case "97": return "C";
            case "96": return "D";
            case "95": return "E";
            case "94": return "F";
            case "93": return "G";
            default:   return "H";
        }
    }
}
