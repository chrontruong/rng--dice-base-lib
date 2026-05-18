package com.io.begstd.slot.repository.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.model.app.Promotion;
import com.io.begstd.slot.repository.PromotionRepositoryService;
import com.io.begstd.slot.utils.JsonParseUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Instant;

public class PromotionRepositoryServiceImpl implements PromotionRepositoryService {
    private final static String PROMOTION_GAME = "PROMOTION_";

    private RedisTemplate<String, Promotion> redisPromotionGameTemplate;

    public PromotionRepositoryServiceImpl(RedisTemplate<String, Promotion> redisPromotionGameTemplate) {
        this.redisPromotionGameTemplate = redisPromotionGameTemplate;
    }
    
    @Override
    public void save(String serviceId, Promotion promotion) {
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId("")
            .actorId(promotion.getUserId())
            .serviceId(serviceId)
            .psId("")
            .stateName("PromotionRepositoryServiceImpl.save")
            .stepName("Save").owner(LogMessage.OWNER_GAME);
            
//        this.redisPromotionGameTemplate.opsForHash().put(PROMOTION_GAME + serviceId, promotion.getUserId(), promotion);
        
        try {
            this.redisPromotionGameTemplate.opsForHash().put(PROMOTION_GAME + serviceId + "_" + promotion.getCurrency(), promotion.getUserId(),
                JsonParseUtils.serializeToJson(promotion));
        } catch (Exception e) {
            
            logBuilder.stepName("ParseJSon")
                .message("Exception - ")
                .timeExe(0);
            LogsUtils.writeLogException(logBuilder.build(), e);
        }
        logBuilder.message("SAVE promotion - " + promotion)
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
    }

    @Override
    public Promotion get(String serviceId, String userId, String currency) {
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        Promotion promotion = null;
//        promotion = (Promotion)this.redisPromotionGameTemplate.opsForHash().get(PROMOTION_GAME + serviceId, userId);
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId("")
            .actorId(userId)
            .serviceId(serviceId)
            .psId("")
            .stateName("PromotionRepositoryServiceImpl.get")
            .stepName("Get").owner(LogMessage.OWNER_GAME)
            .message("Get promotion - " + promotion)
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
        try {
            String promotionJSon = (String) this.redisPromotionGameTemplate.opsForHash().get(PROMOTION_GAME + serviceId + "_" + currency, userId);
            if (promotionJSon != null) {
                promotion = JsonParseUtils.deserializeFromJson(Promotion.class, promotionJSon);
            }
        } catch (Exception e) {
            logBuilder
                .stepName("Exception")
                .message("Try old version - delete old promotion + ")
                .timeExe(0);
            LogsUtils.writeLogDebug(logBuilder.build());
            promotion = (Promotion)this.redisPromotionGameTemplate.opsForHash().get(PROMOTION_GAME + serviceId+ "_" + currency, userId);
            if (promotion == null) {
                this.remove(serviceId, userId, currency);
            } 
        }
        
        return promotion;
    }

    @Override
    public void remove(String serviceId, String userId, String currency) {
//        log.debug("Start remove data {}", userId);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        this.redisPromotionGameTemplate.opsForHash().delete(PROMOTION_GAME + serviceId+ "_" + currency, userId);
//        log.debug("ENd remove data!");
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId("")
            .actorId(userId)
            .serviceId(serviceId)
            .psId("")
            .stateName("PromotionRepositoryServiceImpl.remove")
            .stepName("Remove Redis").owner(LogMessage.OWNER_GAME)
            .message("Remove promotion")
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
    }
}
