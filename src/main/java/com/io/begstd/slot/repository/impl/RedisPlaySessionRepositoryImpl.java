package com.io.begstd.slot.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.common.SlotGameConstant;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.IPlaySessionClass;
import com.io.begstd.slot.repository.RedisPlaySessionRepository;
import com.io.begstd.slot.utils.JsonParseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
public class RedisPlaySessionRepositoryImpl implements RedisPlaySessionRepository {

    private final static String PLAY_SESSION = "PLAY_SESSION_";

    private final static String PLAY_SESSION_GAMBLE = "PLAY_SESSION_GAMBLE";

    private RedisTemplate<String, String> redisTemplate;

    private HashOperations hashOperations;

    private ValueOperations valueOperations;

    @Autowired
    private IPlaySessionClass playSessionClass;

    @Value(value = "${gamble.expiredTime:0}")
    private long expiredTime;

    public RedisPlaySessionRepositoryImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
        this.valueOperations = redisTemplate.opsForValue();
    }

    @Override
    public synchronized void save(BasePlaySession player) {
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
                .cmdId(player.commandId())
                .actorId(player.userId())
                .serviceId(player.serviceId())
                .psId("")
                .stateName("RedisPlaySessionRepositoryImpl.save")
                .owner(LogMessage.OWNER_GAME);

        try {
//            String currency = player.currency() != null ? player.currency().name() : SlotGameConstant.CURRENCY_DEFAULT;
//            String keyPSRedis = PLAY_SESSION + player.serviceId();
//            if (!SlotGameConstant.CURRENCY_DEFAULT.equalsIgnoreCase(currency)) {
//                keyPSRedis += "_" + currency;
//            }
            String keyPSRedis = PLAY_SESSION + player.serviceId();
            if (!StringUtils.isEmpty(player.currency()) && !SlotGameConstant.CURRENCY_DEFAULT.equalsIgnoreCase(player.currency())) {
                keyPSRedis += "_" + player.currency();
            }

            this.hashOperations.put(keyPSRedis, player.userId(),JsonParseUtils.serializeToJson(player));
        } catch (Exception e) {

            logBuilder.stepName("ParseJSon")
                    .message("Exception - ")
                    .timeExe(0);
            LogsUtils.writeLogException(logBuilder.build(), e);
        }
        logBuilder
                .rootCmdId(player.commandId())
                .stepName("End")
                .message("Remove playsession")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
    }
    /*
        @Override
        public synchronized BasePlaySession get(String serviceId, String userID) {
            long lStartTimeGolbal = Instant.now().toEpochMilli();
            LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
            logBuilder
                .actorId(userID)
                .serviceId(serviceId)
                .stateName("RedisPlaySessionRepositoryImpl.get")
                .owner(LogMessage.OWNER_GAME);
            BasePlaySession ps = null;
            String psJSon = (String) hashOperations.get(PLAY_SESSION + serviceId, userID);
            if (psJSon != null) {
                try {
                    ps = JsonParseUtils.deserializeFromJson(playSessionClass.getClazz(), psJSon);
                    if (!userID.equals(ps.userId())) {
    //                    log.error("ERROR USER - "+userID+" ps user "+ ps.userId());
                        logBuilder
                            .cmdId(ps.commandId())
                            .psId(ps.uuid())
                            .stepName("Mismatch userId")
                            .message("ERROR USER - "+userID+" ps user "+ ps.userId())
                            .timeExe(0);
                        LogsUtils.writeLogError(logBuilder.build());
                    }
                    logBuilder
                        .cmdId(ps.commandId())
                        .psId(ps.uuid())
                        .stepName("Get End")
                        .message("User of PS " + ps.userId())
                        .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
                    LogsUtils.writeLogDebug(logBuilder.build());

                } catch (Exception e) {
                    logBuilder
                        .cmdId("")
                        .psId("pls-" + psJSon)
                        .stepName("ParseJSException")
                        .message("Exception - ")
                        .timeExe(0);
                    LogsUtils.writeLogException(logBuilder.build(), e);
                }
             }

            return ps;
        }
    */
    @Override
    public synchronized BasePlaySession get(String serviceId, String userID, String currency) {
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
                .actorId(userID)
                .serviceId(serviceId)
                .stateName("RedisPlaySessionRepositoryImpl.get")
                .owner(LogMessage.OWNER_GAME);
        BasePlaySession ps = null;
        //migrate ps from old to new have currency
        String keyPSRedis = PLAY_SESSION + serviceId;
        if (!StringUtils.isEmpty(currency) && !SlotGameConstant.CURRENCY_DEFAULT.equalsIgnoreCase(currency)) {
            keyPSRedis += "_" + currency;
        }

        String psJSon = (String) hashOperations.get(keyPSRedis, userID);

        if (psJSon != null) {
            try {
                ps = JsonParseUtils.deserializeFromJson(playSessionClass.getClazz(), psJSon);
                if (!userID.equals(ps.userId())) {
//                    log.error("ERROR USER - "+userID+" ps user "+ ps.userId());
                    logBuilder
                            .cmdId(ps.commandId())
                            .psId(ps.uuid())
                            .stepName("Mismatch userId")
                            .message("ERROR USER - "+userID+" ps user "+ ps.userId())
                            .timeExe(0);
                    LogsUtils.writeLogError(logBuilder.build());
                }
                logBuilder
                        .cmdId(ps.commandId())
                        .psId(ps.uuid())
                        .stepName("Get End")
                        .message("User of PS " + ps.userId())
                        .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
                LogsUtils.writeLogDebug(logBuilder.build());

            } catch (Exception e) {
                logBuilder
                        .cmdId("")
                        .psId("pls-" + psJSon)
                        .stepName("ParseJSException")
                        .message("Exception - ")
                        .timeExe(0);
                LogsUtils.writeLogException(logBuilder.build(), e);
            }
        }

        return ps;
    }

    @Override
    public synchronized BasePlaySession getGamble(String userID, String serviceId, String currency) {
//        log.debug("getGamble Get data {}", userID);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
                .actorId(userID)
                .serviceId(serviceId)
                .stateName("RedisPlaySessionRepositoryImpl.get")
                .owner(LogMessage.OWNER_GAME);

        BasePlaySession ps = null;

        String key = PLAY_SESSION_GAMBLE + userID + serviceId;
        if (!StringUtils.isEmpty(currency) && !SlotGameConstant.CURRENCY_DEFAULT.equalsIgnoreCase(currency)) {
            key += "_" + currency;
        }

        String psJSon = (String) valueOperations.get(key);
//        log.debug(" JSON from redis: {} ", psJSon);
        if (psJSon != null) {
            try {
                ps = JsonParseUtils.deserializeFromJson(playSessionClass.getClazz(), psJSon);
                logBuilder
                        .cmdId(ps.commandId())
                        .rootCmdId(ps.commandId())
                        .psId(ps.uuid())
                        .stepName("Get End")
                        .message("PS of gamble " + ps.userId())
                        .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
                LogsUtils.writeLogDebug(logBuilder.build());
            } catch (Exception e) {
//                log.error("get - ", e);
                logBuilder
                        .cmdId("")
                        .psId("pls-" + psJSon)
                        .stepName("ParseJSException")
                        .message("Exception - ")
                        .timeExe(0);
                LogsUtils.writeLogException(logBuilder.build(), e);
            }
        }
//        log.debug("Retrive data {}", ps);
        return ps;
    }

    @Override
    public synchronized void removePlaySession(BasePlaySession basePlaySession) {
//        log.debug("Start removePlaySession {}", playSession.userId());
        long lStartTimeGolbal = Instant.now().toEpochMilli();

        String currency = basePlaySession.currency();

        String keyPSRedis = PLAY_SESSION + basePlaySession.serviceId();
        if (!StringUtils.isEmpty(currency) && !SlotGameConstant.CURRENCY_DEFAULT.equalsIgnoreCase(currency)) {
            keyPSRedis += "_" + currency;
        }

        hashOperations.delete(keyPSRedis, basePlaySession.userId());
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
                .cmdId(basePlaySession.commandId())
                .rootCmdId(basePlaySession.commandId())
                .actorId(basePlaySession.userId())
                .serviceId(basePlaySession.serviceId())
                .psId(basePlaySession.uuid())
                .stateName("RedisPlaySessionRepositoryImpl.removePlaySession")
                .stepName("Remove").owner(LogMessage.OWNER_GAME)
                .message("Remove playsession")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
    }
    /*
        @Override
        public boolean isExistedUserId(String userId, String serviceId) {
            return get(userId, serviceId) != null;
        }
    */
    @Override
    public void saveIfAbsent(BasePlaySession player) {
//        log.debug("Start saveIfAbsent {}", player);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        try {

            String currency = player.currency();

            String keyPSRedis = PLAY_SESSION + player.serviceId();
            if (!StringUtils.isEmpty(currency) && !SlotGameConstant.CURRENCY_DEFAULT.equalsIgnoreCase(currency)) {
                keyPSRedis += "_" + currency;
            }


            hashOperations.putIfAbsent(keyPSRedis, player.userId(), JsonParseUtils.serializeToJson(player));
        } catch (Exception e) {
            LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
            logBuilder
                    .cmdId(player.commandId())
                    .actorId(player.userId())
                    .serviceId(player.serviceId())
                    .psId("")
                    .stateName("RedisPlaySessionRepositoryImpl.saveIfAbsent")
                    .stepName("saveIfAbsent").owner(LogMessage.OWNER_GAME)
                    .message("Exception - ")
                    .timeExe(0);
            LogsUtils.writeLogException(logBuilder.build(), e);
        }

        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
                .cmdId(player.commandId())
                .rootCmdId(player.commandId())
                .actorId(player.userId())
                .serviceId(player.serviceId())
                .psId(player.uuid())
                .stateName("RedisPlaySessionRepositoryImpl.saveIfAbsent")
                .stepName("saveIfAbsent").owner(LogMessage.OWNER_GAME)
                .message("saveIfAbsent playsession")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
    }

    @Override
    public synchronized void saveGamble(BasePlaySession basePlaySession) {
//        log.debug("Start save data {}", playSession);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
                .cmdId(basePlaySession.commandId())
                .actorId(basePlaySession.userId())
                .serviceId(basePlaySession.serviceId())
                .psId("")
                .stateName("RedisPlaySessionRepositoryImpl.saveGamble")
                .owner(LogMessage.OWNER_GAME);

        try {
            String currency = basePlaySession.currency();

            String key = PLAY_SESSION_GAMBLE + basePlaySession.userId() + basePlaySession.serviceId();
            if (!StringUtils.isEmpty(currency) && !SlotGameConstant.CURRENCY_DEFAULT.equalsIgnoreCase(currency)) {
                key += "_" + currency;
            }

            String json = JsonParseUtils.serializeToJson(basePlaySession);
//            log.debug(" JSON to redis: {} ", json);
            logBuilder.stepName("End")
                    .message(basePlaySession)
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogDebug(logBuilder.build());
            this.valueOperations.set(key, json);
            this.redisTemplate.expire(key, expiredTime, TimeUnit.MILLISECONDS);
        } catch (JsonProcessingException e) {
            logBuilder
                    .cmdId("")
                    .psId("")
                    .stepName("ParseJSException")
                    .message("Exception - ")
                    .timeExe(0);
            LogsUtils.writeLogException(logBuilder.build(), e);
        }
//        log.debug("ENd save data!");

        logBuilder.rootCmdId(basePlaySession.commandId()).timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
    }

    @Override
    public synchronized void removeGamblePlaySession(BasePlaySession basePlaySession) {
        log.debug("remove Gamble session {}", basePlaySession.userId());
        String currency = basePlaySession.currency();

        String key = PLAY_SESSION_GAMBLE + basePlaySession.userId() + basePlaySession.serviceId();
        if (!StringUtils.isEmpty(currency) && !SlotGameConstant.CURRENCY_DEFAULT.equalsIgnoreCase(currency)) {
            key += "_" + currency;
        }

        redisTemplate.delete(key);
    }

