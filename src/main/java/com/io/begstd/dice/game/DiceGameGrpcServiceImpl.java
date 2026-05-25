package com.io.begstd.dice.game;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import com.io.begstd.dice.grpc.dicegame.DiceGetLatestStateRequest;
import com.io.begstd.dice.grpc.dicegame.DiceGroupUsersMaintainRequest;
import com.io.begstd.dice.grpc.promotionservice.DicePromotionData;
import com.io.begstd.extension.loader.ExtensionLoader;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.GroupUserMaintaince;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.model.config.CurrencyType;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.model.wallet.WalletOption;
import com.io.begstd.dice.repository.RedisGroupMaintainRepository;
import com.io.begstd.dice.services.internal.ActivePlayerService;
import com.io.begstd.dice.services.internal.DiceGameService;
import com.io.begstd.dice.utils.ConfigManager;
import com.io.begstd.dice.utils.StructUtil;
import com.io.begstd.dice.command.SpinCmd;
import com.io.begstd.dice.common.DiceGameConstant;
import com.io.begstd.dice.common.DiceGameError;
import com.io.begstd.dice.exception.DiceGameException;
import com.io.begstd.dice.grpc.promotionservice.DicePromotionData;
import com.io.begstd.dice.grpc.dicegame.DiceNormalSpin;
import com.io.begstd.dice.grpc.dicegame.DicePlayerInfo;
import com.io.begstd.dice.grpc.dicegame.DiceResponseStatusSG;
import com.io.begstd.dice.grpc.dicegame.DiceResponseStatusSGBO;
import com.io.begstd.dice.grpc.dicegame.DiceGameServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Slf4j
@GRpcService
public class DiceGameGrpcServiceImpl extends DiceGameServiceGrpc.DiceGameServiceImplBase {
//    public final static String BOT_USER_TYPE = "BOT";
//    public final static String USER_TYPE = "USER";
    private static DiceGameGrpcServiceImpl instance;
    @Autowired
    private DiceGameService slotGameService;

    @Autowired
    private RedisGroupMaintainRepository groupMaintainRepository;

    @Autowired
    private ActivePlayerService activePlayerService;

    @Autowired
    private ExtensionLoader extensionLoader;

    @Autowired
    private ConfigManager configManager;

    private IDiceMachineConfig configNormal;

    @PostConstruct
    public void init() {
        instance = this;
        configNormal = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
    }

    @Override
    public void joinGame(DicePlayerInfo player, StreamObserver<DiceResponseStatusSG> responseObserver) {
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(player.getCId())
                .actorId(player.getUId())
                .serviceId("")
                .psId("")
                .stateName("DiceGameGrpcServiceImpl.joinGame")
                .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
                .message(player)
                .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());

        String userType = "";
        if(DiceGameConstant.BOT_TYPE.equals(player.getUd().getUserType())) {
            userType = DiceGameConstant.NBOT_TYPE;
        } else {
            userType = player.getUd().getUserType();
            if (userType == null) {
                userType = DiceGameConstant.USER_TYPE;
            }
        }

        CurrencyType currency = configNormal.getAvailableCurrency(player.getUd().getCurrency());

        if (currency == null) {
            throw new DiceGameException(DiceGameError.NOT_SUPPORT_CURR, player.getUId(),
                    player.getUd().getUserType(),
                    player.getCId());
        }

        UserInfo userInfo = UserInfo.builder()
                .userId(player.getUId())
                .userType(userType)
                .userAgent(player.getUd().getUserAgent())
                .displayName(player.getUd().getDisplayName())
                .money(player.getUd().getMoney())
                .ip(player.getUd().getIp())
                .avatar(player.getUd().getAvatar())
                .env(player.getEnv())
                .ssid(player.getSsid())
                .currency(currency.name())
                .build();
//        userInfo.eventId(player.getEId());

        DicePromotionData promotionData = player.getPromotionData();
        try {
            if (promotionData == DicePromotionData.getDefaultInstance()) {
                promotionData = null;
            } else {
                log.debug("DicePromotionData: {}", JsonFormat.printer().print(promotionData));
            }
        } catch (InvalidProtocolBufferException e) {
            log.error(e.getMessage(), e);
        }
        int result = slotGameService.joinGame(player.getCId(), userInfo, promotionData, player.getEnv());

        activePlayerService.updateLastModified(userInfo.userId(), userType);

