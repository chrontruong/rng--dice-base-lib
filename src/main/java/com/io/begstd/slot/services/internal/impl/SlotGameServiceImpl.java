package com.io.begstd.slot.services.internal.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Struct;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.common.SlotGameError;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.grpc.playerviewstoreservice.DirrectlyStatePush;
import com.io.begstd.slot.grpc.promotionservice.PromotionData;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.repository.IRedisSlotConfigRepository;
import com.io.begstd.slot.repository.RedisConfigRTPRepository;
import com.io.begstd.slot.services.external.JackpotService;
import com.io.begstd.slot.services.external.PlayerViewStoreService;
import com.io.begstd.slot.services.internal.*;
import com.io.begstd.slot.utils.ConfigManager;
import com.io.begstd.slot.utils.GameUtils;
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
public class SlotGameServiceImpl implements SlotGameService, ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private UserService userService;

    @Autowired
    private UserLockService userLockService;

    @Autowired
    private NormalGameService normalGameService;

    @Autowired
    private MiniGameService miniGameService;

    @Autowired
    private FreeGameService freeGameService;

    @Autowired
    private RespinGameService respinGameService;
    
    @Autowired
    private FreeSpinOptionGameService freeSpinOptionGameService;

    @Autowired
    private GambleService gambleService;

    @Autowired
    private LightningGameService lightningGameService;

    @Autowired
    private PowerUpGameService powerUpGameService;

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
    private IRedisSlotConfigRepository redisSlotConfigRepository;

    @Autowired
    private Environment environment;

    @Autowired
    private BaseTrialService trialService;

    @Override
    public int joinGame(String commandId, UserInfo userInfo, PromotionData promotion, int env) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        this.walletTrialModeService.removeUser(userInfo.userId());
        this.jackpotTrialModeService.removeUserJackpot(userInfo.userId(), slotMachineConfig.getJACKPOTS());
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
            ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
            DirrectlyStatePush.Builder builder = DirrectlyStatePush.newBuilder();
            builder.setData(Struct.newBuilder()
                    .putFields("status", com.google.protobuf.Value.newBuilder().setStringValue("0").build())
                    .putFields("cId", com.google.protobuf.Value.newBuilder().setStringValue(commandId).build())
                    .build());
            boolean response = this.playerViewStoreService.notifyMessage(slotMachineConfig.serviceId(), userInfo,
                    commandId, "lgtr", builder.build());
            LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
            logBuilder
                    .cmdId(commandId)
                    .actorId(userInfo.userId())
                    .serviceId(slotMachineConfig.serviceId())
                    .stateName("UserServiceImpl.leaveGameTrial")
                    .psId("").stepName("leaveGameTrial").owner(LogMessage.OWNER_GAME).message("Passed - leaveGameTrial successful " + response)
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogDebug(logBuilder.build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            long lStartTimeGolbal = Instant.now().toEpochMilli();
            ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
            DirrectlyStatePush.Builder builder = DirrectlyStatePush.newBuilder();
            builder.setData(Struct.newBuilder()
                    .putFields("status", com.google.protobuf.Value.newBuilder().setStringValue("1").build())
                    .putFields("cId", com.google.protobuf.Value.newBuilder().setStringValue(commandId).build())
                    .build());
            boolean response = this.playerViewStoreService.notifyMessage(slotMachineConfig.serviceId(), userInfo,
                    commandId, "lgtr", builder.build());
            LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
            logBuilder
                    .cmdId(commandId)
                    .actorId(userInfo.userId())
                    .serviceId(slotMachineConfig.serviceId())
                    .stateName("UserServiceImpl.leaveGameTrial")
                    .psId("").stepName("leaveGameTrial").owner(LogMessage.OWNER_GAME).message("Passed - leaveGameTrial successful" + response)
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogDebug(logBuilder.build());
        }
        return 0;
    }

    @Override
    public BasePlaySession normalSpin(String commandId, UserInfo userInfo, SpinCmd cmd) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.normalSpin");

        if(!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - normalgame")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), userInfo.userType(), commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - normalgame")
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            return normalGameService.spin(commandId, userInfo, cmd);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of Normal")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }

    @Override
    public BasePlaySession freeSpin(String commandId, UserInfo userInfo) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.freeSpin");

        if(!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - freegame")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), "", commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - freegame")
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            return freeGameService.spin(commandId, userInfo);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of Free")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }

    @Override
    public BasePlaySession freeSpinOption(String commandId, UserInfo userInfo, int selectedOption) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.freeSpinOption");

        if(!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("Error - User in progress action - freegameoption")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), "", commandId);
        }

        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - freegameoption:"+selectedOption)

            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            return freeSpinOptionGameService.spin(commandId, userInfo, selectedOption);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of FreeSpinOption")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }

    @Override
    public BasePlaySession respin(String commandId, UserInfo userInfo) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
                .cmdId(commandId)
                .actorId(userInfo.userId())
                .serviceId(slotMachineConfig.serviceId())
                .psId("")
                .stateName("SlotGameServiceImpl.respin");

        if (!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - freegame")
                    .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), "", commandId);
        }
        logBuilder.rootCmdId(commandId).stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - freegame")
                .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            return respinGameService.spin(commandId, userInfo);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.rootCmdId(commandId).stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of Free")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal).actionFinish(true);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }
    
    @Override
    public BasePlaySession playMiniGame(String commandId, UserInfo userInfo, int openCell) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.playMiniGame");

        if(!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - miniGame")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), "", commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - minigame")
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            return miniGameService.play(commandId, userInfo, openCell);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of MiniGame")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }

    @Override
    public BasePlaySession lightingSpin(String commandId, UserInfo userInfo) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.lightingSpin");

        if(!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - lightingSpin")
            .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), "", commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - lightingSpin")
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            return lightningGameService.spin(commandId, userInfo);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of lightingSpin")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }

    @Override
    public BasePlaySession powerUpSpin(String commandId, UserInfo userInfo, int openCell) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.powerUpSpin");

        if(!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - powerUpSpin")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), "", commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - powerUpSpin")
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            return powerUpGameService.spin(commandId, userInfo, openCell);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of powerUpSpin")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }

    @Override
    public BasePlaySession gamble(String commandId, UserInfo userInfo, int openCell, double totalBet) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.gamble");

        if(!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - gamble")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), "", commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - gamble")
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            return gambleService.spin(commandId, userInfo, openCell,  totalBet);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of gamble")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }

    @Override
    public int getLatestState(String commandId, UserInfo userInfo) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.getLatestState");


        if(!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - getLatestState")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.WAITING_GLT, userInfo.userId(), "", commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - getLatestState")
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            BasePlaySession basePlaySession = userService.getPlaySession(slotMachineConfig.serviceId(), userInfo);

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
            return playerViewStoreService.pushState(slotMachineConfig.serviceId(), userInfo.userId(), userInfo.userType(), commandId) == true ? 1:0;

        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of getLatestState")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }
    /*-------------------------- TRIAL MODE -------------------------------------------*/
    @Override
    public BasePlaySession normalSpinTrial(String commandId, UserInfo userInfo, SpinCmd cmd) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.normalSpinTrial");

        if(!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - normalSpinTrial")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), userInfo.userType(), commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - normalSpinTrial")
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());

            GameUtils.initDataForTrialMode(userService, commandId, userInfo, slotMachineConfig.serviceId(),
                walletTrialModeService, jackpotTrialModeService, slotMachineConfig.initTrialWalet(),
                slotMachineConfig.getJackpotListForTrial());

            return normalGameService.spinTrial(commandId, userInfo, cmd);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of normalSpinTrial")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }

    @Override
    public BasePlaySession freeSpinTrial(String commandId, UserInfo userInfo) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.freeSpinTrial");

        if(!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - freeSpinTrial")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), "", commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - freeSpinTrial")
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            return freeGameService.spinTrial(commandId, userInfo);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of freeSpinTrial")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }
    @Override
    public BasePlaySession respinTrial(String commandId, UserInfo userInfo) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
                .cmdId(commandId)
                .actorId(userInfo.userId())
                .serviceId(slotMachineConfig.serviceId())
                .psId("")
                .stateName("SlotGameServiceImpl.respinTrial");

        if (!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - freeSpinTrial")
                    .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), "", commandId);
        }
        logBuilder.rootCmdId(commandId).stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - freeSpinTrial")
                .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            return respinGameService.spinTrial(commandId, userInfo);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.rootCmdId(commandId).stepName("unLocked").owner(LogMessage.OWNER_GAME)
                    .message("User is unlocked in the end of freeSpinTrial")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal).actionFinish(true);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }
    @Override
    public BasePlaySession freeSpinOptionTrial(String commandId, UserInfo userInfo, int selectedOption) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.freeSpinOptionTrial");

        if(!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("Error - User in progress action - freeSpinOptionTrial")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), "", commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - freeSpinOptionTrial:"+selectedOption)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            return freeSpinOptionGameService.spinTrial(commandId, userInfo, selectedOption);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of freeSpinOptionTrial")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }


    @Override
    public BasePlaySession playMiniGameTrial(String commandId, UserInfo userInfo, int openCell) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.playMiniGameTrial");

        if(!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - playMiniGameTrial")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), "", commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - minigameTrial")

            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            return miniGameService.playTrial(commandId, userInfo, openCell);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of MiniGameTrial")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }

    @Override
    public BasePlaySession lightingSpinTrial(String commandId, UserInfo userInfo) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.lightingSpinTrial");

        if(!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - lightingSpinTrial")
            .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), "",commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - lightingSpinTrial")
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            return lightningGameService.spinTrial(commandId, userInfo);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of lightingSpinTrial")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }

    @Override
    public BasePlaySession powerUpSpinTrial(String commandId, UserInfo userInfo, int openCell) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.powerUpSpinTrial");

        if(!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - powerUpSpinTrial")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), "", commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - powerUpSpinTrial")
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            return powerUpGameService.spinTrial(commandId, userInfo, openCell);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of powerUpSpinTrial")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        }
    }

    @Override
    public BasePlaySession gambleTrial(String commandId, UserInfo userInfo, int openCell, double totalBet) {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain( SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.gambleTrial");

        if(!userLockService.isLocked2Added(slotMachineConfig.serviceId(), userInfo.userId())) {
            logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User in progress action - gambleTrial")
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.ERROR_USER_IN_PROGRESS, userInfo.userId(), "", commandId);
        }
        logBuilder.stepName("isLocked").owner(LogMessage.OWNER_GAME).message("User is locked - gambleTrial")
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        try {
            GameUtils.checkingCommandIdInQueue(this.userService, commandId, userInfo, slotMachineConfig.serviceId());
            return gambleService.spinTrial(commandId, userInfo, openCell,  totalBet);
        } finally {
            userLockService.unLocked(slotMachineConfig.serviceId(), userInfo.userId());
            logBuilder.stepName("unLocked").owner(LogMessage.OWNER_GAME).message("User is unlocked in the end of gambleTrial")

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
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
        redisConfigRTPRepositoryImpl.save(slotMachineConfig.serviceId(), slotMachineConfig.rtpSlotConifgs());
        if (!Arrays.asList(environment.getActiveProfiles()).contains("rtp")) {
            if (!redisConfigRTPRepositoryImpl.isExistedRtp(slotMachineConfig.serviceId())) {
                redisConfigRTPRepositoryImpl.saveRtp(slotMachineConfig.serviceId(), slotMachineConfig.rtpDefault());
            }
        }
    }

    public void initGameInfo() {
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId("Bootstrap")
            .actorId("Bootstrap")
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("SlotGameServiceImpl.initGameInfo");

        this.playerViewStoreService.registerState(String.valueOf(slotMachineConfig.serviceId()),
                slotMachineConfig.intervalGame());
        logBuilder.stepName("RegisterState").owner(LogMessage.OWNER_PVS)
            .message("Passed - intervalGame "+ slotMachineConfig.intervalGame())
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());

        this.playerViewStoreService.subscribeJackpot(String.valueOf(slotMachineConfig.serviceId()),
            new ArrayList<>(slotMachineConfig.getJACKPOTS().keySet()));
        logBuilder.stepName("subscribeJackpot").owner(LogMessage.OWNER_PVS)
            .message("Passed - jackpot " + slotMachineConfig.getJACKPOTS().keySet())
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());

        lStartTimeGolbal = Instant.now().toEpochMilli();
        this.jackpotService.initJackpot(slotMachineConfig.prefixService(), String.valueOf(slotMachineConfig.serviceId()), "command_initJackpot",
                slotMachineConfig.getJACKPOTS());

        logBuilder.stepName("InitJackpot").owner(LogMessage.OWNER_JACKPOT).message("Passed - Init jackpot "+ slotMachineConfig.getJACKPOTS().toString())
        .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());
    }
}
