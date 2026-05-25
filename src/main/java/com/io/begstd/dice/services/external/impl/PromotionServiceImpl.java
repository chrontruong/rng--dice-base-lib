package com.io.begstd.dice.services.external.impl;

import com.google.protobuf.Value;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.dice.model.app.Promotion;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.services.external.PromotionService;
import com.io.begstd.dice.utils.BeanUtils;
import com.io.begstd.dice.utils.ConfigManager;
import com.io.begstd.dice.common.DiceGameConstant;
import com.io.begstd.dice.config.ExternalServiceEndPointConfiguration;
import com.io.begstd.dice.grpc.promotionservice.DiceCheckPromotiontRequest;
import com.io.begstd.dice.grpc.promotionservice.DiceCheckServiceHasPromotionRequest;
import com.io.begstd.dice.grpc.promotionservice.DiceCheckServiceHasPromotionResponse;
import com.io.begstd.dice.grpc.promotionservice.DiceCommitPromotionDataRequest;
import com.io.begstd.dice.grpc.promotionservice.DiceMinusPromotionDataRequest;
import com.io.begstd.dice.grpc.promotionservice.DicePromotionData;
import com.io.begstd.dice.grpc.promotionservice.DicePromotionResponse;
import com.io.begstd.dice.grpc.promotionservice.DicePromotionServiceGrpc;
import com.io.begstd.dice.grpc.promotionservice.DiceReleasePromotionDataRequest;
import com.io.begstd.dice.grpc.promotionservice.DiceReservePromotionDataRequest;
import com.io.begstd.dice.grpc.promotionservice.DiceUsePromotionCodeRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
@Slf4j
public class PromotionServiceImpl implements PromotionService {
    @Autowired
    private ExternalServiceEndPointConfiguration externalServiceEndPointConfiguration;

    private DicePromotionServiceGrpc.DicePromotionServiceBlockingStub stub =null;
    private static final ReentrantLock lock = new ReentrantLock();
    private boolean hasPromotion = false;

    DicePromotionServiceGrpc.DicePromotionServiceBlockingStub  initial() {
        if (stub == null) {
            try {
                lock.lock();
                if (stub == null) {
                    ManagedChannel channel = ManagedChannelBuilder.forAddress(externalServiceEndPointConfiguration.getPromotionHostName(),
                            externalServiceEndPointConfiguration.getPromotionHostPort()).usePlaintext().build();
                    stub = DicePromotionServiceGrpc.newBlockingStub(channel);
                }
            } finally {
                lock.unlock();
            }
        }
        return stub;
    }

