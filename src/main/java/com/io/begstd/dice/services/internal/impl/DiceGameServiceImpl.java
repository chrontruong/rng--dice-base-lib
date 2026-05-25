package com.io.begstd.dice.services.internal.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Struct;
import com.io.begstd.dice.services.internal.JackpotTrialModeService;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.repository.IRedisDiceConfigRepository;
import com.io.begstd.dice.repository.RedisConfigRTPRepository;
import com.io.begstd.dice.services.external.JackpotService;
import com.io.begstd.dice.services.external.PlayerViewStoreService;
import com.io.begstd.dice.services.internal.BaseTrialService;
import com.io.begstd.dice.services.internal.NormalGameService;
import com.io.begstd.dice.services.internal.DiceGameService;
import com.io.begstd.dice.services.internal.UserLockService;
import com.io.begstd.dice.services.internal.UserService;
import com.io.begstd.dice.services.internal.WalletTrialModeService;
import com.io.begstd.dice.utils.ConfigManager;
import com.io.begstd.dice.utils.GameUtils;
import com.io.begstd.dice.command.SpinCmd;
import com.io.begstd.dice.common.DiceGameError;
import com.io.begstd.dice.exception.DiceGameException;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceDirrectlyStatePush;
import com.io.begstd.dice.grpc.promotionservice.DicePromotionData;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
@Accessors(fluent = true)
public class DiceGameServiceImpl implements DiceGameService, ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private UserService userService;

    @Autowired
    private UserLockService userLockService;

    @Autowired
    private NormalGameService normalGameService;

    @Autowired
    private PlayerViewStoreService playerViewStoreService;

    @Autowired
    private JackpotService jackpotService;

    @Autowired
    private WalletTrialModeService walletTrialModeService;

    @Autowired
    private JackpotTrialModeService jackpotTrialModeService;

    @Autowired
    RedisConfigRTPRepository redisConfigRTPRepositoryImpl;

