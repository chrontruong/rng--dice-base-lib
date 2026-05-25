package com.io.begstd.dice.services.external.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.dice.model.config.CurrencyType;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.services.external.JackpotService;
import com.io.begstd.dice.utils.BeanUtils;
import com.io.begstd.dice.utils.ConfigManager;
import com.io.begstd.dice.config.ExternalServiceEndPointConfiguration;
import com.io.begstd.dice.grpc.jackpotservice.DiceAwardRequest;
import com.io.begstd.dice.grpc.jackpotservice.DiceAwardResponse;
import com.io.begstd.dice.grpc.jackpotservice.GetDiceJackpotRequest;
import com.io.begstd.dice.grpc.jackpotservice.GetDiceJackpotResponse;
import com.io.begstd.dice.grpc.jackpotservice.InitDiceRequest;
import com.io.begstd.dice.grpc.jackpotservice.DiceJackpotInfo;
import com.io.begstd.dice.grpc.jackpotservice.DiceJackpotMultiResponseStatus;
import com.io.begstd.dice.grpc.jackpotservice.DiceJackpotServiceGrpc;
import com.io.begstd.dice.grpc.jackpotservice.MultiplePlusDiceRequest;
import com.io.begstd.dice.grpc.jackpotservice.PlusDiceRequest;
import com.io.begstd.dice.grpc.jackpotservice.DiceResponseStatusJP;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class JackpotServiceImpl implements JackpotService {

    @Autowired
    private ExternalServiceEndPointConfiguration externalServiceEndPointConfiguration;
    private DiceJackpotServiceGrpc.DiceJackpotServiceBlockingStub stub = null;
    private String serviceId = null;
    private static final ReentrantLock lock = new ReentrantLock();

    public DiceJackpotServiceGrpc.DiceJackpotServiceBlockingStub intial() {
        if (stub == null) {
            try {
                lock.lock();
                if (stub == null) {
                    ManagedChannel channel = ManagedChannelBuilder
                            .forAddress(externalServiceEndPointConfiguration.getJackpotHostName(), externalServiceEndPointConfiguration.getJackpotHostPort())
                            .usePlaintext().build();
                    stub = DiceJackpotServiceGrpc.newBlockingStub(channel);
                }
            } finally {
                lock.unlock();
            }
        }
        return stub;
    }
    @Override
    public boolean plusJackpot(String commandID, String jackpotID, double money) {
        DiceResponseStatusJP status = null;
        try {
            status = intial().plus(
                PlusDiceRequest.newBuilder().setCommandId(commandID).setJackpotId(jackpotID).setAmount(money).build());
            log.info("PlusJackpot status response {}", status.getCode());
        } catch (StatusRuntimeException e) {
            log.error("ERROR --plusJackpot commandID {} - jackpotID {} -- Money {} - error {}", commandID, jackpotID,
                money, e);
            shutDown();
            return false;
        } catch (Exception e) {
            log.error("ERROR --plusJackpot commandID {} - jackpotID {} -- Money {} - error {}", commandID, jackpotID,
                money, e);
            shutDown();
            return false;
        }
        return status.getCode().equals("0");
    }
    @Override
    public Money awardJackpot(String commandID, String jackpotID, double money) {
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(commandID)
            .actorId("")
            .serviceId("")
            .psId("")
            .stateName("JackpotServiceImpl")
            .owner(LogMessage.OWNER_JACKPOT)
            .stepName("awardJackpot")
            .message("Input - "+ jackpotID + ". InitAmt: "+ money)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());

        DiceAwardResponse status = null;
        try {
            DiceAwardRequest.Builder awardBuilder = DiceAwardRequest.newBuilder()
                    .setCommandId(commandID)
                    .setJackpotId(jackpotID)
                    .setAmount(money)
                    .setLevel(1);
            status = intial().award(awardBuilder.build());

            logBuilder.stepName("awardJackpot")
                .message("Passed - "+ status.getCode() + ". Amount: "+ status.getAmount())
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());
//            log.info("Award Jackpot status response {}", status.getCode());
            if (status.getCode().equals("0")) {
                return Money.of(status.getAmount());
            }
        } catch (StatusRuntimeException e) {
            logBuilder.stepName("awardJackpot-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);

            shutDown();
        } catch (Exception e) {
            logBuilder.stepName("awardJackpot-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);

            shutDown();
        }

        return Money.ZERO;
    }

    @Override
    public boolean initJackpot(String prefixService, String gameId, String commandID, Map<String, Money> JACKPOTS) {
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(commandID)
            .actorId("")
            .serviceId(gameId)
            .psId("")
            .stateName("JackpotServiceImpl")
            .owner(LogMessage.OWNER_JACKPOT)
            .stepName("initJackpot")
            .message("JackpotList - "+ JACKPOTS)
            .timeExe(0);
            LogsUtils.writeLogDebug(logBuilder.build());

        DiceResponseStatusJP status = null;
        try {
            InitDiceRequest.Builder initBuilder = InitDiceRequest.newBuilder();
            initBuilder.setCommandId(commandID);
            initBuilder.setServiceId(gameId);

            ConfigManager configManager = BeanUtils.getBean(ConfigManager.class);
            IDiceMachineConfig slotMachineConfig = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
            Map<String, String> jpIdCurrencyMap = new HashMap<>();
            for (CurrencyType currencyType : slotMachineConfig.currencyType()) {
                Map<String, Money> jackpots = slotMachineConfig.getJACKPOTSByCurrency(currencyType.name());
                jackpots.keySet().forEach(k -> jpIdCurrencyMap.put(k, currencyType.name()));
            }
            for (Map.Entry<String, Money> entry : JACKPOTS.entrySet()) {
                DiceJackpotInfo.Builder jackpotIInfo = DiceJackpotInfo.newBuilder();
                jackpotIInfo.setJackpotId(entry.getKey());
                jackpotIInfo.setAmount(entry.getValue().value().doubleValue());
                jackpotIInfo.setCurrency(jpIdCurrencyMap.get(entry.getKey()));
                initBuilder.addJackpotInfos(jackpotIInfo);
            }
            initBuilder.setExtServiceId(prefixService + gameId);
            status = intial().init(initBuilder.build());

            logBuilder.message("Init Jackpot status response - "+ status.getCode())
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());

        } catch (StatusRuntimeException e) {
//            log.error("ERROR -- initJackpot commandID {} - jackpotID {} -- gameIs {} - error {}", commandID, gameId, e);
            logBuilder.stepName("initJackpot-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);

            shutDown();
            return false;
        } catch (Exception e) {
//            log.error("ERROR -- initJackpot commandID {} - jackpotID {} -- Money {} - error {}", commandID, gameId, e);
            logBuilder.stepName("initJackpot-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
            return false;
        }

        return status.getCode().equals("0");
    }

    @Override
    public boolean plusMultipleJackpots(String commandID, Map<String, Money> JACKPOTS) {
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(commandID)
            .actorId("")
            .serviceId("")
            .psId("")
            .stateName("JackpotServiceImpl")
            .owner(LogMessage.OWNER_JACKPOT)
            .stepName("plusMultipleJackpots")
            .message("JackpotList - "+ JACKPOTS)
            .timeExe(0);
            LogsUtils.writeLogDebug(logBuilder.build());

        DiceResponseStatusJP status = null;
        try {
            MultiplePlusDiceRequest.Builder initBuilder = MultiplePlusDiceRequest.newBuilder();
            initBuilder.setCommandId(commandID);

//            log.info("Plus multip Jackpot status JACKPOTS.size {}", JACKPOTS.size());
            for (Map.Entry<String, Money> entry : JACKPOTS.entrySet()) {
                DiceJackpotInfo.Builder jackpotIInfo = DiceJackpotInfo.newBuilder();
                jackpotIInfo.setJackpotId(entry.getKey());
                jackpotIInfo.setAmount(entry.getValue().value().doubleValue());
                initBuilder.addJackpotInfos(jackpotIInfo);
            }

            status = intial().plusMultipleJackpots(initBuilder.build());
//            log.info("Init Jackpot status response {}", status.getCode());
            logBuilder.message("Init Jackpot status response - "+ status.getCode())
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());

        } catch (StatusRuntimeException e) {
//            log.error("ERROR -- initJackpot commandID {} - jackpotID {} -- error {}", commandID, JACKPOTS.toString(), e);
            logBuilder.stepName("plusMultipleJackpots-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
            return false;
        } catch (Exception e) {
//            log.error("ERROR -- initJackpot commandID {} - jackpotID {} --  error {}", commandID, JACKPOTS.toString(), e);
            logBuilder.stepName("plusMultipleJackpots-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
            return false;
        }

        return status.getCode().equals("0");
    }

    @Override
    public Map<String, Money> plusMultipleJackpotsWithAmount(String commandID, Map<String, Money> JACKPOTS) {
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(commandID)
            .actorId("")
            .serviceId("")
            .psId("")
            .stateName("JackpotServiceImpl")
            .owner(LogMessage.OWNER_JACKPOT)
            .stepName("plusMultipleJackpotsWithAmount")
            .message("JackpotList - "+ JACKPOTS)
            .timeExe(0);
            LogsUtils.writeLogDebug(logBuilder.build());

        DiceJackpotMultiResponseStatus status = null;
        try {
            MultiplePlusDiceRequest.Builder initBuilder = MultiplePlusDiceRequest.newBuilder();
            initBuilder.setCommandId(commandID);

//            log.info("Plus multip Jackpot status JACKPOTS.size {}", JACKPOTS.size());
            for (Map.Entry<String, Money> entry : JACKPOTS.entrySet()) {
                DiceJackpotInfo.Builder jackpotIInfo = DiceJackpotInfo.newBuilder();
                jackpotIInfo.setJackpotId(entry.getKey());
                jackpotIInfo.setAmount(entry.getValue().value().doubleValue());
                initBuilder.addJackpotInfos(jackpotIInfo);
            }

            status = intial().plusMultipleJackpotsWithAmount(initBuilder.build());
//            log.info("Init Jackpot status response {}", status.getCode());
            logBuilder.message("Init Jackpot status response - "+ status.getCode())
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());


        } catch (StatusRuntimeException e) {
//            log.error("ERROR -- initJackpot commandID {} - jackpotID {} -- error {}", commandID, JACKPOTS.toString(), e);
            logBuilder.stepName("plusMultipleJackpotsWithAmount-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
            return null;
        } catch (Exception e) {
//            log.error("ERROR -- initJackpot commandID {} - jackpotID {} --  error {}", commandID, JACKPOTS.toString(), e);
            logBuilder.stepName("plusMultipleJackpotsWithAmount-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            shutDown();
            return null;
        }

        Map<String, Money> resultMap = new HashMap<String, Money>();
        if (status.getCode().equals("0")) {
            status.getTotalJackpotInfosList().forEach(item -> {
                resultMap.put(item.getJackpotId(), Money.of(item.getAmount()));
            });
        }

        return resultMap;
    }

    @Override
    public Money getJackpot(String commandId, String jackpotID) {
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(commandId)
            .actorId("")
            .serviceId("")
            .psId("")
            .stateName("JackpotServiceImpl")
            .owner(LogMessage.OWNER_JACKPOT)
            .stepName("getJackpot")
            .message("Input - "+ jackpotID )
            .timeExe(0);
        LogsUtils.writeLogDebug(logBuilder.build());

        GetDiceJackpotResponse status = null;
        try {
            status = intial().getJackpot(GetDiceJackpotRequest.newBuilder().setJackpotId(jackpotID).build());

            logBuilder.stepName("getJackpot")
                .message("Passed - "+ status.getJackpotId() + ". Amount: "+ status.getAmount())
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());
            return Money.of(status.getAmount());
        } catch (StatusRuntimeException e) {
            logBuilder.stepName("getJackpot-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);

            shutDown();
        } catch (Exception e) {
            logBuilder.stepName("getJackpot-Exception")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);

            shutDown();
        }

        return Money.ZERO;
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
}