//    @Override
//    public synchronized List<ExpiredPlaySession> getPlaySessionList(String serviceId) {
//        long lStartTimeGolbal = Instant.now().toEpochMilli();
//        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
//        logBuilder
//            .actorId("ScannerService")
//            .serviceId(serviceId)
//            .stateName("RedisPlaySessionRepositoryImpl.get")
//            .owner(LogMessage.OWNER_GAME);
//        Map<String, String> entries = hashOperations.entries(PLAY_SESSION + serviceId);
//        List<ExpiredPlaySession> retList = null;
//        if (!entries.isEmpty()) {
//            retList = new ArrayList<ExpiredPlaySession>();
//            for(@SuppressWarnings("rawtypes") Map.Entry m:entries.entrySet()){ 
//                try {
//                    ExpiredPlaySession ps = JsonParseUtils.deserializeFromJson(ExpiredPlaySession.class, (String)m.getValue());
//                    retList.add(ps);
//                    logBuilder
//                        .cmdId("")
//                        .actorId(ps.userId())
//                        .psId(ps.uuid())
//                        .stepName("Get End")
//                        .message("User of PS " + ps.userId())
//                        .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
//                    LogsUtils.writeLogDebug(logBuilder.build());
//
//                } catch (Exception e) {
//                    logBuilder
//                        .cmdId("")
//                        .psId("pls-")
//                        .stepName("ParseJSException")
//                        .message("Exception - ")
//                        .timeExe(0);
//                    LogsUtils.writeLogException(logBuilder.build(), e);
//                }
//            }
//        }
//
//        return retList;
//    }

    @Override
    public int deleteTrialPs(String serviceId, long timeDeleted) {
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
                .actorId("ScannerService")
                .serviceId(serviceId)
                .stateName("RedisPlaySessionRepositoryImpl.deleteTrialPs")
                .owner(LogMessage.OWNER_GAME);
        int countPSDeleted = 0;
        logBuilder
                .message("Total deleted pstrial: " + countPSDeleted)
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());
        return countPSDeleted;
    }
}
