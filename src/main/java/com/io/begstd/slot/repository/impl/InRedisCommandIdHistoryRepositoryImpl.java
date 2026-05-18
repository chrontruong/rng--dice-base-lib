package com.io.begstd.slot.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.model.app.CommandIdHistory;
import com.io.begstd.slot.repository.RedisCommandIdHistoryRepository;
import com.io.begstd.slot.utils.JsonParseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
public class InRedisCommandIdHistoryRepositoryImpl implements RedisCommandIdHistoryRepository {

    private final static String COMMANDID = "COMMANDID_";

    private RedisTemplate<String, String> redisTemplate;

    private ValueOperations valueOperations;

    public InRedisCommandIdHistoryRepositoryImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
    }

    @Override
    public void save(CommandIdHistory commandIdObj) {
//        log.debug("Start save data {}", commandIdObj);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        try {
            this.valueOperations.set(commandIdObj.getCommandId() + COMMANDID + commandIdObj.getServiceId(), JsonParseUtils.parseToJson(commandIdObj));
            this.redisTemplate.expire(commandIdObj.getCommandId()+ COMMANDID + commandIdObj.getServiceId(), 1, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage() , e);
        }
        
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandIdObj.getCommandId())
            .actorId(commandIdObj.getUserId())
            .serviceId(commandIdObj.getServiceId())
            .psId("")
            .stateName("InRedisCommandIdHistoryRepositoryImpl.save")
            .stepName("Save Redis").owner(LogMessage.OWNER_GAME)
            .message("Save command history " + commandIdObj)
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
        
//        log.debug("ENd save data!");
    }
    
    @Override
    public CommandIdHistory get(String commandId, String serviceId) {

        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId("")
            .serviceId(serviceId)
            .psId("")
            .stateName("InRedisCommandIdHistoryRepositoryImpl.get");
            
        CommandIdHistory cmdIdHis = null;
        String cmdId = (String) valueOperations.get(commandId + COMMANDID + serviceId);
        if (cmdId != null) {
            try {
                cmdIdHis = JsonParseUtils.deserializeFromJson(CommandIdHistory.class, cmdId);

            } catch (Exception e) {
                logBuilder.stepName("ParseJSon").owner(LogMessage.OWNER_GAME)
                    .message("Exception - ")
                    .timeExe(0);
                LogsUtils.writeLogException(logBuilder.build(), e);
            }
        }

        logBuilder
            .stepName("End").owner(LogMessage.OWNER_GAME)
            .message("Get command history "+cmdIdHis)
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
        return cmdIdHis;

    }

    @Override
    public boolean isExistedCommandId(String commandId, String serviceId) {
        return get(commandId, serviceId) != null;
    }

    @Override
    public void saveIfAbsent(CommandIdHistory commandId) {
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId.getCommandId())
            .actorId(commandId.getUserId())
            .serviceId(commandId.getServiceId())
            .psId("")
            .stateName("InRedisCommandIdHistoryRepositoryImpl.saveIfAbsent");
//        log.debug("Start saveIfAbsent {}", commandId);
        try {
            valueOperations.setIfAbsent(commandId.getCommandId()+ COMMANDID + commandId.getServiceId(), JsonParseUtils.parseToJson(commandId), 1, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            logBuilder.stepName("ParseJSon").owner(LogMessage.OWNER_GAME)
                .message("Exception - ")
                .timeExe(0);
            LogsUtils.writeLogException(logBuilder.build(), e);
        }
        
        logBuilder.stepName("End").owner(LogMessage.OWNER_GAME)
            .message("Save command history " + commandId)
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
    }
}
