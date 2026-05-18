package com.io.begstd.slot.services.internal;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.common.SlotGameError;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.CommandIdHistory;
import com.io.begstd.slot.model.app.GameState;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.ISlotConfigGamble;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.services.external.PlayerViewStoreService;
import com.io.begstd.slot.services.external.PromotionService;
import com.io.begstd.slot.services.internal.impl.QueueHistoryServiceImpl;
import com.io.begstd.slot.utils.ConfigManager;
import com.io.begstd.slot.utils.kafka.IProducerKafkaLog;
import com.io.begstd.wallet.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class BaseSpinService {

    @Autowired
    private Environment environment;
    
    @Autowired
    private WalletService walletService;

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerViewStoreService playerViewStoreService;
   
    @Autowired
    private IProducerKafkaLog kafkaLogService;

    @Autowired
    private WalletTrialModeService walletTrialModeService;
    
    @Autowired
    private PromotionService promotionService;
    
    @Autowired
    protected ConfigManager configManager;

    @Autowired
    private QueueHistoryServiceImpl<String, CommandIdHistory> queueHistoryUtils;

    @Value(value = "${gamble.expiredTime:0}")
    private long expiredTime;


    protected void checkAuthorize(String serviceId, String userId, String commandId) {
        if (!userService.isJoinedGame(serviceId, UserInfo.builder().userId(userId).build())) {
            throw new SlotGameException(SlotGameError.NO_JOIN_GAME, userId, null, commandId);
        }
    }

    protected BasePlaySession finishPlaySession(BasePlaySession basePlaySession, ISlotMachineConfig slotMachineConfigNormal, UserInfo userInfo) {
        String addResult = "";

        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(basePlaySession.commandId())
            .actorId(basePlaySession.userId())
            .serviceId(basePlaySession.serviceId())
            .psId(basePlaySession.uuid())
            .stateName("BaseSpinService.finishPlaySession");

        BasePlaySession returnBasePlaySession = basePlaySession;
        BasePlaySession.BasePlaySessionBuilder<?,?> builder = basePlaySession.toBuilder();
        builder.lastModified(lStartTimeGolbal);

        if (basePlaySession.state() == GameState.GAMBLE_GAME) {
            if (!basePlaySession.isFinishGamble()) {
                addResult = this.walletService.addWalletAmount(returnBasePlaySession, slotMachineConfigNormal.totalCredit(),
                    slotMachineConfigNormal.serviceCode() + ":"+ slotMachineConfigNormal.serviceName(),
                    slotMachineConfigNormal.prefixService());
            }
            if (basePlaySession.gambleGameRemain() == 0) { // finish
                //lose - add money in wallet
                  userService.removeGamblePlaySession(basePlaySession);
                  builder.isFinishGamble(true);
                  builder.isFinished(true);
              } else {
               // save the session for next spin - update time for expired
                  builder.savedTimeOfPlaySession(Instant.now().toEpochMilli());
                  userService.saveGamblePlaySession(builder.build());
              }
        } else {
            // NORMAL, FREE, BONUS, FREEGAME OPTION
            if (basePlaySession.checkFinish()) {
                
                // add money to Wallet - call Wallet service
                builder.isFinished(true);
                returnBasePlaySession = builder.build();
                if (!returnBasePlaySession.isTrialMode()) {
                    addResult = this.walletService.addWalletAmount(returnBasePlaySession, slotMachineConfigNormal.totalCredit(),
                        slotMachineConfigNormal.serviceCode() + ":"+ slotMachineConfigNormal.serviceName(),
                        slotMachineConfigNormal.prefixService());
                    
                    if (!"0".equals(addResult)) {
                        logBuilder.stepName("Added Wallet").owner(LogMessage.OWNER_WALLET)
                            .message("Response from addWalletAmount: " + returnBasePlaySession.winAmount() + " -- return code:"+addResult);
    
                        addResult = "Added wallet response: " + addResult;
                        logBuilder.stepName("Add Wallet")
                            .message("Error of " + addResult + ". Amount: "+ returnBasePlaySession.winAmount())
    
                            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
                        LogsUtils.writeLogError(logBuilder.build());
                    }
                } else {
                    // NONE ADD MONEY TO WALLLET FOR TRIAL DATA
                    this.walletTrialModeService.addWalletAmount(returnBasePlaySession, slotMachineConfigNormal.totalCredit(),
                            slotMachineConfigNormal.serviceCode() + ":"+ slotMachineConfigNormal.serviceName(),
                            slotMachineConfigNormal.prefixService());
                }

                // has gamble mode
                if (expiredTime > 0) {
                    if (basePlaySession.winAmount().gt(Money.ZERO)) {
                        ISlotConfigGamble slotMachineConfigGamble = (ISlotConfigGamble) configManager.getConfigMain(SlotConfigMode.GAMBLE);
                        // win gamble
                        int gambleAward = slotMachineConfigGamble.numPlayTotalInGamble();
                        builder.addGambleGameCount(gambleAward);
                        builder.gamblerUserBet(basePlaySession.winAmount());
                        
                        //set time for gamble
                        builder.savedTimeOfPlaySession(Instant.now().toEpochMilli());
                        builder.expiredTime(expiredTime);
                        builder.isFinishGamble(false);
                        userService.saveGamblePlaySession(builder.build());
                    }
                }
                userService.removePlaySession(returnBasePlaySession);
            } else {
                userService.savePlaySession(builder.build());
            }
        }

        returnBasePlaySession = builder.build();

        boolean resUpdate = this.playerViewStoreService.updateState(returnBasePlaySession);
        
      //call commit for promotion case
        if ((basePlaySession.state() == GameState.NORMAL_GAME) && (!StringUtils.isEmpty(basePlaySession.promotionCode()))) {
            promotionService.commitPromotionData(basePlaySession.serviceId(), basePlaySession.uuid(), basePlaySession.userId(), basePlaySession.promotionCode(),
                slotMachineConfigNormal.prefixService(), basePlaySession.currency());
        }
        
        // support to writting log for normal and trial - 18-08-2021
        log.info(kafkaLogService.getKafkaMessage(returnBasePlaySession, slotMachineConfigNormal.prefixService(), addResult));
        
        queueHistoryUtils.insert(basePlaySession.userId() + "_" + basePlaySession.commandId(), new CommandIdHistory(slotMachineConfigNormal.serviceId(), basePlaySession.commandId(),
            basePlaySession.userId(), true, ""));

        logBuilder.stepName("End").owner(LogMessage.OWNER_GAME)
            .message("PS mode: " + (returnBasePlaySession.isTrialMode() ? "Trial mode": "Normal mode"))
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
    
        return returnBasePlaySession;
    }

    public BasePlaySession finishPlaySessionRtp(BasePlaySession basePlaySession) {

        BasePlaySession returnBasePlaySession = basePlaySession;
        BasePlaySession.BasePlaySessionBuilder<?,?> builder = basePlaySession.toBuilder();

        if ((basePlaySession.freeGameRemain() == 0) &&
                (basePlaySession.bonusGameRemain() == 0) &&
                (basePlaySession.freeGameOption() == null || basePlaySession.freeGameOption().size() == 0) &&
                (basePlaySession.lightningGameRemain() == 0) &&
                (basePlaySession.powerUpGameRemain() == 0)) {

            builder.isFinished(true);
            returnBasePlaySession = builder.build();
        }
        if(Arrays.asList(environment.getActiveProfiles()).contains("mock")) {
            this.playerViewStoreService.updateState(returnBasePlaySession);
        }

        return returnBasePlaySession;
    }

}