//    @Autowired
//    private ApplicationContext appContext;

    @Autowired
    private ConfigManager configManager;

    @Autowired
    @Qualifier("objectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private IRedisDiceConfigRepository redisDiceConfigRepository;

    @Autowired
    private Environment environment;

    @Autowired
    private BaseTrialService trialService;

    @Override
    public int joinGame(String commandId, UserInfo userInfo, DicePromotionData promotion, int env) {
        IDiceMachineConfig diceMachineConfig = (IDiceMachineConfig) configManager.getConfigMain( DiceConfigMode.NORMAL);
        this.walletTrialModeService.removeUser(userInfo.userId());
        this.jackpotTrialModeService.removeUserJackpot(userInfo.userId(), diceMachineConfig.getJACKPOTS());
        return userService.joinGame(commandId, userInfo, promotion, env);
    }

    @Override
    public int leaveGame(String commandId, UserInfo userInfo) {
        return userService.leaveGame(commandId, userInfo);
    }

    @Override
    public int leaveGameTrial(String commandId, UserInfo userInfo) {
        try {
            trialService.clearPlaySession(userInfo);
            trialService.clearProcessAfterSpin(userInfo);
            long lStartTimeGolbal = Instant.now().toEpochMilli();
            IDiceMachineConfig diceMachineConfig = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
            DiceDirrectlyStatePush.Builder builder = DiceDirrectlyStatePush.newBuilder();
            builder.setData(Struct.newBuilder()
                    .putFields("status", com.google.protobuf.Value.newBuilder().setStringValue("0").build())
                    .putFields("cId", com.google.protobuf.Value.newBuilder().setStringValue(commandId).build())
                    .build());
            boolean response = this.playerViewStoreService.notifyMessage(diceMachineConfig.serviceId(), userInfo,
                    commandId, "lgtr", builder.build());
            LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
            logBuilder
                    .cmdId(commandId)
                    .actorId(userInfo.userId())
                    .serviceId(diceMachineConfig.serviceId())
                    .stateName("UserServiceImpl.leaveGameTrial")
                    .psId("").stepName("leaveGameTrial").owner(LogMessage.OWNER_GAME).message("Passed - leaveGameTrial successful " + response)
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogDebug(logBuilder.build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            long lStartTimeGolbal = Instant.now().toEpochMilli();
            IDiceMachineConfig diceMachineConfig = (IDiceMachineConfig) configManager.getConfigMain( DiceConfigMode.NORMAL);
            DiceDirrectlyStatePush.Builder builder = DiceDirrectlyStatePush.newBuilder();
            builder.setData(Struct.newBuilder()
                    .putFields("status", com.google.protobuf.Value.newBuilder().setStringValue("1").build())
                    .putFields("cId", com.google.protobuf.Value.newBuilder().setStringValue(commandId).build())
                    .build());
            boolean response = this.playerViewStoreService.notifyMessage(diceMachineConfig.serviceId(), userInfo,
                    commandId, "lgtr", builder.build());
            LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
            logBuilder
                    .cmdId(commandId)
                    .actorId(userInfo.userId())
                    .serviceId(diceMachineConfig.serviceId())
                    .stateName("UserServiceImpl.leaveGameTrial")
                    .psId("").stepName("leaveGameTrial").owner(LogMessage.OWNER_GAME).message("Passed - leaveGameTrial successful" + response)
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogDebug(logBuilder.build());
        }
        return 0;
    }

    @Override
    public BasePlaySession normalSpin(String commandId, UserInfo userInfo, SpinCmd cmd) {
        IDiceMachineConfig diceMachineConfig = (IDiceMachineConfig) configManager.getConfigMain( DiceConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(diceMachineConfig.serviceId())
            .psId("")
            .stateName("DiceGameServiceImpl.normalSpin");

        if(!userLockService.isLocked2Added(diceMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - normalgame")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new DiceGameException(DiceGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), userInfo.userType(), commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - normalgame")
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, diceMachineConfig.serviceId());
            return normalGameService.spin(commandId, userInfo, cmd);
        } finally {
            userLockService.unLocked(diceMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of Normal")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }

    @Override
    public int getLatestState(String commandId, UserInfo userInfo) {
        IDiceMachineConfig diceMachineConfig = (IDiceMachineConfig) configManager.getConfigMain( DiceConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(diceMachineConfig.serviceId())
            .psId("")
            .stateName("DiceGameServiceImpl.getLatestState");


        if(!userLockService.isLocked2Added(diceMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - getLatestState")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new DiceGameException(DiceGameError.WAITING_GLT, userInfo.userId(), "", commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - getLatestState")
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, diceMachineConfig.serviceId());
            BasePlaySession basePlaySession = userService.getPlaySession(diceMachineConfig.serviceId(), userInfo);

            if (basePlaySession != null) {

                if(basePlaySession.isTrialMode()) {
                    userService.removePlaySession(basePlaySession);
                    basePlaySession = null;
                } else {
                    if(userInfo.userId().equals(basePlaySession.userId())) {
                        this.playerViewStoreService.updateLatestState(basePlaySession, 2);
                    }
                }
            }
            return playerViewStoreService.pushState(diceMachineConfig.serviceId(), userInfo.userId(), userInfo.userType(), commandId) == true ? 1:0;

        } finally {
            userLockService.unLocked(diceMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of getLatestState")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }
    /*-------------------------- TRIAL MODE -------------------------------------------*/
    @Override
    public BasePlaySession normalSpinTrial(String commandId, UserInfo userInfo, SpinCmd cmd) {
        IDiceMachineConfig diceMachineConfig = (IDiceMachineConfig) configManager.getConfigMain( DiceConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(diceMachineConfig.serviceId())
            .psId("")
            .stateName("DiceGameServiceImpl.normalSpinTrial");

        if(!userLockService.isLocked2Added(diceMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - normalSpinTrial")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new DiceGameException(DiceGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), userInfo.userType(), commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - normalSpinTrial")
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, diceMachineConfig.serviceId());

            GameUtils.initDataForTrialMode(userService, commandId, userInfo, diceMachineConfig.serviceId(),
                walletTrialModeService, jackpotTrialModeService, diceMachineConfig.initTrialWalet(),
                diceMachineConfig.getJackpotListForTrial());

            return normalGameService.spinTrial(commandId, userInfo, cmd);
        } finally {
            userLockService.unLocked(diceMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of normalSpinTrial")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        saveRtpConfigToRedis();
        initGameInfo();
    }

    private void saveRtpConfigToRedis() {
        IDiceMachineConfig diceMachineConfig = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
        redisConfigRTPRepositoryImpl.save(diceMachineConfig.serviceId(), diceMachineConfig.rtpDiceConifgs());
        if (!Arrays.asList(environment.getActiveProfiles()).contains("rtp")) {
            if (!redisConfigRTPRepositoryImpl.isExistedRtp(diceMachineConfig.serviceId())) {
                redisConfigRTPRepositoryImpl.saveRtp(diceMachineConfig.serviceId(), diceMachineConfig.rtpDefault());
            }
        }
    }

    public void initGameInfo() {
        IDiceMachineConfig diceMachineConfig = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId("Bootstrap")
            .actorId("Bootstrap")
            .serviceId(diceMachineConfig.serviceId())
            .psId("")
            .stateName("DiceGameServiceImpl.initGameInfo");

        this.playerViewStoreService.registerState(String.valueOf(diceMachineConfig.serviceId()),
                diceMachineConfig.intervalGame());
        logBuilder.stepName("RegisterState").owner(LogMessage.OWNER_PVS)
            .message("Passed - intervalGame "+ diceMachineConfig.intervalGame())
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());

        this.playerViewStoreService.subscribeJackpot(String.valueOf(diceMachineConfig.serviceId()),
            new ArrayList<>(diceMachineConfig.getJACKPOTS().keySet()));
        logBuilder.stepName("subscribeJackpot").owner(LogMessage.OWNER_PVS)
            .message("Passed - jackpot " + diceMachineConfig.getJACKPOTS().keySet())
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());

        lStartTimeGolbal = Instant.now().toEpochMilli();
        this.jackpotService.initJackpot(diceMachineConfig.prefixService(), String.valueOf(diceMachineConfig.serviceId()), "command_initJackpot",
                diceMachineConfig.getJACKPOTS());

        logBuilder.stepName("InitJackpot").owner(LogMessage.OWNER_JACKPOT).message("Passed - Init jackpot "+ diceMachineConfig.getJACKPOTS().toString())
        .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());
    }
}
