package com.io.begstd.dice.services.external.impl;

import com.google.common.collect.Maps;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.GameState;
import com.io.begstd.dice.model.app.Promotion;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.projection.IPlaySessionProjection;
import com.io.begstd.dice.services.external.IPlayerViewStoreClient;
import com.io.begstd.dice.services.external.PlayerViewStoreService;
import com.io.begstd.dice.utils.StructUtil;
import com.io.begstd.dice.common.DiceGameConstant;
import com.io.begstd.dice.common.DiceGameError;
import com.io.begstd.dice.config.ExternalServiceEndPointConfiguration;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceAddUserToGroupRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceChannelType;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceDirrectlyStatePush;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceError;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceErrorRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceJackpotSubscriptionRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceMessage;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceMessageRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceRemoveUserFromGroupRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceResponseStatus;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateClearRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateInfo;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateMetadata;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateRegisterRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateUpdateRequest;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
public class PlayerViewStoreServiceImpl implements PlayerViewStoreService {
//    public final static String BOT_USER_TYPE = "BOT";

    @Autowired
    private ExternalServiceEndPointConfiguration externalServiceEndPointConfiguration;

    @Autowired
    private IPlayerViewStoreClient playerViewStoreClient;

    @Autowired
    private IPlaySessionProjection playSessionProjection;

//    @Autowired
//    private ApplicationContext appContext;