        DiceResponseStatusSG resultRes = DiceResponseStatusSG.newBuilder().setC(String.valueOf(result)).build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
//        log.info("Join successfully");
    }

    @Override
    public void leaveGame(DicePlayerInfo player, StreamObserver<DiceResponseStatusSG> responseObserver) {

        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(player.getCId())
            .actorId(player.getUId())
            .serviceId("")
            .psId("")
            .stateName("DiceGameGrpcServiceImpl.leaveGame")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(player)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());

        UserInfo userInfo = UserInfo.builder()
                .userId(player.getUId())
                .userType(player.getUd().getUserType())
                .userAgent(player.getUd().getUserAgent())
                .displayName(player.getUd().getDisplayName())
                .money(player.getUd().getMoney())
                .ip(player.getUd().getIp())
                .avatar(player.getUd().getAvatar())
                .build();

        int result = slotGameService.leaveGame(player.getCId(), userInfo);
        DiceResponseStatusSG resultRes = DiceResponseStatusSG.newBuilder().setC(String.valueOf(result)).build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
    }

    @Override
    public void normalSpin(DiceNormalSpin request, StreamObserver<DiceResponseStatusSG> responseObserver) {
        //log builder
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("DiceGameGrpcServiceImpl.NormalGame")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        String userType = "";
        if(DiceGameConstant.BOT_TYPE.equals(request.getUd().getUserType())) {
            userType = DiceGameConstant.NBOT_TYPE;
        } else {
            if (request.getUd().getUserType() != null) {
                userType = request.getUd().getUserType();
            } else {
                userType = DiceGameConstant.USER_TYPE;
            }
        }
        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new DiceGameException(DiceGameError.NOT_SUPPORT_CURR, request.getUId(),
                    userType, request.getCId());
        }
        WalletOption optWallet =  WalletOption.parseWalletOption(request.getWo());

     // userBrand
        String userBrand = "";
        if (!StringUtils.isEmpty(request.getUd().getUserBrand())) {
            userBrand = request.getUd().getUserBrand();
        }

//        String prefixArray[] = request.getUId().split("_");
        GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        if (groupMaintain != null && groupMaintain.isMaintain()) {
            throw new DiceGameException(DiceGameError.USER_MAINTAINANCE, request.getUId(),
                request.getUd().getUserType(),
                request.getCId());
        }

        SpinCmd spinCmd = null;
        if (!request.getBId().equals("")) {
            spinCmd = new SpinCmd().totalBetId(request.getBId()).lineIds(request.getBLnList()).currency(currency.name()).lang(request.getL());
        }
        if (spinCmd != null) {
            UserInfo userInfo = UserInfo.builder()
                    .userId(request.getUId())
                    .userType(userType)
                    .userAgent(request.getUd().getUserAgent())
                    .displayName(request.getUd().getDisplayName())
                    .money(request.getUd().getMoney())
                    .ip(request.getUd().getIp())
                    .avatar(request.getUd().getAvatar())
                    .walletOption(WalletOption.MAIN)
                    .env(request.getEnv())
                    .ssid(request.getSsid())
                    .currency(currency.name())
                    .userBrand(userBrand)
                    .build();

            BasePlaySession basePlaySessionResult = slotGameService.normalSpin(request.getCId(), userInfo, spinCmd);

            activePlayerService.updateLastModified(userInfo.userId(), userType);

            int codeResult = basePlaySessionResult == null ? 1 : 0;
            DiceResponseStatusSG.Builder builder = DiceResponseStatusSG.newBuilder();
            builder.setC(String.valueOf(codeResult));
            builder.setSt(buildState(basePlaySessionResult));
            DiceResponseStatusSG resultRes = builder.build();
            responseObserver.onNext(resultRes);
            responseObserver.onCompleted();
//            log.info("After Next normal play {}", codeResult);
        } else {
//            log.info("BettingLine is Zero");
            logBuilder.message("BettingLine is Zero ")
                .timeExe(0);
            LogsUtils.writeLogDebug(logBuilder.build());

            DiceResponseStatusSG resultRes = DiceResponseStatusSG.newBuilder().setC(String.valueOf(1)).build();
            responseObserver.onNext(resultRes);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getLatestState(DiceGetLatestStateRequest request, StreamObserver<DiceResponseStatusSG> responseObserver) {
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("DiceGameGrpcServiceImpl.getLatestState")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());

//        String prefixArray[] = request.getUId().split("_");
        // GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        // if (groupMaintain != null && groupMaintain.isMaintain()) {
        //     throw new DiceGameException(DiceGameError.USER_MAINTAINANCE, request.getUId(),
        //         request.getUd().getUserType(), request.getUd().getUserType(), request.getCId());
        // }

        DiceResponseStatusSG.Builder builder = DiceResponseStatusSG.newBuilder();

        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new DiceGameException(DiceGameError.NOT_SUPPORT_CURR, request.getUId(),
                    request.getUd().getUserType(),
                    request.getCId());
        }
        UserInfo userInfo = UserInfo.builder()
                .userId(request.getUId())
                .userType(request.getUd().getUserType())
                .userAgent(request.getUd().getUserAgent())
                .displayName(request.getUd().getDisplayName())
                .money(request.getUd().getMoney())
                .ip(request.getUd().getIp())
                .avatar(request.getUd().getAvatar())
                .currency(currency.name())
                .build();

        int result = slotGameService.getLatestState(request.getCId(), userInfo);

        activePlayerService.updateLastModified(userInfo.userId(), userInfo.userType());

        builder.setC(String.valueOf(result));
        Struct.Builder structBuilder = Struct.newBuilder();
        builder.setSt(structBuilder.build());
        responseObserver.onNext(builder.build());
