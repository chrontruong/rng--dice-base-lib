package com.io.begstd.slot.game;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import com.io.begstd.extension.loader.ExtensionLoader;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.common.SlotGameConstant;
import com.io.begstd.slot.common.SlotGameError;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.grpc.promotionservice.PromotionData;
import com.io.begstd.slot.grpc.slotgame.*;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.GroupUserMaintaince;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.CurrencyType;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.model.wallet.WalletOption;
import com.io.begstd.slot.repository.RedisGroupMaintainRepository;
import com.io.begstd.slot.services.internal.ActivePlayerService;
import com.io.begstd.slot.services.internal.SlotGameService;
import com.io.begstd.slot.utils.ConfigManager;
import com.io.begstd.slot.utils.StructUtil;
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
public class SlotGameGrpcServiceImpl extends SlotGameServiceGrpc.SlotGameServiceImplBase {
//    public final static String BOT_USER_TYPE = "BOT";
//    public final static String USER_TYPE = "USER";
    private static SlotGameGrpcServiceImpl instance;
    @Autowired
    private SlotGameService slotGameService;

    @Autowired
    private RedisGroupMaintainRepository groupMaintainRepository;

    @Autowired
    private ActivePlayerService activePlayerService;

    @Autowired
    private ExtensionLoader extensionLoader;

    @Autowired
    private ConfigManager configManager;

    private ISlotMachineConfig configNormal;

    @PostConstruct
    public void init() {
        instance = this;
        configNormal = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
    }
    
    @Override
    public void joinGame(PlayerInfo player, StreamObserver<ResponseStatusSG> responseObserver) {
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(player.getCId())
                .actorId(player.getUId())
                .serviceId("")
                .psId("")
                .stateName("SlotGameGrpcServiceImpl.joinGame")
                .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
                .message(player)
                .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
//        log.info("Start join game - player={}!", player);
//        String prefixArray[] = player.getUId().split("_");
//        GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), player.getUId());
//        if (groupMaintain != null && groupMaintain.isMaintain()) {
//            throw new SlotGameException(SlotGameError.USER_MAINTAINANCE,
//                player.getUId(), player.getUd().getUserType(),
//                player.getCId());
//        }

        String userType = "";
        if(SlotGameConstant.BOT_TYPE.equals(player.getUd().getUserType())) {
            userType = SlotGameConstant.NBOT_TYPE;
        } else {
            userType = player.getUd().getUserType();
            if (userType == null) {
                userType = SlotGameConstant.USER_TYPE;
            }
        }

        CurrencyType currency = configNormal.getAvailableCurrency(player.getUd().getCurrency());
        
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, player.getUId(),
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

        PromotionData promotionData = player.getPromotionData();
        try {
            if (promotionData == PromotionData.getDefaultInstance()) {
                promotionData = null;
            } else {
                log.debug("PromotionData: {}", JsonFormat.printer().print(promotionData));
            }
        } catch (InvalidProtocolBufferException e) {
            log.error(e.getMessage(), e);
        }
        int result = slotGameService.joinGame(player.getCId(), userInfo, promotionData, player.getEnv());

        activePlayerService.updateLastModified(userInfo.userId(), userType);
        
        ResponseStatusSG resultRes = ResponseStatusSG.newBuilder().setC(String.valueOf(result)).build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
//        log.info("Join successfully");
    }