    @Override
    public boolean addUserToGroup(String commandId, String userId, String userType, String groupId, String serviceId,
                                  String stateType, Map<String, Money> JACKPOTS, String userName, Money currentAmount,
                                  Promotion promotion, BasePlaySession basePlaySession, List<String> betIdList, List<String> exBetIdList,
                                  List<String> errorList) {

        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(commandId)
                .actorId(userId)
                .serviceId(serviceId)
                .psId("")
                .stateName("PlayerViewStoreServiceImpl")
                .owner(LogMessage.OWNER_PVS)
                .stepName("addUserToGroup");

        if (basePlaySession != null && DiceGameConstant.BOT_TYPE.equalsIgnoreCase(basePlaySession.userType())) {
            logBuilder.message("It is a BOT").timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogDebug(logBuilder.build());
            return true;
        } else if (basePlaySession == null) {
//            UserJoinGame userInfo = redisJoinGameRepository.get(serviceId, userId);
//            if(userInfo != null && BOT_USER_TYPE.equals(userInfo.getUserType())) {
            if (DiceGameConstant.BOT_TYPE.equalsIgnoreCase(userType)) {
                logBuilder.message("It is a BOT").timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogDebug(logBuilder.build());
                return true;
            }
        }
        try {
            DiceAddUserToGroupRequest.Builder builder = DiceAddUserToGroupRequest.newBuilder();
            builder.setUserId(userId);
            builder.setGroupId(groupId);
            builder.setServiceId(serviceId);
            builder.setCommandId(commandId);
            for (Map.Entry<String, Money> entry : JACKPOTS.entrySet()) {
                DiceStateMetadata.Builder stateBuilder = DiceStateMetadata.newBuilder();
                stateBuilder.setStateType(stateType);
                stateBuilder.setObjectId(entry.getKey());
                stateBuilder.setCommandId(commandId);
                builder.addStateMetadatas(stateBuilder.build());
            }

            // add more item for new network
            if (basePlaySession != null) {
                DiceStateMetadata.Builder stateBuilder = DiceStateMetadata.newBuilder();
                stateBuilder.setStateType(serviceId);
                stateBuilder.setObjectId(userId);
                stateBuilder.setCommandId(commandId);
                builder.addStateMetadatas(stateBuilder.build());
            }
            //end

            if (promotion != null) {
                builder.setExtendData(buildPromotionMetaData(promotion.getStatus(),
                        promotion.getBetId(), promotion.getRemain(), promotion.getTotal(), promotion.getCode()));
            }
            //dynamic betId list based on env
            if ((betIdList != null) && (betIdList.size() > 0)) {
                builder.setExtendData(buildMetaDataBetEnv(builder.build().getExtendData(), betIdList));
            }

            //dynamic betId list
            if ((exBetIdList != null) && (exBetIdList.size() > 0)) {
                builder.setExtendData(buildMetaDataExtraBetEnv(builder.build().getExtendData(), exBetIdList));
            }
            if ((JACKPOTS != null) && (JACKPOTS.size() > 0)) {
                buildFixedJackpot(builder, JACKPOTS);
            }
            if ((errorList != null) && (errorList.size() > 0)) {
                builder.setExtendData(buildMetaDataError(builder.build().getExtendData(), errorList));
            }



            logBuilder.message("Input: " + builder.build())
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogDebug(logBuilder.build());

            DiceResponseStatus result = playerViewStoreClient.addUserToGroup(builder.build());

            logBuilder.message("Passed - " + result.getCode())
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());

            return result.getCode().equals("0");
        } catch (StatusRuntimeException e) {
//            log.error("ERROR --Add UserToGroup {} - error {}", userId, e);

            logBuilder.stepName("addUserToGroup-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        } catch (Exception e) {
//            log.error("ERROR --Add UserToGroup {} - error {}", userId, e);

            logBuilder.stepName("addUserToGroup-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        }
    }



    @Override
    public boolean addUserToGroupWithExtraData(String commandId, String userId, String userType, String groupId, String serviceId,
                                               String stateType, Map<String, Money> JACKPOTS, String userName, Money currentAmount,
                                               Promotion promotion, BasePlaySession basePlaySession, List<String> betIdList,
                                               List<String> exBetIdList, List<String> errorList,
                                               List<String> exDataList, List<String> exCommonData) {

        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(commandId)
                .actorId(userId)
                .serviceId(serviceId)
                .psId("")
                .stateName("PlayerViewStoreServiceImpl")
                .owner(LogMessage.OWNER_PVS)
                .stepName("addUserToGroupWithExtraData");

        if (basePlaySession != null && DiceGameConstant.BOT_TYPE.equalsIgnoreCase(basePlaySession.userType())) {
            logBuilder.message("It is a BOT").timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogDebug(logBuilder.build());
            return true;
        } else if (basePlaySession == null) {
            if (DiceGameConstant.BOT_TYPE.equalsIgnoreCase(userType)) {
                logBuilder.message("It is a BOT").timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
                LogsUtils.writeLogDebug(logBuilder.build());
                return true;
            }
        }
        try {
            DiceAddUserToGroupRequest.Builder builder = DiceAddUserToGroupRequest.newBuilder();
            builder.setUserId(userId);
            builder.setGroupId(groupId);
            builder.setServiceId(serviceId);
            builder.setCommandId(commandId);
            for (Map.Entry<String, Money> entry : JACKPOTS.entrySet()) {
                DiceStateMetadata.Builder stateBuilder = DiceStateMetadata.newBuilder();
                stateBuilder.setStateType(stateType);
                stateBuilder.setObjectId(entry.getKey());
                stateBuilder.setCommandId(commandId);
                builder.addStateMetadatas(stateBuilder.build());
            }

            // add more item for new network
            if (basePlaySession != null) {
                DiceStateMetadata.Builder stateBuilder = DiceStateMetadata.newBuilder();
                stateBuilder.setStateType(serviceId);
                stateBuilder.setObjectId(userId);
                stateBuilder.setCommandId(commandId);
                builder.addStateMetadatas(stateBuilder.build());
            }
            //end

            if (promotion != null) {
                builder.setExtendData(buildPromotionMetaData(promotion.getStatus(),
                        promotion.getBetId(), promotion.getRemain(), promotion.getTotal(), promotion.getCode()));
            }
            //removed - don't use from FE
//            builder.setExtendData(buildUserMetaData(builder.build().getExtendData(), userName, currentAmount));
            //dynamic betId list based on env
            if ((betIdList != null) && (betIdList.size() > 0)) {
                builder.setExtendData(buildMetaDataBetEnv(builder.build().getExtendData(), betIdList));
            }

            //dynamic betId list
            if ((exBetIdList != null) && (exBetIdList.size() > 0)) {
                builder.setExtendData(buildMetaDataExtraBetEnv(builder.build().getExtendData(), exBetIdList));
            }
            if ((JACKPOTS != null) && (JACKPOTS.size() > 0)) {
                buildFixedJackpot(builder, JACKPOTS);
            }
            //error code in join game - 3.0.9
            if ((errorList != null) && (errorList.size() > 0)) {
                builder.setExtendData(buildMetaDataError(builder.build().getExtendData(), errorList));
            }

            //dynamic extra Data list - 3.0.9
            if ((exDataList != null) && (exDataList.size() > 0)) {
                builder.setExtendData(buildMetaDataExtraDataEnv(builder.build().getExtendData(), exDataList));
            }

            // extra data common for game - currency
            if ((exCommonData != null) && (exCommonData.size() > 0)) {
                builder.setExtendData(buildMetaDataExtraCommonDataEnv(builder.build().getExtendData(), exCommonData));
            }

            logBuilder.message("Input: " + builder.build())
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogDebug(logBuilder.build());

            DiceResponseStatus result = playerViewStoreClient.addUserToGroup(builder.build());

            logBuilder.message("Passed - " + result.getCode())
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());

            return result.getCode().equals("0");
        } catch (StatusRuntimeException e) {
//            log.error("ERROR --Add UserToGroup {} - error {}", userId, e);

            logBuilder.stepName("addUserToGroup-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        } catch (Exception e) {
//            log.error("ERROR --Add UserToGroup {} - error {}", userId, e);

            logBuilder.stepName("addUserToGroup-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        }
    }

    public boolean removeUserFromGroup(String commandId, String userId, String userType, String groupId, String serviceId) {

        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(commandId)
                .actorId(userId)
                .serviceId(serviceId)
                .psId("")
                .stateName("PlayerViewStoreServiceImpl")
                .owner(LogMessage.OWNER_PVS)
                .stepName("removeUserFromGroup");


//        UserJoinGame userInfo = redisJoinGameRepository.get(serviceId, userId);
//        if(userInfo != null && DiceGameConstant.BOT_TYPE.equals(userInfo.getUserType())) {
        if (DiceGameConstant.BOT_TYPE.equalsIgnoreCase(userType)) {
            logBuilder.message("It is a BOT").timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogDebug(logBuilder.build());
            return true;
        }
        try {
            DiceResponseStatus result = playerViewStoreClient.removeUserFromGroup(
                    DiceRemoveUserFromGroupRequest.newBuilder()
                            .setUserId(userId)
                            .setGroupId(groupId)
                            .setServiceId(serviceId)
                            .setCommandId(commandId).build());
            logBuilder.message("Passed - Status response " + result.getCode())
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());

            return result.getCode().equals("0");
        } catch (StatusRuntimeException e) {
            logBuilder.stepName("removeUserFromGroup-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        } catch (Exception e) {
            logBuilder.stepName("removeUserFromGroup-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        }
    }

    public boolean registerState(String serviceId, int intevalGame) {
        //log
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId("")
                .actorId("")
                .serviceId(serviceId)
                .psId("")
                .stateName("PlayerViewStoreServiceImpl")
                .owner(LogMessage.OWNER_PVS)
                .stepName("registerState");

        try {
            DiceStateRegisterRequest.Builder builder = DiceStateRegisterRequest.newBuilder();
            builder.setServiceId(serviceId);

            DiceStateInfo.Builder stateBuilder = DiceStateInfo.newBuilder();

            stateBuilder.setStateType(serviceId);
            stateBuilder.setChannelType(DiceChannelType.DICE_PRIVATE);
            stateBuilder.setInterval(intevalGame);
            stateBuilder.setMaxRetrySending(externalServiceEndPointConfiguration.getMaxRetrySending());
            builder.addStateInfos(stateBuilder);
//              log.info("registerState:" + builder.build());
            DiceResponseStatus result = playerViewStoreClient.registerState(builder.build());

            logBuilder.message("Passed - Status response " + result.getCode())
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());

            return result.getCode().equals("0");
        } catch (StatusRuntimeException e) {
//              log.error("ERROR --registerState {} - error {}", serviceId, e);
            logBuilder.stepName("registerState-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        } catch (Exception e) {
//              log.error("ERROR --registerState {} - error {}", serviceId, e);
            logBuilder.stepName("registerState-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        }
    }

    public boolean updateState(BasePlaySession basePlaySession) {
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(basePlaySession.commandId())
                .actorId(basePlaySession.userId())
                .serviceId(basePlaySession.serviceId())
                .psId(basePlaySession.uuid())
                .stateName("PlayerViewStoreServiceImpl")
                .owner(LogMessage.OWNER_PVS)
                .stepName("updateState");

        if (basePlaySession != null && DiceGameConstant.BOT_TYPE.equals(basePlaySession.userType())) {
//            log.info("WARN --updateState {} -- serviceId {} for bot", playSession.userId(), playSession.serviceId());
            logBuilder.message("It is a BOT").timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogDebug(logBuilder.build());
            return true;
        }
        try {
//            log.debug("Update PlaySession start serviceId={} -- objectId={} -- state={}", playSession.serviceId(), JsonParseUtils
//            .parseToJson(playSession));
            DiceStateUpdateRequest.Builder builder = DiceStateUpdateRequest.newBuilder();

            builder.setServiceId(String.valueOf(basePlaySession.serviceId()));
            builder.setStateType(String.valueOf(basePlaySession.serviceId()));
            builder.setObjectId(basePlaySession.userId());
            Object viewerObj = null;
            viewerObj = playSessionProjection.convertToSmallViewModel(basePlaySession);
//            log.debug("Update PlaySession viewerObj: {}", objectMapper.writeValueAsString(viewerObj));

            Struct state = StructUtil.convertToStruct(viewerObj);
//            log.debug("Update PlaySession viewerObj: {}", state.toString());
//            log.info("User Update: "+playSession.userId());

            builder.setState(state);
            builder.setEventName(getEventName(basePlaySession));

            logBuilder.message(viewerObj)
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);

            LogsUtils.writeLogDebug(logBuilder.build());

            DiceResponseStatus result = playerViewStoreClient.updateState(builder.build());

            logBuilder.message("Passed - " + result.getCode()).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());

            return result.getCode().equals("0");
        } catch (StatusRuntimeException e) {
            logBuilder.stepName("updateState-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
        } catch (Exception e) {
            logBuilder.stepName("updateState-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
        }
        return false;
    }

    protected String getEventName(BasePlaySession basePlaySession) {
        switch (basePlaySession.state()) {
            case GameState.NORMAL_GAME:
                return "n";
        }
        // unknown
        return "u";
    }

    public boolean updateLatestState(BasePlaySession basePlaySession, int isFull) {
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(basePlaySession.commandId())
                .actorId(basePlaySession.userId())
                .serviceId(basePlaySession.serviceId())
                .psId(basePlaySession.uuid())
                .stateName("PlayerViewStoreServiceImpl")
                .owner(LogMessage.OWNER_PVS)
                .stepName("updateLatestState");

        if (basePlaySession != null && DiceGameConstant.BOT_TYPE.equalsIgnoreCase(basePlaySession.userType())) {
//            log.info("WARN --updateLatestState {} -- serviceId {} for bot", playSession.userId(), playSession.serviceId());
            logBuilder.message("It is a BOT").timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogDebug(logBuilder.build());
            return true;
        }
        try {
//            log.debug("UpdateLatestState PlaySession start serviceId={} -- objectId={} -- state={}", playSession.serviceId(),
//            JsonParseUtils.parseToJson(playSession));
            DiceStateUpdateRequest.Builder builder = DiceStateUpdateRequest.newBuilder();

            builder.setServiceId(String.valueOf(basePlaySession.serviceId()));
            builder.setStateType(String.valueOf(basePlaySession.serviceId()));
            builder.setObjectId(basePlaySession.userId());

            Object viewerObj = null;
            if (isFull == 1) {
                viewerObj = playSessionProjection.convertToFullViewModel(basePlaySession);
            } else {
                viewerObj = playSessionProjection.convertToSmallViewModel(basePlaySession);
            }
//            log.debug("UpdateLatestState PlaySession viewerObj: {}", objectMapper.writeValueAsString(viewerObj));

            Struct state = StructUtil.convertToStruct(viewerObj);

            builder.setState(state);

            logBuilder.message(viewerObj).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogDebug(logBuilder.build());

            DiceResponseStatus result = playerViewStoreClient.updateLatestState(builder.build());
//            log.info("UpdateLatestState PlaySession status response {}", result.getCode());
            logBuilder.message("Passed - " + result.getCode()).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());

            return result.getCode().equals("0");
        } catch (StatusRuntimeException e) {
//            log.error("ERROR --UpdateLatestState updatePlaySession {} - error {}", playSession, e);
            logBuilder.stepName("updateState-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
        } catch (Exception e) {
//            log.error("ERROR --UpdateLatestState updatePlaySession {} - error {}", playSession, e);
            logBuilder.stepName("updateState-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
        }
        return false;
    }

    public boolean subscribeJackpot(String serviceId, List<String> jackPotList) {
        //log
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId("")
                .actorId("")
                .serviceId(serviceId)
                .psId("")
                .stateName("PlayerViewStoreServiceImpl")
                .owner(LogMessage.OWNER_PVS)
                .stepName("subscribeJackpot");

        try {
            DiceJackpotSubscriptionRequest.Builder requestBuilder = DiceJackpotSubscriptionRequest.newBuilder();
            requestBuilder.setServiceId(serviceId);
            for (String item : jackPotList) {
                requestBuilder.addJackpotIds(item);
            }
            DiceResponseStatus result = playerViewStoreClient.subscribeJackpot(requestBuilder.build());
//            log.info("SubscribeJackpot status response {}", result.getCode());
            logBuilder.message("Passed - " + result.getCode() + ". Jackpotlist: " + jackPotList).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());
            return result.getCode().equals("0");
        } catch (StatusRuntimeException e) {
//            log.error("ERROR --Create GroupChannel {} - error {}", serviceId, e);
            logBuilder.stepName("updateState-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        }
    }

    @Override
    public boolean pushError(String serviceId, String userId, String userType, String commandId, List<String> errorList) {
        //log
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(commandId)
                .actorId(userId)
                .serviceId(serviceId)
                .psId("")
                .stateName("PlayerViewStoreServiceImpl")
                .owner(LogMessage.OWNER_PVS)
                .stepName("pushError");

//        UserJoinGame userInfo = redisJoinGameRepository.get(serviceId, userId);
//        if(userInfo != null && DiceGameConstant.BOT_TYPE.equals(userInfo.getUserType())) {
        if (DiceGameConstant.BOT_TYPE.equalsIgnoreCase(userType)) {
//            log.info("WARN --pushError {} -- serviceId {} for bot", userId, serviceId);
            logBuilder.message("It is a BOT").timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogDebug(logBuilder.build());
            return true;
        }
        try {
            DiceErrorRequest.Builder requestBuilder = DiceErrorRequest.newBuilder();
            requestBuilder.setServiceId(serviceId);
            requestBuilder.setChannelType(DiceChannelType.DICE_PRIVATE);
            requestBuilder.setObjectId(userId);

            for (String errorItem : errorList) {
                DiceError.Builder errorBuilder = DiceError.newBuilder();
                errorBuilder.setCommandId(commandId);
                errorBuilder.setCode(errorItem);
                requestBuilder.addErrors(errorBuilder.build());
            }

            DiceResponseStatus result = playerViewStoreClient.pushError(requestBuilder.build());
            logBuilder.message("Passed - " + result.getCode() + ". ErrorCode:" + errorList).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());

            return result.getCode().equals("0");
        } catch (StatusRuntimeException e) {
            logBuilder.stepName("pushError-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        }
    }

    @Override
    public boolean pushErrorHasMeta(String serviceId, String userId, String userType,
                                    String commandId, List<String> errorList, String metaData) {
        //log
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(commandId)
                .actorId(userId)
                .serviceId(serviceId)
                .psId("")
                .stateName("PlayerViewStoreServiceImpl")
                .owner(LogMessage.OWNER_PVS)
                .stepName("pushErrorHasMeta");

        if (DiceGameConstant.BOT_TYPE.equalsIgnoreCase(userType)) {
            logBuilder.message("It is a BOT").timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogDebug(logBuilder.build());
            return true;
        }
        try {
            DiceErrorRequest.Builder requestBuilder = DiceErrorRequest.newBuilder();
            requestBuilder.setServiceId(serviceId);
            requestBuilder.setChannelType(DiceChannelType.DICE_PRIVATE);
            requestBuilder.setObjectId(userId);

            Value.Builder vb0016 = Value.newBuilder();
            Map<String, Value> values0016 = Maps.newHashMap();
            values0016.put("promotion", vb0016.setStringValue(metaData).build());

            for (String errorItem : errorList) {
                DiceError.Builder errorBuilder = DiceError.newBuilder();
                errorBuilder.setCommandId(commandId);
                errorBuilder.setCode(errorItem);
                if (DiceGameError.ERROR_USER_DEFFIRENT_BET_MODE_AWARD.getErrorCode().equals(errorItem)
                        || DiceGameError.PROMOTION_NEW.getErrorCode().equals(errorItem)
                        || DiceGameError.PROMOTION_RESET.getErrorCode().equals(errorItem)) {
                    errorBuilder.setMetaData(Struct.newBuilder().putAllFields(values0016).build());
                }
                requestBuilder.addErrors(errorBuilder.build());
            }

            DiceResponseStatus result = playerViewStoreClient.pushError(requestBuilder.build());
            logBuilder.message("Passed - " + result.getCode() + ". Input:" + requestBuilder.build()).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());

            return result.getCode().equals("0");
        } catch (StatusRuntimeException e) {
            logBuilder.stepName("pushError-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        }
    }

    @Override
    public boolean clearState(String serviceId, String userId) {
        //log
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId("")
                .actorId(userId)
                .serviceId(serviceId)
                .psId("")
                .stateName("PlayerViewStoreServiceImpl")
                .owner(LogMessage.OWNER_PVS)
                .stepName("clearState");

//        UserJoinGame userInfo = redisJoinGameRepository.get(serviceId, userId);
//        if(userInfo != null && DiceGameConstant.BOT_TYPE.equals(userInfo.getUserType())) {
////            log.info("WARN --clearState {} -- serviceId {} for bot", userId, serviceId);
//            logBuilder.message("It is a BOT").timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
//            LogsUtils.writeLogDebug(logBuilder.build());
//            return true;
//        }
        try {
//            log.info("Clear PlaySession start serviceId={} -- objectId={} -- userId={}", serviceId, userId);
            DiceStateClearRequest.Builder builder = DiceStateClearRequest.newBuilder();

            builder.setServiceId(String.valueOf(serviceId));
            builder.setStateType(serviceId);
            builder.setObjectId(userId);

            DiceResponseStatus result = playerViewStoreClient.clearState(builder.build());
//            log.info("Clear PlaySession status response {}", result.getCode());
            logBuilder.message("Passed - Clear playsession " + result.getCode()).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());
            return result.getCode().equals("0");
        } catch (StatusRuntimeException e) {
//            log.error("ERROR --clearState {} - error {}", userId, e);
            logBuilder.stepName("clearState-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
        } catch (Exception e) {
//            log.error("ERROR --clearState {} - error {}", userId, e);
            logBuilder.stepName("clearState-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
        }
        return false;
    }

    @Override
    public boolean pushMessage(String serviceId, String userId, String userType, String commandId, String errorCode) {
        //log
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId("")
                .actorId(userId)
                .serviceId(serviceId)
                .psId("")
                .stateName("PlayerViewStoreServiceImpl")
                .owner(LogMessage.OWNER_PVS)
                .stepName("pushMessage");

//        UserJoinGame userInfo = redisJoinGameRepository.get(serviceId, userId);
//        if(userInfo != null && DiceGameConstant.BOT_TYPE.equals(userInfo.getUserType())) {
        if (DiceGameConstant.BOT_TYPE.equalsIgnoreCase(userType)) {
//            log.info("WARN --pushError {} -- serviceId {} for bot", userId, serviceId);
            logBuilder.message("It is a BOT").timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());
            return true;
        }
        try {
            DiceMessageRequest.Builder requestBuilder = DiceMessageRequest.newBuilder();
            requestBuilder.setServiceId(serviceId);
            requestBuilder.setChannelTypeValue(DiceChannelType.DICE_PRIVATE_VALUE);
            requestBuilder.setObjectId(userId);

            DiceMessage.Builder errorBuilder = DiceMessage.newBuilder();
            errorBuilder.setCode(errorCode);
            errorBuilder.setMetaData(buildMetaData(serviceId, userId, commandId));

            requestBuilder.setMessage(errorBuilder.build());
//            log.debug("pushMessage  userId{}  -- commandId={} --- requestBuilder.build()={}", userId, commandId, requestBuilder.build());
            logBuilder.message("DataInput - " + requestBuilder.build()).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogDebug(logBuilder.build());

            DiceResponseStatus result = playerViewStoreClient.pushMessage(requestBuilder.build());
            logBuilder.message("Passed - " + result.getCode()).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogInfo(logBuilder.build());
//            log.info("pushMessage status response {}", result.getCode());
            return result.getCode().equals("0");
        } catch (StatusRuntimeException e) {
//            log.error("ERROR -- pushMessage {} errorCode {} - error {}", serviceId, errorCode, e);
            logBuilder.stepName("pushMessage-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        } catch (Exception e) {
//            log.error("ERROR --Exception pushMessage {} errorCode {} - error {}", serviceId, errorCode, e);
            logBuilder.stepName("pushMessage-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        }
    }

//    @Override
//    public boolean pushMessagePromotion(String serviceId, String userId, String commandId, String errorCode, String betId, int
//    promotionRemain, int promotionTotal, String promotionCode) {
//        try {
//            MessageRequest.Builder requestBuilder = MessageRequest.newBuilder();
//            requestBuilder.setServiceId(serviceId);
//            requestBuilder.setChannelTypeValue(ChannelType.PRIVATE_VALUE);
//            requestBuilder.setObjectId(userId);
//
//            Message.Builder errorBuilder = Message.newBuilder();
//            errorBuilder.setCode(errorCode);
//            errorBuilder.setMetaData(buildPromotionMetaData(serviceId, userId, commandId, betId, promotionRemain, promotionTotal,
//            promotionCode));
//
//            requestBuilder.setMessage(errorBuilder.build());
//            log.info("pushMessagePromotion  userId{}  -- commandId={} --- requestBuilder.build()={}", userId, commandId, requestBuilder
//            .build());
//            ResponseStatus result = intial().pushMessage(requestBuilder.build());
//            log.info("pushMessagePromotion status response {}", result.getCode());
//            return result.getCode().equals("0");
//        } catch (StatusRuntimeException e) {
//            log.error("ERROR -- pushMessagePromotion {} errorCode {} - error {}", serviceId, errorCode, e);
//            shutDown();
//            return false;
//        } catch (Exception e) {
//            log.error("ERROR --Exception pushMessagePromotion {} errorCode {} - error {}", serviceId, errorCode, e);
//            shutDown();
//            return false;
//        }
//    }

    private Struct buildMetaData(String serviceId, String userId, String commandId) {

        Value.Builder vb = Value.newBuilder();
        Map<String, Value> values = Maps.newHashMap();
        values.put("serviceId", vb.setStringValue(serviceId).build());
        values.put("objectId", vb.setStringValue(userId).build());
        values.put("commandId", vb.setStringValue(commandId).build());
        values.put("stateType", vb.setStringValue(serviceId).build());
        return Struct.newBuilder().putAllFields(values).build();
    }

    private Struct buildPromotionMetaData(int status,
                                          String betId, int promotionRemain, int promotionTotal, String promotionCode) {

        Value.Builder vb = Value.newBuilder();
        Map<String, Value> values = Maps.newHashMap();
        Map<String, Value> valuesRoot = Maps.newHashMap();
        if (status == 0) {
            values.put("bId", vb.setStringValue(betId).build());
            values.put("pRe", vb.setNumberValue(promotionRemain).build());
            values.put("pTal", vb.setNumberValue(promotionTotal).build());
            values.put("pCd", vb.setStringValue(promotionCode).build());
        }
//        values.put("status", vb.setNumberValue(status).build());
        // metaDataPromotion = mDP
        valuesRoot.put("mDP", vb.setStructValue(Struct.newBuilder().putAllFields(values).build()).build());
        return Struct.newBuilder().putAllFields(valuesRoot).build();
    }

    private Struct buildMetaDataBetEnv(Struct extenData, List<String> betList) {
        Struct.Builder builder = Struct.newBuilder();
        if (extenData != null) {
            builder = extenData.toBuilder();
        }
        Value.Builder vb = Value.newBuilder();
//        Map<String, Value> values = Maps.newHashMap();
        Map<String, Value> valuesRoot = Maps.newHashMap();
        if ((betList != null) && (betList.size() > 0)) {
            String buff = "";
            for (String item : betList) {
                buff += item + ",";
            }
            buff = buff.substring(0, buff.length() - 1);
            // mBet
            valuesRoot.put("mb", vb.setStringValue(buff).build());
        }
//        valuesRoot.put("metaDataBetEnv", vb.setStructValue(Struct.newBuilder().putAllFields(values).build()).build());
        return builder.putAllFields(valuesRoot).build();
    }

    private Struct buildMetaDataExtraBetEnv(Struct extenData, List<String> exBetList) {
        Struct.Builder builder = Struct.newBuilder();
        if (extenData != null) {
            builder = extenData.toBuilder();
        }
        Value.Builder vb = Value.newBuilder();
//        Map<String, Value> values = Maps.newHashMap();
        Map<String, Value> valuesRoot = Maps.newHashMap();
        if ((exBetList != null) && (exBetList.size() > 0)) {
            String buff = "";
            for (String item : exBetList) {
                buff += item + ",";
            }
            buff = buff.substring(0, buff.length() - 1);
            // eBet
            valuesRoot.put("eb", vb.setStringValue(buff).build());
        }
//        valuesRoot.put("metaDataBetEnv", vb.setStructValue(Struct.newBuilder().putAllFields(values).build()).build());
        return builder.putAllFields(valuesRoot).build();
    }


    protected void buildFixedJackpot(DiceAddUserToGroupRequest.Builder root, Map<String, Money> JACKPOTS){

    }



    private Struct buildMetaDataError(Struct extenData, List<String> errorList) {
        Struct.Builder builder = Struct.newBuilder();
        if (extenData != null) {
            builder = extenData.toBuilder();
        }
        Value.Builder vb = Value.newBuilder();
//        Map<String, Value> values = Maps.newHashMap();
        Map<String, Value> valuesRoot = Maps.newHashMap();
        if ((errorList != null) && (errorList.size() > 0)) {
            String buff = "";
            for (String item : errorList) {
                buff += item + ",";
            }
            buff = buff.substring(0, buff.length() - 1);
            valuesRoot.put("err", vb.setStringValue(buff).build());
        }
//        valuesRoot.put("metaDataBetEnv", vb.setStructValue(Struct.newBuilder().putAllFields(values).build()).build());
        return builder.putAllFields(valuesRoot).build();
    }

    private Struct buildMetaDataExtraDataEnv(Struct extenData, List<String> exDataList) {
        Struct.Builder builder = Struct.newBuilder();
        if (extenData != null) {
            builder = extenData.toBuilder();
        }
        Value.Builder vb = Value.newBuilder();
//        Map<String, Value> values = Maps.newHashMap();
        Map<String, Value> valuesRoot = Maps.newHashMap();
        if ((exDataList != null) && (exDataList.size() > 0)) {
            String buff = "";
            for (String item : exDataList) {
                buff += item + ",";
            }
            buff = buff.substring(0, buff.length() - 1);
            // eData
            valuesRoot.put("ed", vb.setStringValue(buff).build());
        }
//        valuesRoot.put("metaDataBetEnv", vb.setStructValue(Struct.newBuilder().putAllFields(values).build()).build());
        return builder.putAllFields(valuesRoot).build();
    }

    private Struct buildMetaDataExtraCommonDataEnv(Struct extenData, List<String> exCommonDataList) {
        Struct.Builder builder = Struct.newBuilder();
        if (extenData != null) {
            builder = extenData.toBuilder();
        }
        Value.Builder vb = Value.newBuilder();
//        Map<String, Value> values = Maps.newHashMap();
        Map<String, Value> valuesRoot = Maps.newHashMap();
        if ((exCommonDataList != null) && (exCommonDataList.size() > 0)) {
            String buff = "";
            for (String item : exCommonDataList) {
                buff += item + "#";
            }
            buff = buff.substring(0, buff.length() - 1);
            valuesRoot.put("ec", vb.setStringValue(buff).build());
        }
//        valuesRoot.put("metaDataBetEnv", vb.setStructValue(Struct.newBuilder().putAllFields(values).build()).build());
        return builder.putAllFields(valuesRoot).build();
    }

    @Override
    public boolean addUserToGroupTrial(String commandId, String userId, String groupId, String serviceId,
                                       String stateType, Map<String, Money> JACKPOTS, String userName, Money currentAmount,
                                       Promotion promotion, BasePlaySession basePlaySession, List<String> betIdList) {

        try {
            DiceAddUserToGroupRequest.Builder builder = DiceAddUserToGroupRequest.newBuilder();
            builder.setUserId(userId);
            builder.setGroupId(groupId);
            builder.setServiceId(serviceId);
            builder.setCommandId(commandId);

            DiceResponseStatus result = playerViewStoreClient.addUserToGroupTrial(builder.build());
            log.debug("Add UserToGroup Trial-- build :" + builder.build());
            log.info("Add UserToGroup Trial status response {}", result.getCode());
            return result.getCode().equals("0");
        } catch (StatusRuntimeException e) {
            log.error("ERROR --Add UserToGroup Trial {} - error {}", userId, e);
            playerViewStoreClient.shutDown();
            return false;
        } catch (Exception e) {
            log.error("ERROR --Add UserToGroup Trial {} - error {}", userId, e);
            playerViewStoreClient.shutDown();
            return false;
        }
    }

    @Override
    public boolean pushState(String serviceId, String userId, String userType, String commandId) {
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId("")
                .actorId(userId)
                .serviceId(serviceId)
                .psId("")
                .stateName("PlayerViewStoreServiceImpl")
                .owner(LogMessage.OWNER_PVS)
                .stepName("pushState");

        if (DiceGameConstant.BOT_TYPE.equalsIgnoreCase(userType)) {
            logBuilder.message("It is a BOT").timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogDebug(logBuilder.build());
            return true;
        }
        try {
            DiceStateRequest.Builder requestBuilder = DiceStateRequest.newBuilder();
            requestBuilder.setServiceId(serviceId);
            requestBuilder.setCommandId(commandId);
            requestBuilder.setStateType(serviceId);
            requestBuilder.setObjectId(userId);
            requestBuilder.setUserId(userId);

            DiceResponseStatus result = playerViewStoreClient.pushState(requestBuilder.build());
            logBuilder.message("Passed - " + requestBuilder.build().toString() + ".\n Result: " + result.getCode()).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogDebug(logBuilder.build());
//            log.info("pushMessage status response {}", result.getCode());
            return result.getCode().equals("0");
        } catch (StatusRuntimeException e) {
//            log.error("ERROR -- pushMessage {} errorCode {} - error {}", serviceId, errorCode, e);
            logBuilder.stepName("pushMessage-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        } catch (Exception e) {
//            log.error("ERROR --Exception pushMessage {} errorCode {} - error {}", serviceId, errorCode, e);
            logBuilder.stepName("pushMessage-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        }
    }

    public boolean notifyMessage(String serviceId, UserInfo userInfo, String commandId, String event, DiceDirrectlyStatePush message) {
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId("")
                .actorId(userInfo.userId())
                .serviceId(serviceId)
                .psId("")
                .stateName("PlayerViewStoreServiceImpl.notifyMessage")
                .owner(LogMessage.OWNER_PVS)
                .stepName("pushState");

        if (DiceGameConstant.BOT_TYPE.equalsIgnoreCase(userInfo.userType())) {
            logBuilder.message("It is a BOT").timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogDebug(logBuilder.build());
            return true;
        }
        try {
            DiceDirrectlyStatePush.Builder builder = message.toBuilder();
            builder.setServiceId(serviceId);
            builder.setEvent(event);
            builder.setChannelType(DiceChannelType.DICE_PRIVATE);
            builder.setReceiver(userInfo.userId());

            DiceResponseStatus result = playerViewStoreClient.notifyMessage(builder.build());
            logBuilder.message("message: " + builder.build() + " Passed - " + result.getCode()).timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogDebug(logBuilder.build());
            return result.getCode().equals("0");
        } catch (StatusRuntimeException e) {
//            log.error("ERROR -- pushMessage {} errorCode {} - error {}", serviceId, errorCode, e);
            logBuilder.stepName("notifyMessage-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        } catch (Exception e) {
//            log.error("ERROR --Exception pushMessage {} errorCode {} - error {}", serviceId, errorCode, e);
            logBuilder.stepName("notifyMessage-Exception")
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGobal);
            LogsUtils.writeLogException(logBuilder.build(), e);
            playerViewStoreClient.shutDown();
            return false;
        }
    }
}