//        log.info("After Next gamble game {}", codeResult);
        responseObserver.onCompleted();

    }

    @Override
    public void leaveGameTrial(DicePlayerInfo player, StreamObserver<DiceResponseStatusSG> responseObserver) {
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(player.getCId())
                .actorId(player.getUId())
                .serviceId("")
                .psId("")
                .stateName("DiceGameGrpcServiceImpl.leaveGameTrial")
                .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
                .message(player)
                .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());

        CurrencyType currency = configNormal.getAvailableCurrency(player.getUd().getCurrency());
        if (currency == null) {
            throw new DiceGameException(DiceGameError.NOT_SUPPORT_CURR, player.getUId(),
                    player.getUd().getUserType(),
                    player.getCId());
        }
        UserInfo userInfo = UserInfo.builder()
                .userId(player.getUId())
                .userType(player.getUd().getUserType())
                .userAgent(player.getUd().getUserAgent())
                .displayName(player.getUd().getDisplayName())
                .money(player.getUd().getMoney())
                .ip(player.getUd().getIp())
                .avatar(player.getUd().getAvatar())
                .currency(currency.name())
                .build();

        int result = slotGameService.leaveGameTrial(player.getCId(), userInfo);
        DiceResponseStatusSG resultRes = DiceResponseStatusSG.newBuilder().setC(String.valueOf(result)).build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
    }

    @Override
    public void normalSpinTrial(DiceNormalSpin request, StreamObserver<DiceResponseStatusSG> responseObserver) {
        //log builder
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("DiceGameGrpcServiceImpl.normalSpinTrial")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());

//        String prefixArray[] = request.getUId().split("_");
        GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        if (groupMaintain != null && groupMaintain.isMaintain()) {
            throw new DiceGameException(DiceGameError.USER_MAINTAINANCE, request.getUId(),
                request.getUd().getUserType(), request.getCId());
        }

        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new DiceGameException(DiceGameError.NOT_SUPPORT_CURR, request.getUId(),
                    request.getUd().getUserType(),
                    request.getCId());
        }

        SpinCmd spinCmd = null;
        if (!request.getBId().equals("")) {
            spinCmd = new SpinCmd().totalBetId(request.getBId()).lineIds(request.getBLnList()).currency(currency.name());
        }
        if (spinCmd != null) {

            UserInfo userInfo = UserInfo.builder()
                    .userId(request.getUId())
                    .userType(request.getUd().getUserType())
                    .userAgent(request.getUd().getUserAgent())
                    .displayName(request.getUd().getDisplayName())
                    .money(request.getUd().getMoney())
                    .ip(request.getUd().getIp())
                    .avatar(request.getUd().getAvatar())
                    .env(request.getEnv())
                    .ssid(request.getSsid())
                    .currency(currency.name())
                    .build();

            BasePlaySession basePlaySessionResult = slotGameService.normalSpinTrial(request.getCId(), userInfo, spinCmd);

            activePlayerService.updateLastModified(userInfo.userId(), userInfo.userType());

            int codeResult = basePlaySessionResult == null ? 1 : 0;
            DiceResponseStatusSG.Builder builder = DiceResponseStatusSG.newBuilder();
            builder.setC(String.valueOf(codeResult));
            builder.setSt(buildState(basePlaySessionResult));
            DiceResponseStatusSG resultRes = builder.build();
            responseObserver.onNext(resultRes);
            responseObserver.onCompleted();

            logBuilder.message("After Next normal trial play")
                .timeExe(0);
            LogsUtils.writeLogDebug(logBuilder.build());
        } else {
            logBuilder.message("BettingLine is Zero in normal trial")
                .timeExe(0);
            LogsUtils.writeLogDebug(logBuilder.build());

            DiceResponseStatusSG resultRes = DiceResponseStatusSG.newBuilder().setC(String.valueOf(1)).build();
            responseObserver.onNext(resultRes);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void setGroupUsersMaintain(DiceGroupUsersMaintainRequest request, StreamObserver<DiceResponseStatusSGBO> responseObserver) {
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId("")
            .actorId(request.getPrefix())
            .serviceId("")
            .psId("")
            .stateName("DiceGameGrpcServiceImpl.setGroupUsersMaintain")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        if (request.getMaintain() == true) {
            groupMaintainRepository.save(configNormal.serviceId(), new GroupUserMaintaince(configNormal.serviceId(),
                request.getPrefix(), request.getMaintain()));
        } else {
            groupMaintainRepository.removeGroupMaintain(configNormal.serviceId(), request.getPrefix());
        }

        DiceResponseStatusSGBO.Builder builder = DiceResponseStatusSGBO.newBuilder();

        int codeResult =  0;
        builder.setCode(String.valueOf(codeResult));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();

    }

    private Struct buildState(BasePlaySession basePlaySession) {

        BaseViewerObj viewerObj = new BaseViewerObj();
        Struct struct = null;
        try {
            viewerObj.isFinished(basePlaySession.isFinished());
            struct = StructUtil.convertToStruct(viewerObj);

        } catch (IllegalArgumentException | IllegalAccessException e) {
            log.error(e.getMessage() , e);
        }
        return struct;
    }
}

@Data
@Accessors(fluent = true)
class BaseViewerObj {
    private boolean isFinished;
}