    @Override
    public void leaveGame(PlayerInfo player, StreamObserver<ResponseStatusSG> responseObserver) {

        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(player.getCId())
            .actorId(player.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.leaveGame")
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
        ResponseStatusSG resultRes = ResponseStatusSG.newBuilder().setC(String.valueOf(result)).build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
    }

    @Override
    public void normalSpin(NormalSpin request, StreamObserver<ResponseStatusSG> responseObserver) { 
        //log builder
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.NormalGame")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        String userType = "";
        if(SlotGameConstant.BOT_TYPE.equals(request.getUd().getUserType())) {
            userType = SlotGameConstant.NBOT_TYPE;
        } else {
            if (request.getUd().getUserType() != null) {
                userType = request.getUd().getUserType();
            } else {
                userType = SlotGameConstant.USER_TYPE;
            }
        }
        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) { 
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
            throw new SlotGameException(SlotGameError.USER_MAINTAINANCE, request.getUId(), 
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
            ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
            builder.setC(String.valueOf(codeResult));
            builder.setSt(buildState(basePlaySessionResult));
            ResponseStatusSG resultRes = builder.build();
            responseObserver.onNext(resultRes);
            responseObserver.onCompleted();
//            log.info("After Next normal play {}", codeResult);
        } else {
//            log.info("BettingLine is Zero");
            logBuilder.message("BettingLine is Zero ")
                .timeExe(0);
            LogsUtils.writeLogDebug(logBuilder.build());
        
            ResponseStatusSG resultRes = ResponseStatusSG.newBuilder().setC(String.valueOf(1)).build();
            responseObserver.onNext(resultRes);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void freeSpin(FreeSpin request, StreamObserver<ResponseStatusSG> responseObserver) {
//        log.info("Start free play request={}!", request);
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.freeSpin")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        
//        String prefixArray[] = request.getUId().split("_");
//        GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
//        if (groupMaintain != null && groupMaintain.isMaintain()) {
//            throw new SlotGameException(SlotGameError.USER_MAINTAINANCE,
//                request.getUId(), request.getUd().getUserType(), request.getCId());
//        }
        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
                .env(request.getEnv())
                .ssid(request.getSsid())
                .currency(currency.name())
                .build();

        BasePlaySession basePlaySessionResult = slotGameService.freeSpin(request.getCId(), userInfo);

        activePlayerService.updateLastModified(userInfo.userId(), userInfo.userType());

        int codeResult = basePlaySessionResult == null ? 1 : 0;
        ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
        builder.setC(String.valueOf(codeResult));
        builder.setSt(buildState(basePlaySessionResult));
        ResponseStatusSG resultRes = builder.build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
//        log.info("After Next free play {}", codeResult);
    }
    
    @Override
    public void respin(FreeSpin request, StreamObserver<ResponseStatusSG> responseObserver) {
//        log.info("Start free play request={}!", request);
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
                .rootCmdId(request.getCId())
                .actorId(request.getUId())
                .serviceId("")
                .psId("")
                .stateName("SlotGameGrpcServiceImpl.respin")
                .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
                .message(request)
                .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        ResponseStatusSG resultRes = ResponseStatusSG.newBuilder().setC(String.valueOf(0)).build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();

//        String prefixArray[] = request.getUId().split("_");
//        GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
//        if (groupMaintain != null && groupMaintain.isMaintain()) {
//            throw new SlotGameException(SlotGameError.USER_MAINTAINANCE,
//                    request.getUId(), request.getUd().getUserType(), request.getCId());
//        }

        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
                .env(request.getEnv())
                .ssid(request.getSsid())
                .currency(currency.name())
                .build();

//        BasePlaySession basePlaySessionResult =
        slotGameService.respin(request.getCId(), userInfo);

        activePlayerService.updateLastModified(userInfo.userId(), request.getUd().getUserType());

//        int codeResult = basePlaySessionResult == null ? 1 : 0;
//        ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
//        builder.setC(String.valueOf(codeResult));
//        builder.setSt(buildState(basePlaySessionResult));
//        ResponseStatusSG resultRes = builder.build();
//        responseObserver.onNext(resultRes);
//        responseObserver.onCompleted();
//        log.info("After Next free play {}", codeResult);
    }

    @Override
    public void freeSpinOption(FreeSpinOption request, StreamObserver<ResponseStatusSG> responseObserver) {
//        log.info("Start free Spin Option play request={}!", request);
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.freeSpinOption")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        
//        String prefixArray[] = request.getUId().split("_");
//        GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
//        if (groupMaintain != null && groupMaintain.isMaintain()) {
//            throw new SlotGameException(SlotGameError.USER_MAINTAINANCE, request.getUId(),
//                request.getUd().getUserType(), request.getCId());
//        }
        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
                .env(request.getEnv())
                .ssid(request.getSsid())
                .currency(currency.name())
                .build();

        BasePlaySession basePlaySessionResult = slotGameService.freeSpinOption(request.getCId(), userInfo, request.getOpt());

        activePlayerService.updateLastModified(userInfo.userId(), userInfo.userType());

        int codeResult = basePlaySessionResult == null ? 1 : 0;
        ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
        builder.setC(String.valueOf(codeResult));
        builder.setSt(buildState(basePlaySessionResult));
        ResponseStatusSG resultRes = builder.build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
//        log.info("After Next free Spin Option play {}", codeResult);
    }

    @Override
    public void miniGame(MiniGame request, StreamObserver<ResponseStatusSG> responseObserver) {
//        log.info("Start mini game request={}!", request);
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.miniGame")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        
//        String prefixArray[] = request.getUId().split("_");
        // GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        // if (groupMaintain != null && groupMaintain.isMaintain()) {
        //     throw new SlotGameException(SlotGameError.USER_MAINTAINANCE, request.getUId(), 
        //         request.getUd().getUserType(), request.getCId());
        // }
        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
                .env(request.getEnv())
                .ssid(request.getSsid())
                .currency(currency.name())
                .build();

        BasePlaySession basePlaySessionResult = slotGameService.playMiniGame(request.getCId(), userInfo, request.getCOp());

        activePlayerService.updateLastModified(userInfo.userId(), userInfo.userType());

        int codeResult = basePlaySessionResult == null ? 1 : 0;
        ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
        builder.setC(String.valueOf(codeResult));
        builder.setSt(buildState(basePlaySessionResult));
        ResponseStatusSG resultRes = builder.build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
//        log.info("After Next mini game {}", codeResult);
    }
    
    @Override
    public void lightningSpin(LightningSpin request, StreamObserver<ResponseStatusSG> responseObserver) {
//        log.info("Start lightingSpin play request={}!", request);
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.lightningSpin")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        
//        String prefixArray[] = request.getUId().split("_");
        // GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        // if (groupMaintain != null && groupMaintain.isMaintain()) {
        //     throw new SlotGameException(SlotGameError.USER_MAINTAINANCE, request.getUId(), 
        //         request.getUd().getUserType(), request.getCId());
        // }
        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
                .env(request.getEnv())
                .ssid(request.getSsid())
                .currency(currency.name())
                .build();

        BasePlaySession basePlaySessionResult = slotGameService.lightingSpin(request.getCId(), userInfo);

        activePlayerService.updateLastModified(userInfo.userId(), userInfo.userType());

        int codeResult = basePlaySessionResult == null ? 1 : 0;
        ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
        builder.setC(String.valueOf(codeResult));
        builder.setSt(buildState(basePlaySessionResult));
        ResponseStatusSG resultRes = builder.build();
        responseObserver.onNext(resultRes);
//        log.info("After Next lightingSpin play {}", codeResult);
        responseObserver.onCompleted();
    }
    
    @Override
    public void powerUpSpin(PowerUpSpin request, StreamObserver<ResponseStatusSG> responseObserver) {
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.powerUpSpin")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());

//        String prefixArray[] = request.getUId().split("_");
        // GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        // if (groupMaintain != null && groupMaintain.isMaintain()) {
        //     throw new SlotGameException(SlotGameError.USER_MAINTAINANCE, request.getUId(), 
        //         request.getUd().getUserType(), request.getCId());
        // }
        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
                .env(request.getEnv())
                .ssid(request.getSsid())
                .currency(currency.name())
                .build();

        BasePlaySession basePlaySessionResult = slotGameService.powerUpSpin(request.getCId(), userInfo, request.getCOp());

        activePlayerService.updateLastModified(userInfo.userId(), userInfo.userType());

        int codeResult = basePlaySessionResult == null ? 1 : 0;
        ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
        builder.setC(String.valueOf(codeResult));
        builder.setSt(buildState(basePlaySessionResult));
        ResponseStatusSG resultRes = builder.build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
//        log.info("After Next powerUpSpin play {}", codeResult);
    }
    
    @Override
    public void gamble(GambleBet request, StreamObserver<ResponseStatusSG> responseObserver) {
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.gamble")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        
//        String prefixArray[] = request.getUId().split("_");
        // GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        // if (groupMaintain != null && groupMaintain.isMaintain()) {
        //     throw new SlotGameException(SlotGameError.USER_MAINTAINANCE, request.getUId(), 
        //         request.getUd().getUserType(), request.getCId());
        // }
        
        ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
                .env(request.getEnv())
                .ssid(request.getSsid())
                .currency(currency.name())
                .build();

        BasePlaySession basePlaySessionResult = slotGameService.gamble(request.getCId(), userInfo, request.getCOp(), request.getTalB());

        activePlayerService.updateLastModified(userInfo.userId(), userInfo.userType());

        int codeResult = basePlaySessionResult == null ? 1 : 0;
        builder.setC(String.valueOf(codeResult));
        builder.setSt(buildState(basePlaySessionResult));
        responseObserver.onNext(builder.build());
//        log.info("After Next gamble game {}", codeResult);
        responseObserver.onCompleted();

    }
    
    @Override
    public void getLatestState(GetLatestStateRequest request, StreamObserver<ResponseStatusSG> responseObserver) {
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.getLatestState")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        
//        String prefixArray[] = request.getUId().split("_");
        // GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        // if (groupMaintain != null && groupMaintain.isMaintain()) {
        //     throw new SlotGameException(SlotGameError.USER_MAINTAINANCE, request.getUId(), 
        //         request.getUd().getUserType(), request.getUd().getUserType(), request.getCId());
        // }
        
        ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();

        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
    


//===============================trial==============================================
    
//    @Override
//    public void joinGameTrial(PlayerTrialInfo player, StreamObserver<ResponseStatusSG> responseObserver) {
////        log.info("Start join game - player={}!", player);
//        int result = slotGameService.joinGameTrial(player.getUId(), player.getCId(),player.getServiceId());
//        
//        ResponseStatusSG resultRes = ResponseStatusSG.newBuilder().setC(String.valueOf(result)).build();
//        responseObserver.onNext(resultRes);
//        responseObserver.onCompleted();
////        log.info("Join successfully");
//    }

    @Override
    public void leaveGameTrial(PlayerInfo player, StreamObserver<ResponseStatusSG> responseObserver) {
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(player.getCId())
                .actorId(player.getUId())
                .serviceId("")
                .psId("")
                .stateName("SlotGameGrpcServiceImpl.leaveGameTrial")
                .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
                .message(player)
                .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());

        CurrencyType currency = configNormal.getAvailableCurrency(player.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, player.getUId(),
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
        ResponseStatusSG resultRes = ResponseStatusSG.newBuilder().setC(String.valueOf(result)).build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
    }

    @Override
    public void normalSpinTrial(NormalSpin request, StreamObserver<ResponseStatusSG> responseObserver) {        
        //log builder
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.normalSpinTrial")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        
//        String prefixArray[] = request.getUId().split("_");
        GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        if (groupMaintain != null && groupMaintain.isMaintain()) {
            throw new SlotGameException(SlotGameError.USER_MAINTAINANCE, request.getUId(), 
                request.getUd().getUserType(), request.getCId());
        }
        
        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
            ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
            builder.setC(String.valueOf(codeResult));
            builder.setSt(buildState(basePlaySessionResult));
            ResponseStatusSG resultRes = builder.build();
            responseObserver.onNext(resultRes);
            responseObserver.onCompleted();
            
            logBuilder.message("After Next normal trial play")
                .timeExe(0);
            LogsUtils.writeLogDebug(logBuilder.build());
        } else {
            logBuilder.message("BettingLine is Zero in normal trial")
                .timeExe(0);
            LogsUtils.writeLogDebug(logBuilder.build());
        
            ResponseStatusSG resultRes = ResponseStatusSG.newBuilder().setC(String.valueOf(1)).build();
            responseObserver.onNext(resultRes);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void respinTrial(FreeSpin request, StreamObserver<ResponseStatusSG> responseObserver) {
//        log.info("Start free play request={}!", request);
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
                .rootCmdId(request.getCId())
                .actorId(request.getUId())
                .serviceId("")
                .psId("")
                .stateName("SlotGameGrpcServiceImpl.freeSpinTrial")
                .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
                .message(request)
                .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());

//        String prefixArray[] = request.getUId().split("_");
        // GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        // if (groupMaintain != null && groupMaintain.isMaintain()) {
        //     throw new SlotGameException(SlotGameError.USER_MAINTAINANCE, request.getUId(),
        //             request.getUd().getUserType(), request.getCId());
        // }
        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
                .env(request.getEnv())
                .ssid(request.getSsid())
                .currency(currency.name())
                .build();

        BasePlaySession basePlaySessionResult = slotGameService.respinTrial(request.getCId(), userInfo);

        activePlayerService.updateLastModified(userInfo.userId(), request.getUd().getUserType());

        int codeResult = basePlaySessionResult == null ? 1 : 0;
        ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
        builder.setC(String.valueOf(codeResult));
        builder.setSt(buildState(basePlaySessionResult));
        ResponseStatusSG resultRes = builder.build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
//        log.info("After Next pre play {}", codeResult);
    }

    @Override
    public void freeSpinTrial(FreeSpin request, StreamObserver<ResponseStatusSG> responseObserver) {
//        log.info("Start free play request={}!", request);
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.freeSpinTrial")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        
//        String prefixArray[] = request.getUId().split("_");
        // GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        // if (groupMaintain != null && groupMaintain.isMaintain()) {
        //     throw new SlotGameException(SlotGameError.USER_MAINTAINANCE, request.getUId(), 
        //         request.getUd().getUserType(), request.getCId());
        // }

        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
                .env(request.getEnv())
                .ssid(request.getSsid())
                .currency(request.getUd().getCurrency())
                .build();

        BasePlaySession basePlaySessionResult = slotGameService.freeSpinTrial(request.getCId(), userInfo);

        activePlayerService.updateLastModified(userInfo.userId(), userInfo.userType());

        int codeResult = basePlaySessionResult == null ? 1 : 0;
        ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
        builder.setC(String.valueOf(codeResult));
        builder.setSt(buildState(basePlaySessionResult));
        ResponseStatusSG resultRes = builder.build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
//        log.info("After Next free play {}", codeResult);
    }
    
    @Override
    public void freeSpinOptionTrial(FreeSpinOption request, StreamObserver<ResponseStatusSG> responseObserver) {
//        log.info("Start free Spin Option play request={}!", request);
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.freeSpinOptionTrial")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        
//        String prefixArray[] = request.getUId().split("_");
        // GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        // if (groupMaintain != null && groupMaintain.isMaintain()) {
        //     throw new SlotGameException(SlotGameError.USER_MAINTAINANCE, request.getUId(), 
        //         request.getUd().getUserType(), request.getCId());
        // }

        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
                .env(request.getEnv())
                .ssid(request.getSsid())
                .currency(request.getUd().getCurrency())
                .build();

        BasePlaySession basePlaySessionResult = slotGameService.freeSpinOptionTrial(request.getCId(), userInfo, request.getOpt());

        activePlayerService.updateLastModified(userInfo.userId(), userInfo.userType());

        int codeResult = basePlaySessionResult == null ? 1 : 0;
        ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
        builder.setC(String.valueOf(codeResult));
        builder.setSt(buildState(basePlaySessionResult));
        ResponseStatusSG resultRes = builder.build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
//        log.info("After Next free Spin Option play {}", codeResult);
    }

    @Override
    public void miniGameTrial(MiniGame request, StreamObserver<ResponseStatusSG> responseObserver) {
//        log.info("Start mini game request={}!", request);
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.miniGameTrial")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        
//        String prefixArray[] = request.getUId().split("_");
        // GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        // if (groupMaintain != null && groupMaintain.isMaintain()) {
        //     throw new SlotGameException(SlotGameError.USER_MAINTAINANCE, request.getUId(), 
        //         request.getUd().getUserType(), request.getCId());
        // }

        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
                .env(request.getEnv())
                .ssid(request.getSsid())
                .currency(request.getUd().getCurrency())
                .build();

        BasePlaySession basePlaySessionResult = slotGameService.playMiniGameTrial(request.getCId(), userInfo, request.getCOp());

        activePlayerService.updateLastModified(userInfo.userId(), userInfo.userType());

        int codeResult = basePlaySessionResult == null ? 1 : 0;
        ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
        builder.setC(String.valueOf(codeResult));
        builder.setSt(buildState(basePlaySessionResult));
        ResponseStatusSG resultRes = builder.build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
//        log.info("After Next mini game {}", codeResult);
    }
    
    @Override
    public void lightningSpinTrial(LightningSpin request, StreamObserver<ResponseStatusSG> responseObserver) {
//        log.info("Start lightingSpin play request={}!", request);
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.lightningSpinTrial")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        
//        String prefixArray[] = request.getUId().split("_");
        // GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        // if (groupMaintain != null && groupMaintain.isMaintain()) {
        //     throw new SlotGameException(SlotGameError.USER_MAINTAINANCE, request.getUId(), 
        //         request.getUd().getUserType(), request.getCId());
        // }

        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
                .env(request.getEnv())
                .ssid(request.getSsid())
                .currency(request.getUd().getCurrency())
                .build();

        BasePlaySession basePlaySessionResult = slotGameService.lightingSpinTrial(request.getCId(), userInfo);

        activePlayerService.updateLastModified(userInfo.userId(), userInfo.userType());

        int codeResult = basePlaySessionResult == null ? 1 : 0;
        ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
        builder.setC(String.valueOf(codeResult));
        builder.setSt(buildState(basePlaySessionResult));
        ResponseStatusSG resultRes = builder.build();
        responseObserver.onNext(resultRes);
//        log.info("After Next lightingSpin play {}", codeResult);
        responseObserver.onCompleted();
    }
    
    @Override
    public void powerUpSpinTrial(PowerUpSpin request, StreamObserver<ResponseStatusSG> responseObserver) {
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.powerUpSpinTrial")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        
//        String prefixArray[] = request.getUId().split("_");
        // GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        // if (groupMaintain != null && groupMaintain.isMaintain()) {
        //     throw new SlotGameException(SlotGameError.USER_MAINTAINANCE, request.getUId(), 
        //         request.getUd().getUserType(), request.getCId());
        // }

        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
                .env(request.getEnv())
                .ssid(request.getSsid())
                .currency(request.getUd().getCurrency())
                .build();

        BasePlaySession basePlaySessionResult = slotGameService.powerUpSpinTrial(request.getCId(), userInfo, request.getCOp());

        activePlayerService.updateLastModified(userInfo.userId(), userInfo.userType());

        int codeResult = basePlaySessionResult == null ? 1 : 0;
        ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
        builder.setC(String.valueOf(codeResult));
        builder.setSt(buildState(basePlaySessionResult));
        ResponseStatusSG resultRes = builder.build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
//        log.info("After Next powerUpSpin play {}", codeResult);

    }
    
    @Override
    public void gambleTrial(GambleBet request, StreamObserver<ResponseStatusSG> responseObserver) {
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(request.getCId())
            .actorId(request.getUId())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.gambleTrial")
            .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
            .message(request)
            .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());
        
//        String prefixArray[] = request.getUId().split("_");
        // GroupUserMaintaince groupMaintain = groupMaintainRepository.get(configNormal.serviceId(), request.getUId());
        // if (groupMaintain != null && groupMaintain.isMaintain()) {
        //     throw new SlotGameException(SlotGameError.USER_MAINTAINANCE, request.getUId(), 
        //         request.getUd().getUserType(), request.getCId());
        // }

        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
        if (currency == null) {
            throw new SlotGameException(SlotGameError.NOT_SUPPORT_CURR, request.getUId(),
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
                .env(request.getEnv())
                .ssid(request.getSsid())
                .currency(request.getUd().getCurrency())
                .build();

        ResponseStatusSG.Builder builder = ResponseStatusSG.newBuilder();
        BasePlaySession basePlaySessionResult = slotGameService.gambleTrial(
                request.getCId(), userInfo, request.getCOp(), request.getTalB());

        activePlayerService.updateLastModified(userInfo.userId(), userInfo.userType());

        int codeResult = basePlaySessionResult == null ? 1 : 0;
        builder.setC(String.valueOf(codeResult));
        builder.setSt(buildState(basePlaySessionResult));
        responseObserver.onNext(builder.build());
//        log.info("After Next gamble game {}", codeResult);
        responseObserver.onCompleted();

    }
    
    @Override
    public void setGroupUsersMaintain(GroupUsersMaintainRequest request, StreamObserver<ResponseStatusSGBO> responseObserver) {
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId("")
            .actorId(request.getPrefix())
            .serviceId("")
            .psId("")
            .stateName("SlotGameGrpcServiceImpl.setGroupUsersMaintain")
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
        
        ResponseStatusSGBO.Builder builder = ResponseStatusSGBO.newBuilder();
        
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
            viewerObj.hasFreeGameGame(basePlaySession.hasFreeGame());
            viewerObj.hasBonusGame(basePlaySession.hasBonusGame());
            viewerObj.hasLightningGame(basePlaySession.hasLightningGame());
            viewerObj.hasPowerUpGame(basePlaySession.hasPowerUpGame());
            
            if(basePlaySession.freeGameOption() != null && basePlaySession.freeGameOption().size() > 0) {
                viewerObj.hasFreeOptionGame(true);
            }
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
    private boolean hasBonusGame;
    private boolean hasFreeGameGame;
    private boolean hasFreeOptionGame;
    private boolean hasLightningGame;
    private boolean hasPowerUpGame;
}