    @Override
    public Promotion usePromotionCode(String serviceId, String commandId, String userId, String code, String prefixService, String currency) {
      //log
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(commandId)
            .actorId(userId)
            .serviceId(serviceId)
            .psId("")
            .stateName("PromotionServiceImpl")
            .owner(LogMessage.OWNER_PROMOTION)
            .stepName("usePromotionCode");

        Promotion promotion = null;
        try {
            DiceUsePromotionCodeRequest.Builder builder = DiceUsePromotionCodeRequest.newBuilder();
            builder.setUserId(userId);
            builder.setCommandId(commandId);
            builder.setServiceId(prefixService +serviceId);
            builder.setCode(code);
            builder.setCurrency(currency);
            DicePromotionResponse result = initial().withDeadlineAfter(3, TimeUnit.SECONDS).usePromotionCode(builder.build());
            if(result != null) {
//                log.info("Add addPromotionCode status response {}", result);
                logBuilder.message("Passed - " + result.toString()).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogDebug(logBuilder.build());

                promotion = buildPromotionModel(result, userId);
            } else {
                logBuilder.stepName("usePromotionCode-Exception")
                    .message("Dont promotion code")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogError(logBuilder.build());
//                log.error("ERROR --Add addPromotionCode {} - error {}", userId, promotion);
            }
        } catch (StatusRuntimeException e) {
//            log.error("ERROR --Add addPromotionCode {} - error {}", userId, e);
            logBuilder.stepName("usePromotionCode-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);

            shutDown();
        } catch (Exception e) {
//            log.error("ERROR --Add addPromotionCode {} - error {}", userId, e);
            logBuilder.stepName("usePromotionCode-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
        }
        return promotion;
    }

    @Override
    public Promotion checkUserHasPromotion(String serviceId, String commandId, String userId, String prefixService, String currency) {
      //log
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(commandId)
            .actorId(userId)
            .serviceId(serviceId)
            .psId("")
            .stateName("PromotionServiceImpl")
            .owner(LogMessage.OWNER_PROMOTION)
            .stepName("checkUserHasPromotion");

        Promotion promotion = null;
        try {
            DiceCheckPromotiontRequest.Builder builder = DiceCheckPromotiontRequest.newBuilder();
            builder.setUserId(userId);
            builder.setCommandId(commandId);
            builder.setServiceId(prefixService +serviceId);
            builder.setCurrency(currency);
            DicePromotionResponse result = initial().withDeadlineAfter(3, TimeUnit.SECONDS).checkUserHasPromotion(builder.build());
            if(result != null) {
//                log.info("Check checkUserHasPromotion status response {}", result);
                logBuilder.message("Passed - " + result.toString()).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogDebug(logBuilder.build());
                promotion = buildPromotionModel(result, userId);
            } else {
                logBuilder.stepName("checkUserHasPromotion-Exception")
                    .message("Dont have promotion code")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogError(logBuilder.build());
//                log.error("ERROR --Check checkUserHasPromotion {} - error {}", userId, promotion);
            }
        } catch (StatusRuntimeException e) {
//            log.error("ERROR --Check checkUserHasPromotion {} - error {}", userId, e);
            logBuilder.stepName("checkUserHasPromotion-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
        } catch (Exception e) {
//            log.error("ERROR --Check checkUserHasPromotion {} - error {}", userId, e);
            logBuilder.stepName("checkUserHasPromotion-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
        }
        return promotion;
    }

    @Override
    public Promotion minusPromotionData(String serviceId, String psId, String userId, String promotionCode, String prefixService, String currency) {
      //log
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(psId)
            .actorId(userId)
            .serviceId(serviceId)
            .psId("")
            .stateName("PromotionServiceImpl")
            .owner(LogMessage.OWNER_PROMOTION)
            .stepName("minusDicePromotionData");

        Promotion promotion = null;
        try {
            DiceMinusPromotionDataRequest.Builder builder = DiceMinusPromotionDataRequest.newBuilder();
            builder.setUserId(userId);
            builder.setCommandId(psId);
            builder.setServiceId(prefixService +serviceId);
            builder.setPromotionCode(promotionCode);
            builder.setCurrency(currency);
            DicePromotionResponse result = initial().withDeadlineAfter(3, TimeUnit.SECONDS).minusPromotionData(builder.build());
            if(result != null) {
//                log.info("Minus minusDicePromotionData status response {}", result);
                logBuilder.message("Passed - " + result.toString() +". PromotionCode:" + promotionCode).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogDebug(logBuilder.build());

                promotion = buildPromotionModel(result, userId);
            } else {
//                log.error("ERROR --Minus minusDicePromotionData {} - error {}", userId, promotion);
                logBuilder.stepName("minusDicePromotionData-Exception")
                    .message("Failed minus "+ promotionCode)
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogError(logBuilder.build());

            }
        } catch (StatusRuntimeException e) {
//            log.error("ERROR --Add minusDicePromotionData {} - error {}", userId, e);
            logBuilder.stepName("checkUserHasPromotion-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
        } catch (Exception e) {
//            log.error("ERROR --Add minusDicePromotionData {} - error {}", userId, e);
            logBuilder.stepName("checkUserHasPromotion-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
        }
        return promotion;
    }

    @Override
    public Promotion reservePromotionData(String serviceId, String psId, String userId, String promotionCode, String prefixService, String currency) {
      //log
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(psId)
            .actorId(userId)
            .serviceId(serviceId)
            .psId("")
            .stateName("PromotionServiceImpl")
            .owner(LogMessage.OWNER_PROMOTION)
            .stepName("reserveDicePromotionData");

        Promotion promotion = null;
        try {
            DiceReservePromotionDataRequest.Builder builder = DiceReservePromotionDataRequest.newBuilder();
            builder.setUserId(userId);
            builder.setCommandId(psId);
            builder.setServiceId(prefixService +serviceId);
            builder.setPromotionCode(promotionCode);
            builder.setNumberSpin(1);
            builder.setCurrency(currency);
            DicePromotionResponse result = initial().withDeadlineAfter(3, TimeUnit.SECONDS).reservePromotionData(builder.build());
            if(result != null) {
                logBuilder.message("Passed - " + result.toString() +". PromotionCode:" + promotionCode).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogDebug(logBuilder.build());

                promotion = buildPromotionModel(result, userId);
            } else {
                logBuilder.stepName("reserveDicePromotionData-Exception")
                    .message("Failed minus "+ promotionCode)
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogError(logBuilder.build());

            }
        } catch (StatusRuntimeException e) {
            logBuilder.stepName("reserveDicePromotionData-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
        } catch (Exception e) {
            logBuilder.stepName("reserveDicePromotionData-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
        }
        return promotion;
    }

    @Override
    public Promotion commitPromotionData(String serviceId, String psId, String userId, String promotionCode, String prefixService, String currency) {
      //log
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(psId)
            .actorId(userId)
            .serviceId(serviceId)
            .psId("")
            .stateName("PromotionServiceImpl")
            .owner(LogMessage.OWNER_PROMOTION)
            .stepName("commitDicePromotionData");

        Promotion promotion = null;
        try {
            DiceCommitPromotionDataRequest.Builder builder = DiceCommitPromotionDataRequest.newBuilder();
            builder.setUserId(userId);
            builder.setCommandId(psId);
            builder.setServiceId(prefixService +serviceId);
            builder.setPromotionCode(promotionCode);
            builder.setNumberSpin(1);
            builder.setCurrency(currency);
            DicePromotionResponse result = initial().withDeadlineAfter(3, TimeUnit.SECONDS).commitPromotionData(builder.build());
            if(result != null) {
                logBuilder.message("Passed - " + result.toString() +". PromotionCode:" + promotionCode).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogDebug(logBuilder.build());

                promotion = buildPromotionModel(result, userId);
            } else {
                logBuilder.stepName("commitDicePromotionData-Exception")
                    .message("Failed commitPromotion "+ promotionCode)
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogError(logBuilder.build());

            }
        } catch (StatusRuntimeException e) {
            logBuilder.stepName("commitDicePromotionData-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
        } catch (Exception e) {
            logBuilder.stepName("commitDicePromotionData-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
        }
        return promotion;
    }

    @Override
    public Promotion releasePromotionData(String serviceId, String psId, String userId, String promotionCode, String prefixService, String currency) {
      //log
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(psId)
            .actorId(userId)
            .serviceId(serviceId)
            .psId("")
            .stateName("PromotionServiceImpl")
            .owner(LogMessage.OWNER_PROMOTION)
            .stepName("releaseDicePromotionData");

        Promotion promotion = null;
        try {
            DiceReleasePromotionDataRequest.Builder builder = DiceReleasePromotionDataRequest.newBuilder();
            builder.setUserId(userId);
            builder.setCommandId(psId);
            builder.setServiceId(prefixService +serviceId);
            builder.setPromotionCode(promotionCode);
            builder.setNumberSpin(1);
            builder.setCurrency(currency);
            DicePromotionResponse result = initial().withDeadlineAfter(3, TimeUnit.SECONDS).releasePromotionData(builder.build());
            if(result != null) {
                logBuilder.message("Passed - " + result.toString() +". PromotionCode:" + promotionCode).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogDebug(logBuilder.build());

                promotion = buildPromotionModel(result, userId);
            } else {
                logBuilder.stepName("releaseDicePromotionData-Exception")
                    .message("Failed minus "+ promotionCode)
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogError(logBuilder.build());

            }
        } catch (StatusRuntimeException e) {
            logBuilder.stepName("releaseDicePromotionData-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
        } catch (Exception e) {
            logBuilder.stepName("releaseDicePromotionData-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
        }
        return promotion;
    }

    public void shutDown() {
        long shutDownAt = System.currentTimeMillis();
        try {
            lock.lock();
            if (stub != null && stub.getChannel() != null) {
                if (stub.getChannel() instanceof ManagedChannel) {
                    try {
                        ManagedChannel mChannel = ((ManagedChannel) stub.getChannel());
                        mChannel.shutdown();
                        if (!mChannel.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                            log.error("Threads didn't finish in 500 milis!");
                        }
                    } catch (InterruptedException e) {
                        log.error("ERROR", e);
                    } catch (Exception e) {
                        log.error("Shutdown channel is error!!!", e);
                    } finally {
                        log.error("Stop pvs channel is done!!!");
                        stub = null;
                    }
                }
            }
        } finally {
            lock.unlock();
            log.error("Time shutdown: {}", (System.currentTimeMillis() - shutDownAt));
        }
    }

    private Promotion buildPromotionModel(DicePromotionResponse value, String userId) {
        if(value == null)
            return null;
        Promotion result = new Promotion();
        if(value.getCode() == 0) {
            DicePromotionData promotionData = value.getPromotionData();
            result.setCode(promotionData.getPromotionCode());
            result.setName(promotionData.getPromotionName());
            result.setValid(promotionData.getIsValid());
            result.setExpireAt(promotionData.getExpireAt());
            Value data = promotionData.getData().getFields().get("betid");
            result.setBetId(data.getStringValue());
            data = promotionData.getData().getFields().get("remain");
            result.setRemain((int)data.getNumberValue());
            data = promotionData.getData().getFields().get("total");
            result.setTotal((int)data.getNumberValue());
            result.setUserId(userId);
            result.setNotifyStatus(DiceGameConstant.PROMOTION_NOTIFIED_STATUS);
            if (StringUtils.isEmpty(promotionData.getCurrency())) {
                ConfigManager configManager = BeanUtils.getBean(ConfigManager.class);
                IDiceMachineConfig normalConfig = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
                result.setCurrency(normalConfig.getAvailableCurrency("").name());
            } else {
                result.setCurrency(promotionData.getCurrency());
            }
        }
        result.setStatus(value.getCode());
        return result;
    }

    @Override
    public void updateServiceHasPromotion(String serviceId, String commandId, String prefixService) {
        //log
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(commandId)
                .actorId(serviceId)
                .serviceId(serviceId)
                .psId("")
                .stateName("PromotionServiceImpl.checkServiceHasPromotion")
                .owner(LogMessage.OWNER_PROMOTION)
                .stepName("checkServiceHasPromotion");

        try {
            DiceCheckServiceHasPromotionRequest.Builder builder = DiceCheckServiceHasPromotionRequest.newBuilder();
            builder.setCommandId(commandId);
            builder.setServiceId(prefixService +serviceId);

            DiceCheckServiceHasPromotionResponse result = initial().withDeadlineAfter(3, TimeUnit.SECONDS).checkServiceHasPromotion(builder.build());
            if(result != null) {
//                log.info("Check checkUserHasPromotion status response {}", result);
                logBuilder.rootCmdId(commandId).message("Has promotion - " + result).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogDebug(logBuilder.build());
                hasPromotion = result.getResult();
            } else {
                logBuilder.stepName("checkUserHasPromotion-Exception")
                        .message("Dont have promotion code")
                        .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogError(logBuilder.build());
//                log.error("ERROR --Check checkUserHasPromotion {} - error {}", userId, promotion);
            }
        } catch (StatusRuntimeException e) {
//            log.error("ERROR --Check checkUserHasPromotion {} - error {}", userId, e);
            logBuilder.stepName("checkUserHasPromotion-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
        } catch (Exception e) {
//            log.error("ERROR --Check checkUserHasPromotion {} - error {}", userId, e);
            logBuilder.stepName("checkUserHasPromotion-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
        }
    }

    @Override
    public boolean hasPromotion() {
        return hasPromotion;
    }

}
