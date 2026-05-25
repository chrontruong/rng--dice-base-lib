package com.io.begstd.dice.repository.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.repository.IRedisDiceConfigRepository;
import com.io.begstd.dice.utils.JsonParseUtils;
import com.io.begstd.dice.config.model.RedisNotification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;

import java.time.Instant;

import static com.io.begstd.dice.config.model.RedisKeyListener.CHANNEL_GAME_CONFIG;
import static com.io.begstd.dice.config.model.RedisKeyListener.CHANNEL_RTP_CONFIG;

public class RedisDiceConfigRepositoryImpl implements IRedisDiceConfigRepository {

    private RedisTemplate<String, String> redisTemplate;

    private ValueOperations<String, String> valueOperations;

    public RedisDiceConfigRepositoryImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
    }

    @Override
    public void saveMain(DiceConfigMode mode, String json, String clazz, String serviceId) {
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        try {
            valueOperations.set(CHANNEL_GAME_CONFIG + "_" + serviceId, JsonParseUtils.serializeToJson(RedisNotification.builder()
                    .serviceId(serviceId)
                    .clazz(clazz)
                    .mode(mode.name())
                    .json(json)
                    .build()));

            LogMessage.LogMessageBuilder logBuilder = LogMessage.builder()
                    .serviceId(serviceId)
                    .stateName("RedisDiceConfigRepositoryImpl")
                    .stepName("saveMain")
                    .owner("SystemMonitor")
                    .message("Save main config to Redis - " + String.format("mode: %s, clazz: %s, json: %s",
                            mode.name(), clazz, json))
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        } catch (Exception e) {
            LogMessage.LogMessageBuilder logBuilder = LogMessage.builder()
                    .serviceId(serviceId)
                    .stateName("RedisDiceConfigRepositoryImpl")
                    .stepName("saveMain")
                    .owner(LogMessage.OWNER_GAME)
                    .message("Error ave main config to Redis - " + String.format("mode: %s, clazz: %s, json: %s",
                            mode.name(), clazz, json))
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogException(logBuilder.build(), e);
        }
    }



    @Override
    public boolean isExistedChannel(String channel) {
        return redisTemplate.hasKey(channel);
    }

    @Override
    public String get(String channel, String serviceId) {
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        String data = null;
        try {
            data = valueOperations.get(channel);

            LogMessage.LogMessageBuilder logBuilder = LogMessage.builder()
                    .serviceId(serviceId)
                    .stateName("RedisDiceConfigRepositoryImpl")
                    .stepName("get")
                    .owner(LogMessage.OWNER_GAME)
                    .message("Get config data in Redis from channel " + channel + ", data: " + data)
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogDebug(logBuilder.build());
        } catch (Exception e) {
            LogMessage.LogMessageBuilder logBuilder = LogMessage.builder()
                    .serviceId(serviceId)
                    .stateName("RedisDiceConfigRepositoryImpl")
                    .stepName("get")
                    .owner(LogMessage.OWNER_GAME)
                    .message("Error get config data in Redis from channel " + channel)
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogException(logBuilder.build(), e);
        }
        return data;
    }

    @Override
    public void remove(String channel, String serviceId) {
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder()
                .serviceId(serviceId)
                .stateName("RedisDiceConfigRepositoryImpl");

        redisTemplate.delete(channel + "_" + serviceId);

        logBuilder
                .stepName("remove")
                .owner("SystemMonitor")
                .message("Remove config data in Redis by channel " + channel)
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());
    }

    @Override
    public void saveRtp(String serviceId, String rtp) {
        if (!StringUtils.isEmpty(rtp)) {
            long lStartTimeGolbal = Instant.now().toEpochMilli();
            try {
                valueOperations.set(CHANNEL_RTP_CONFIG + "_" + serviceId, rtp);

                LogMessage.LogMessageBuilder logBuilder = LogMessage.builder()
                        .serviceId(serviceId)
                        .stateName("RedisDiceConfigRepositoryImpl")
                        .stepName("saveMain")
                        .owner("SystemMonitor")
                        .message("Save rtp config to Redis - " + rtp)
                        .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
                LogsUtils.writeLogInfo(logBuilder.build());
            } catch (Exception e) {
                LogMessage.LogMessageBuilder logBuilder = LogMessage.builder()
                        .serviceId(serviceId)
                        .stateName("RedisDiceConfigRepositoryImpl")
                        .stepName("saveMain")
                        .owner(LogMessage.OWNER_GAME)
                        .message("Error save rtp config to Redis - " + rtp)
                        .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
                LogsUtils.writeLogException(logBuilder.build(), e);
            }
        }
    }
}

