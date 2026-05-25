package com.io.begstd.dice.services.internal.impl;

import com.google.protobuf.Value;
import com.io.begstd.extension.loader.DiceExtensionManagerImpl;
import com.io.begstd.extension.loader.ExtensionLoader;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.CommandIdHistory;
import com.io.begstd.dice.model.app.Promotion;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.model.config.CurrencyType;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.repository.PromotionRepositoryService;
import com.io.begstd.dice.repository.RedisCommandIdHistoryRepository;
import com.io.begstd.dice.repository.impl.RedisPlaySessionRepositoryImpl;
import com.io.begstd.dice.services.extension.common.ExtraDataInitGameExtension;
import com.io.begstd.dice.services.extension.common.ResumeExtension;
import com.io.begstd.dice.services.external.PlayerViewStoreService;
import com.io.begstd.dice.services.external.PromotionService;
import com.io.begstd.dice.services.internal.UserLockService;
import com.io.begstd.dice.services.internal.UserService;
import com.io.begstd.dice.utils.ConfigManager;
import com.io.begstd.dice.utils.GameUtils;
import com.io.begstd.dice.common.DiceGameConstant;
import com.io.begstd.dice.common.DiceGameError;
import com.io.begstd.dice.exception.DiceGameException;
import com.io.begstd.dice.grpc.promotionservice.DicePromotionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserLockService userLockService;

    @Autowired
    private PlayerViewStoreService playerViewStoreService;

    @Autowired
    private RedisPlaySessionRepositoryImpl playSessionRepository;

    @Autowired
    private RedisCommandIdHistoryRepository commandIdRepository;

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private PromotionRepositoryService promotionRepository;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private ExtensionLoader<DiceExtensionManagerImpl> extensionLoader;

    private IDiceMachineConfig slotMachineConfig;

    @PostConstruct
    public void init() {
        slotMachineConfig = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
    }

    public int joinGame(String commandId, UserInfo userInfo, DicePromotionData promotionData, int env) {

        DiceExtensionManagerImpl extensionManager = extensionLoader.getExtensionManager();
        ExtraDataInitGameExtension extraDataInitExtension = extensionManager.extraDataInitGameExtension();
        ResumeExtension resumeExtension = extensionManager.resumeExtension();

        String serviceId = slotMachineConfig.serviceId();

        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(serviceId)
            .stateName("UserServiceImpl.JoinGame");
        // validate joinGame.
        // Rule 1: don't have any playSession is Running belong to the user.

//        joinGameRepository.save(serviceId, new UserJoinGame(userId, userType, userName));

        Promotion promotion = null;
        CurrencyType currencyType = slotMachineConfig.getAvailableCurrency(userInfo.currency());
        if (promotionData != null) {
            promotion = buildPromotionModel(promotionData, userInfo.userId());
        } else {
            promotionRepository.remove(serviceId, userInfo.userId(), currencyType.name());
        }

        if(promotion != null && promotion.getStatus() == 0 && promotion.isValid() && promotion.getRemain() > 0) {
            // check betId
            if (this.slotMachineConfig.isValidBet(promotion.getBetId(), currencyType.name())
                    && isValidEnv(env, promotion.getBetId(), currencyType.name())) {
                promotionRepository.save(serviceId, promotion);
            } else {
                promotionRepository.remove(serviceId, userInfo.userId(), currencyType.name());
                promotion = null;
            }
        } else if(promotion != null && (!promotion.isValid() || promotion.getRemain() <= 0)) {
            promotionRepository.remove(serviceId, userInfo.userId(), currencyType.name());
        }
        logBuilder
            .stepName("checkUserHasPromotion")
            .owner(LogMessage.OWNER_PROMOTION)
            .message("User: " + userInfo.userId() + " to " + serviceId + ": promotion - " + promotion)
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());

        BasePlaySession basePlaySession = resumeExtension.getPlaySession(playSessionRepository, slotMachineConfig.serviceId(), userInfo.userId(), userInfo.currency());
        String psId = "";
        // check playsession
        if (basePlaySession != null) {
            if(basePlaySession.isTrialMode()) {
                playSessionRepository.removePlaySession(basePlaySession);
                basePlaySession = null;
            } else {
                if(userInfo.userId().equals(basePlaySession.userId())) {
                    this.playerViewStoreService.updateLatestState(basePlaySession, 1);
                } else {
                    String sf = String.format("ERROR USERID %s, PS %s", userInfo.userId(), basePlaySession.userId());
                    logBuilder.psId(basePlaySession.uuid()).stepName("Uncorrect Playsession").owner(LogMessage.OWNER_GAME).message(sf)
                        .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
                    LogsUtils.writeLogError(logBuilder.build());
                    throw new DiceGameException(DiceGameError.UNEXPECTED, userInfo.userId(), userInfo.userType(), commandId);
                }
                psId = basePlaySession.uuid();
            }
        }
        // get betId list
        List<String> betIdList = this.slotMachineConfig.getDenominationLevelsForEnv(env, currencyType.name());
        List<String> extraIdList = this.slotMachineConfig.getExtraBetForEnv();
        logBuilder.psId(psId).stepName("BetList")
            .owner(LogMessage.OWNER_GAME).message("Passed - "+betIdList)
            .timeExe(0);
        LogsUtils.writeLogDebug(logBuilder.build());

        List<String> errorCodeList = new ArrayList<String>();
        if(userLockService.isLocked(slotMachineConfig.serviceId(), userInfo.userId())) {
            errorCodeList.add(DiceGameError.ERROR_USER_IN_PROGRESS.getErrorCode());
        }

        //get extra data
        List<String> eDataList = extraDataInitExtension.getExtraDataForInit(slotMachineConfig, userInfo);
        List<String> eCommonDataList = new ArrayList<>();
        eCommonDataList.add("c:"+currencyType.name());
        eCommonDataList.add("v:" + GameUtils.getBuildVersion());
