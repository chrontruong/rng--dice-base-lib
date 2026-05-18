package com.io.begstd.slot.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.model.gamerule.RTPSlotConfig;
import com.io.begstd.slot.repository.RedisConfigRTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.List;

public class RedisConfigRTPRepositoryImpl implements RedisConfigRTPRepository {

    private final static String SLOT_RTP = "RtpConfig_";
    private final static String SLOT_RTP_CONFIG = "RTPSlotConfigs";

    private RedisTemplate<String, String> redisTemplateRTP;

    private HashOperations hashOperations;

    private ValueOperations valueOperations;

    @Autowired
    private ApplicationContext appContext;

    public RedisConfigRTPRepositoryImpl(RedisTemplate<String, String> redisTemplateRTP) {
        this.redisTemplateRTP = redisTemplateRTP;
        this.hashOperations = redisTemplateRTP.opsForHash();
        this.valueOperations = redisTemplateRTP.opsForValue();
    }

    @Override
    public synchronized void save(String serviceId, List<RTPSlotConfig> rtpconfig) {
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId("saveRTP")
            .actorId("all")
            .serviceId(serviceId)
            .psId("")
            .stateName("RedisConfigRTPRepositoryImpl.save")
            .owner("SystemMonitor");
        
        try {
            if (rtpconfig != null) {
                ObjectMapper objectMapper = appContext.getBean("objectMapper", ObjectMapper.class);
                this.hashOperations.put(SLOT_RTP_CONFIG, serviceId, objectMapper.writeValueAsString(rtpconfig));
            }
        } catch (Exception e) {
            logBuilder.stepName("ParseJSon")
                .message("Exception - ")
                .timeExe(0);
            LogsUtils.writeLogException(logBuilder.build(), e);
        }
        
        logBuilder.stepName("End")
            .message("Save rtp config for game "+serviceId)
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());
    }

    @Override
    public synchronized String getRtp(String serviceId) {
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        String rtp = (String) this.valueOperations.get(SLOT_RTP+serviceId);
        logBuilder
            .actorId("all")
            .serviceId(serviceId)
            .stateName("RedisConfigRTPRepositoryImpl.getRtp")
            .message("value: " + rtp)
            .owner(LogMessage.OWNER_GAME)
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
        return rtp == null ? "":rtp;
    }

    @Override
    public synchronized void saveRtp(String serviceId, String rtpValue) {

        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();

        this.valueOperations.set(SLOT_RTP + serviceId, rtpValue);

        logBuilder
            .actorId("all")
            .serviceId(serviceId)
            .stateName("RedisConfigRTPRepositoryImpl.saveRtp")
            .message("value: " + rtpValue)
            .owner("SystemMonitor")
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());

    }
    @Override
    public synchronized void removeRtp(String serviceId) {
        long lStartTimeGolbal = Instant.now().toEpochMilli();

        this.redisTemplateRTP.delete(SLOT_RTP+serviceId);

        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId("")
            .actorId("all")
            .serviceId(serviceId)
            .psId("")
            .stateName("RedisConfigRTPRepositoryImpl.removeRtp")
            .stepName("Remove").owner("SystemMonitor")
            .message("Remove rtp")
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());
    }

    @Override
    public boolean isExistedRtp(String serviceId) {
        return !"".equals(getRtp(serviceId));
    }
}
