package com.io.begstd.slot.services.internal.impl;

import com.google.protobuf.Value;
import com.io.begstd.extension.loader.ExtensionLoader;
import com.io.begstd.extension.loader.ExtensionManagerImpl;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.common.SlotGameConstant;
import com.io.begstd.slot.common.SlotGameError;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.grpc.promotionservice.PromotionData;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.CommandIdHistory;
import com.io.begstd.slot.model.app.Promotion;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.CurrencyType;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.repository.PromotionRepositoryService;
import com.io.begstd.slot.repository.RedisCommandIdHistoryRepository;
import com.io.begstd.slot.repository.impl.RedisPlaySessionRepositoryImpl;
import com.io.begstd.slot.services.extension.common.ExtraDataInitGameExtension;
import com.io.begstd.slot.services.extension.common.ResumeExtension;
import com.io.begstd.slot.services.external.PlayerViewStoreService;
import com.io.begstd.slot.services.external.PromotionService;
import com.io.begstd.slot.services.internal.UserLockService;
import com.io.begstd.slot.services.internal.UserService;
import com.io.begstd.slot.utils.ConfigManager;
import com.io.begstd.slot.utils.GameUtils;
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
    private ExtensionLoader<ExtensionManagerImpl> extensionLoader;

    private ISlotMachineConfig slotMachineConfig;

//    @Autowired
//    private IUserEventManager userEventManager;

//    @Value("${service.externalServiceType.eventService}")
//    private String eventServiceFlag;

    @PostConstruct
    public void init() {
        slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
    }

    public int joinGame(String commandId, UserInfo userInfo, PromotionData promotionData, int env) {

        ExtensionManagerImpl extensionManager = extensionLoader.getExtensionManager();
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
//        if(!StringUtils.isEmpty(code)) {
//            promotion = promotionService.usePromotionCode(serviceId, commandId, userInfo.userId(), code, slotMachineConfig.prefixService(), currencyType.name());
//        } else {
//            if (!SlotGameConstant.BOT_TYPE.equals(userInfo.userType())
//                    && !SlotGameConstant.NBOT_TYPE.equals(userInfo.userType()) && promotionService.hasPromotion()) {
//                promotion = promotionService.checkUserHasPromotion(serviceId, commandId, userInfo.userId(), slotMachineConfig.prefixService(), currencyType.name());
//                if (promotion != null && (!promotion.isValid() || promotion.getRemain() == 0)) {
//                    promotionRepository.remove(serviceId, userInfo.userId(), currencyType.name());
//                }
//            }
//        }
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
                    throw new SlotGameException(SlotGameError.UNEXPECTED, userInfo.userId(), userInfo.userType(), commandId);
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
            errorCodeList.add(SlotGameError.ERROR_USER_IN_PROGRESS.getErrorCode());
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

//    private List<String> updateExtraDataWithEvent(String commandId, UserInfo userInfo, List<String> eDataList) {
//        Optional<Quest> questOptional = loadUserEvent(commandId, userInfo, slotMachineConfig);
//        if (questOptional.isPresent()) {
//            eDataList = updateExtraDataWithEventTasks(eDataList, questOptional.get());
//        } else {
//            boolean pushResult = playerViewStoreService.pushError(
//                    slotMachineConfig.serviceId(),
//                    userInfo.userId(),
//                    userInfo.userType(),
//                    commandId,
//                    Arrays.asList(SlotGameError.INVALID_ENVENTID.getErrorCode())
//            );
//
//            LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
//            logBuilder
//                    .cmdId(commandId)
//                    .actorId(userInfo.userId())
//                    .serviceId(slotMachineConfig.serviceId())
//                    .psId("")
//                    .stateName("UserServiceImpl.updateExtraDataWithEvent")
//                    .stepName("pushErrorToPVS")
//                    .owner(LogMessage.OWNER_GAME)
//                    .message(SlotGameError.INVALID_ENVENTID.getErrorCode() + " send to client: " + pushResult)
//                    .timeExe(0);
//            LogsUtils.writeLogError(logBuilder.build());
//        }
//        return eDataList;
//    }

//    private List<String> updateExtraDataWithEventTasks(List<String> eDataList, Quest quest) {
//        List<String> taskInfos = quest.getTasks().stream()
//                .map(task ->
//                        task.getId() + ";" +
//                        task.getName() + ";" +
//                        GameUtils.formatDouble(task.getScore()) + "/" + GameUtils.formatDouble(task.getTargetScore()))
//                .collect(Collectors.toList());
//
//        if (eDataList == null) {
//            eDataList = new ArrayList<>();
//        }
//        eDataList.add(String.format("%s;%s;%s", quest.getEventId(), quest.getQuestId(), quest.getEndTime()));
//        eDataList.addAll(taskInfos);
//        return eDataList;
//    }

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
    public void saveGamblePlaySession(BasePlaySession basePlaySession) {
        playSessionRepository.saveGamble(basePlaySession);
    }

    @Override
    public void removeGamblePlaySession(BasePlaySession basePlaySession) {
        playSessionRepository.removeGamblePlaySession(basePlaySession);
    }

    @Override
    public BasePlaySession getGamblePlaySession(String serviceId, UserInfo userInfo) {
        return playSessionRepository.getGamble(userInfo.userId(), serviceId, userInfo.currency());
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

    private Promotion buildPromotionModel(PromotionData value, String userId) {
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
            result.setNotifyStatus(SlotGameConstant.PROMOTION_NOTIFIED_STATUS);
            if (StringUtils.isEmpty(value.getCurrency())) {
                ISlotMachineConfig normalConfig = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
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
/*
    private Optional<Quest> loadUserEvent(String commandId, UserInfo userInfo, ISlotMachineConfig slotMachineConfig) {
        long startTime = Instant.now().toEpochMilli();
        try {
            Optional<Quest> questOptional = Optional.empty();
            // FE is required to send eventId to load event
            if (!StringUtils.isEmpty(userInfo.eventId())) {
                questOptional = userEventManager.loadUserEvent(userInfo.userId(), commandId, userInfo.eventId());
            }
            LogsUtils.writeLogInfo(LogMessage.builder()
                    .cmdId(commandId)
                    .serviceId(slotMachineConfig.serviceId())
                    .actorId(userInfo.userId())
                    .stateName("UserServiceImpl.joinGame")
                    .stepName("loadUserEvent")
                    .message("Quest: " + (questOptional.isPresent() ? JsonParseUtils.parseToJson(questOptional.get()).replace("\"", "'") : "empty"))
                    .timeExe(Instant.now().toEpochMilli() - startTime)
                    .build());
            return questOptional;
        } catch (Exception e) {
            LogsUtils.writeLogWarn(LogMessage.builder()
                    .cmdId(commandId)
                    .serviceId(slotMachineConfig.serviceId())
                    .actorId(userInfo.userId())
                    .stateName("SlotGameServiceImpl.joinGame")
                    .stepName("loadUserEventAsync")
                    .message("Failed to load user event but game still continues, error: " + e.getMessage())
                    .timeExe(Instant.now().toEpochMilli() - startTime)
                    .build());
        }
        return Optional.empty();
    }
*/
}