//        if (!TEST.equals(eventServiceFlag) && !StringUtils.isEmpty(userInfo.eventId())) {
//            eDataList = updateExtraDataWithEvent(commandId, userInfo, eDataList);
//        }

        boolean createGrpResult = this.playerViewStoreService.addUserToGroupWithExtraData(commandId, userInfo.userId(), userInfo.userType(),
            String.valueOf(serviceId),
            String.valueOf(serviceId),
            this.slotMachineConfig.stateType(),
            slotMachineConfig.getJACKPOTSByCurrency(userInfo.currency()),
            userInfo.displayName(), Money.of(userInfo.money()), promotion, basePlaySession,
            betIdList, extraIdList, errorCodeList, eDataList, eCommonDataList);

        logBuilder.psId(psId).stepName("End")
            .owner(LogMessage.OWNER_GAME).message("Passed - JoinGame succesful. UserType: " + userInfo.userType() + " -- adduserToGroup:"+createGrpResult)
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());


        return 0;
    }

    private boolean isValidEnv(int env, String betId, String currency) {
        List<String> mainBets = slotMachineConfig.getDenominationLevelsForEnv(env, currency);
        return mainBets != null
                && mainBets.stream().anyMatch(mainBet -> mainBet.startsWith(betId.substring(0, 1)));
    }

    public int leaveGame(String commandId, UserInfo userInfo) {
        long lStartTimeGolbal = Instant.now().toEpochMilli();

        boolean createGrpResult;
        createGrpResult = this.playerViewStoreService.removeUserFromGroup(commandId, userInfo.userId(), userInfo.userType(),
                slotMachineConfig.serviceId(), slotMachineConfig.serviceId());

//        if (createGrpResult) {
//            joinGameRepository.removeUserJoin(slotMachineConfig.serviceId(), userId);
//        }
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(slotMachineConfig.serviceId())
            .stateName("UserServiceImpl.leaveGame")
            .psId("").stepName("leaveGame").owner(LogMessage.OWNER_GAME).message("Passed - leaveGame successful"+createGrpResult)
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
        return 0;
    }

    public boolean isJoinedGame(String serviceId, UserInfo userInfo) {
        return true; //joinGameRepository.isExistedUserId(serviceId, userId);
    }

    @Override
    public BasePlaySession getPlaySession(String serviceId, UserInfo userInfo) {
        return playSessionRepository.get(serviceId, userInfo.userId(), userInfo.currency());
    }

    @Override
    public void removePlaySession(BasePlaySession basePlaySession) {
        playSessionRepository.removePlaySession(basePlaySession);
    }

    @Override
    public void savePlaySession(BasePlaySession basePlaySession) {
        playSessionRepository.save(basePlaySession);
    }

    @Override
    public void saveCommandIdInRedis(CommandIdHistory commandIdHistory) {
        commandIdRepository.saveIfAbsent(commandIdHistory);

    }

    @Override
    public CommandIdHistory getCommandIdInRedis(String commandId, String serviceId) {
        return commandIdRepository.get(commandId, serviceId);
    }

    public String getServiceId() {
        return slotMachineConfig.serviceId();
    }

    private Promotion buildPromotionModel(DicePromotionData value, String userId) {
        if (value == null)
            return null;
        try {
            Promotion result = new Promotion();
            result.setCode(value.getPromotionCode());
            result.setName(value.getPromotionName());
            result.setValid(value.getIsValid());
            result.setExpireAt(value.getExpireAt());
            Value data = value.getData().getFields().get("betid");
            result.setBetId(data.getStringValue());
            data = value.getData().getFields().get("remain");
            result.setRemain((int) data.getNumberValue());
            data = value.getData().getFields().get("total");
            result.setTotal((int) data.getNumberValue());
            result.setUserId(userId);
            result.setNotifyStatus(DiceGameConstant.PROMOTION_NOTIFIED_STATUS);
            if (StringUtils.isEmpty(value.getCurrency())) {
                IDiceMachineConfig normalConfig = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
                result.setCurrency(normalConfig.getAvailableCurrency("").name());
            } else {
                result.setCurrency(value.getCurrency());
            }
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
