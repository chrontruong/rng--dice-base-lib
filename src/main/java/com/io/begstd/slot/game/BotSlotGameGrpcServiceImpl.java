package com.io.begstd.slot.game;

import com.google.protobuf.Struct;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.common.SlotGameConstant;
import com.io.begstd.slot.common.SlotGameError;
import com.io.begstd.slot.grpc.slotbot.*;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.CurrencyType;
import com.io.begstd.slot.model.config.SlotMachineConfigForNormal;
import com.io.begstd.slot.model.wallet.WalletOption;
import com.io.begstd.slot.projection.IPlaySessionProjection;
import com.io.begstd.slot.services.internal.ActivePlayerService;
import com.io.begstd.slot.services.internal.SlotGameService;
import com.io.begstd.slot.services.internal.UserService;
import com.io.begstd.slot.utils.StructUtil;
import io.grpc.stub.StreamObserver;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@GRpcService
public class BotSlotGameGrpcServiceImpl extends BotSlotGameServiceGrpc.BotSlotGameServiceImplBase {
//    public final static String BOT_USER_TYPE = "BOT";
    @Autowired
    private SlotGameService slotGameService;
    
    @Autowired
    private IPlaySessionProjection playSessionProjection;

    @Autowired
    private UserService userService;
    
    @Autowired
    private ActivePlayerService activePlayerService;
    
    @Autowired  
    private SlotMachineConfigForNormal configNormal;

  //=============================================BOT==========================================================
  @Override
  public void botJoinGame(BotPlayerInfo player, StreamObserver<ResponseStatusBotSG> responseObserver) {
//      log.info("Start join game - player={}!", player);
      LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
      logBuilder.cmdId(player.getCId())
          .actorId(player.getUId())
          .stateName("BotSlotGameGrpcServiceImpl.botJoinGame")
          .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
          .message(player);
      LogsUtils.writeLogInfo(logBuilder.build());
          
      if(!SlotGameConstant.BOT_TYPE.equals(player.getUd().getUserType())) {
//        log.error("ERROR:  This service only use for bot in botPlayNormalSpin play request:{} -- userInfo :{}!", request, userInfo);
        logBuilder.message("ERROR:  This service only uses for bot! " + player)
            .timeExe(0);
        LogsUtils.writeLogError(logBuilder.build());
    
        ResponseStatusBotSG resultRes = ResponseStatusBotSG.newBuilder().setC(SlotGameError.INVALID_COMMAND.getErrorCode()).build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
        return;
      }
      CurrencyType currency = configNormal.getAvailableCurrency(player.getUd().getCurrency());
      UserInfo userInfo = UserInfo.builder()
              .userId(player.getUId())
              .userType(player.getUd().getUserType())
              .userAgent(player.getUd().getUserAgent())
              .displayName(player.getUd().getDisplayName())
              .money(player.getUd().getMoney())
              .ip(player.getUd().getIp())
              .avatar(player.getUd().getAvatar())
              .env(player.getEnv())     
              .currency(currency.name()) 
              .build();
      
      int result = slotGameService.joinGame(player.getCId(), userInfo, null, player.getEnv());
      
      activePlayerService.updateLastModified(userInfo.userId(), player.getUd().getUserType());
      
      ResponseStatusBotSG resultRes = ResponseStatusBotSG.newBuilder().setC(String.valueOf(result)).build();
      responseObserver.onNext(resultRes);
      responseObserver.onCompleted();
//      log.info("Join successfully");
  }
  @Override
  public void botPlayNormalSpin(BotNormalSpin request, StreamObserver<ResponseStatusBotSG> responseObserver) {
//      log.info("Start botPlayNormalSpin play request={}!", request);
      LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
      logBuilder.cmdId(request.getCId())
          .actorId(request.getUId())
          .serviceId("")
          .psId("")
          .stateName("BotSlotGameGrpcServiceImpl.botPlayNormalSpin")
          .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
          .message(request)
          .timeExe(0);
      LogsUtils.writeLogInfo(logBuilder.build());
      
//      UserJoinGame userInfo = joinGameRepository.get(userService.getServiceId(), request.getUId());
//      if(userInfo ==  null || !BOT_USER_TYPE.equals(userInfo.getUserType())) {
      if (!SlotGameConstant.BOT_TYPE.equalsIgnoreCase(request.getUd().getUserType())) {
          logBuilder.message("ERROR:  This service only uses for bot! " + request.getUd().getUserType())
              .timeExe(0);
          LogsUtils.writeLogError(logBuilder.build());
      
          ResponseStatusBotSG resultRes = ResponseStatusBotSG.newBuilder().setC(SlotGameError.INVALID_COMMAND.getErrorCode()).build();
          responseObserver.onNext(resultRes);
          responseObserver.onCompleted();
          return;
      }
//      log.info("BetId: " + request.getBId());
//      log.info("TotalBet: " + Money.of(request.getTalB()));
      CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
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
                  .walletOption(WalletOption.MAIN)
                  .currency(currency.name())
                  .build();

          BasePlaySession basePlaySessionResult = slotGameService.normalSpin(request.getCId(), userInfo, spinCmd);
          
          activePlayerService.updateLastModified(userInfo.userId(), request.getUd().getUserType());
          
          int codeResult = basePlaySessionResult == null ? 1 : 0;
          ResponseStatusBotSG.Builder builder = ResponseStatusBotSG.newBuilder();
          builder.setC(String.valueOf(codeResult));
          builder.setSt(buildStateForBot(basePlaySessionResult));
          ResponseStatusBotSG resultRes = builder.build();
          responseObserver.onNext(resultRes);
          responseObserver.onCompleted();
          
          logBuilder.message("Done - botPlayNormalSpin play " + request.getUd().getUserType())
              .timeExe(0);
          LogsUtils.writeLogDebug(logBuilder.build());
      
//          log.info("After Next botPlayNormalSpin play {}", codeResult);
      } else {
//          log.info("BettingLine is Zero");
          logBuilder.message("BettingLine is Zero")
              .timeExe(0);
          LogsUtils.writeLogDebug(logBuilder.build());
      
          ResponseStatusBotSG resultRes = ResponseStatusBotSG.newBuilder().setC(String.valueOf(1)).build();
          responseObserver.onNext(resultRes);
          responseObserver.onCompleted();
      }
  }

  @Override
  public void botPlayFreeSpin(BotFreeSpin request, StreamObserver<ResponseStatusBotSG> responseObserver) {
      LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
      logBuilder.cmdId(request.getCId())
          .actorId(request.getUId())
          .serviceId("")
          .psId("")
          .stateName("BotSlotGameGrpcServiceImpl.botPlayFreeSpin")
          .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
          .message(request)
          .timeExe(0);
      LogsUtils.writeLogInfo(logBuilder.build());
      
//      log.info("Start botPlayFreeSpin play request={}!", request);
//      UserJoinGame userInfo = joinGameRepository.get(userService.getServiceId(), request.getUId());
//      if(userInfo ==  null || !BOT_USER_TYPE.equals(userInfo.getUserType())) {
      if (!SlotGameConstant.BOT_TYPE.equalsIgnoreCase(request.getUd().getUserType())) {
          logBuilder.message("ERROR:  This service only uses for bot! " + request.getUd().getUserType())
              .timeExe(0);
          LogsUtils.writeLogError(logBuilder.build());
//          log.error("ERROR:  This service only use for bot in botPlayFreeSpin play request={} -- userInfo :{}!", request, userInfo);
          ResponseStatusBotSG resultRes = ResponseStatusBotSG.newBuilder().setC(SlotGameError.INVALID_COMMAND.getErrorCode()).build();
          responseObserver.onNext(resultRes);
          responseObserver.onCompleted();
          return;
      }
      CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
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

      BasePlaySession basePlaySessionResult = slotGameService.freeSpin(request.getCId(), userInfo);

      int codeResult = basePlaySessionResult == null ? 1 : 0;
      ResponseStatusBotSG.Builder builder = ResponseStatusBotSG.newBuilder();
      builder.setC(String.valueOf(codeResult));
      builder.setSt(buildStateForBot(basePlaySessionResult));
      ResponseStatusBotSG resultRes = builder.build();
      responseObserver.onNext(resultRes);
      responseObserver.onCompleted();
//      log.info("After Next botPlayFreeSpin play {}", codeResult);
  }
  
  @Override
  public void botPlayFreeSpinOption(BotFreeSpinOption request, StreamObserver<ResponseStatusBotSG> responseObserver) {
//      log.info("Start botPlayFreeSpinOption play request={}!", request);
      
      LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
      logBuilder.cmdId(request.getCId())
          .actorId(request.getUId())
          .serviceId("")
          .psId("")
          .stateName("BotSlotGameGrpcServiceImpl.botPlayFreeSpinOption")
          .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
          .message(request)
          .timeExe(0);
      LogsUtils.writeLogInfo(logBuilder.build());
      
//      UserJoinGame userInfo = joinGameRepository.get(userService.getServiceId(), request.getUId());
//      if(userInfo ==  null || !BOT_USER_TYPE.equals(userInfo.getUserType())) {
      if (!SlotGameConstant.BOT_TYPE.equalsIgnoreCase(request.getUd().getUserType())) {
          logBuilder.message("ERROR:  This service only uses for bot! " + request.getUd().getUserType())
              .timeExe(0);
          LogsUtils.writeLogError(logBuilder.build());
          ResponseStatusBotSG resultRes = ResponseStatusBotSG.newBuilder().setC(SlotGameError.INVALID_COMMAND.getErrorCode()).build();
          responseObserver.onNext(resultRes);
          responseObserver.onCompleted();
          return;
      }
      CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
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

      BasePlaySession basePlaySessionResult = slotGameService.freeSpinOption(request.getCId(), userInfo, request.getOpt());

      int codeResult = basePlaySessionResult == null ? 1 : 0;
      ResponseStatusBotSG.Builder builder = ResponseStatusBotSG.newBuilder();
      builder.setC(String.valueOf(codeResult));
      builder.setSt(buildStateForBot(basePlaySessionResult));
      ResponseStatusBotSG resultRes = builder.build();
      responseObserver.onNext(resultRes);
      responseObserver.onCompleted();
//      log.info("After Next botPlayFreeSpinOption play {}", codeResult);
  }

  @Override
  public void botPlayMiniGame(BotMiniGame request, StreamObserver<ResponseStatusBotSG> responseObserver) {

      LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
      logBuilder.cmdId(request.getCId())
          .actorId(request.getUId())
          .serviceId("")
          .psId("")
          .stateName("BotSlotGameGrpcServiceImpl.botPlayMiniGame")
          .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
          .message(request)
          .timeExe(0);
      LogsUtils.writeLogInfo(logBuilder.build());

//      log.info("Start botPlayMiniGame game request={}!", request);
//      UserJoinGame userInfo = joinGameRepository.get(userService.getServiceId(), request.getUId());
//      if(userInfo ==  null || !BOT_USER_TYPE.equals(userInfo.getUserType())) {
      if (!SlotGameConstant.BOT_TYPE.equalsIgnoreCase(request.getUd().getUserType())) {
          logBuilder.message("ERROR:  This service only uses for bot! " + request.getUd().getUserType())
              .timeExe(0);
          LogsUtils.writeLogError(logBuilder.build());

//          log.error("ERROR:  This service only use for bot in botPlayMiniGame play request:{} -- userInfo :{}!", request, userInfo);
          ResponseStatusBotSG resultRes = ResponseStatusBotSG.newBuilder().setC(SlotGameError.INVALID_COMMAND.getErrorCode()).build();
          responseObserver.onNext(resultRes);
          responseObserver.onCompleted();
          return;
      }
      CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
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

      BasePlaySession basePlaySessionResult = slotGameService.playMiniGame(request.getCId(), userInfo, request.getCOp());
      int codeResult = basePlaySessionResult == null ? 1 : 0;
      ResponseStatusBotSG.Builder builder = ResponseStatusBotSG.newBuilder();
      builder.setC(String.valueOf(codeResult));
      builder.setSt(buildStateForBot(basePlaySessionResult));
      ResponseStatusBotSG resultRes = builder.build();
      responseObserver.onNext(resultRes);
      responseObserver.onCompleted();
//      log.info("After Next botPlayMiniGame game {}", codeResult);
  }
  
  @Override
  public void botResumeGameInfo(BotResumeSpin request, StreamObserver<ResponseStatusBotSG> responseObserver) {
      
      LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
      logBuilder.cmdId(request.getCId())
          .actorId(request.getUId())
          .serviceId("")
          .psId("")
          .stateName("BotSlotGameGrpcServiceImpl.botResumeGameInfo")
          .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
          .message(request)
          .timeExe(0);
      LogsUtils.writeLogInfo(logBuilder.build());
      
//      log.info("Start botResumeGameInfo game request={}!", request);
//      UserJoinGame userInfo = joinGameRepository.get(userService.getServiceId(), request.getUId());
//      if(userInfo ==  null || !SlotGameConstant.BOT_TYPE.equals(userInfo.getUserType())) {
      if (!SlotGameConstant.BOT_TYPE.equalsIgnoreCase(request.getUd().getUserType())) {
          logBuilder.message("ERROR:  This service only uses for bot! " + request.getUd().getUserType())
              .timeExe(0);
          LogsUtils.writeLogError(logBuilder.build());
//          log.error("ERROR:  This service only use for bot in botResumeGameInfo play request:{} -- userInfo :{}!", request, userInfo);
          ResponseStatusBotSG resultRes = ResponseStatusBotSG.newBuilder().setC(SlotGameError.INVALID_COMMAND.getErrorCode()).build();
          responseObserver.onNext(resultRes);
          responseObserver.onCompleted();
          return;
      }
      CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
      BasePlaySession basePlaySessionResult = userService.getPlaySession(userService.getServiceId(), UserInfo.builder().userId(request.getUId()).currency(currency.name()).build());
      int codeResult = 0;
      ResponseStatusBotSG.Builder builder = ResponseStatusBotSG.newBuilder();
      builder.setC(String.valueOf(codeResult));
      builder.setSt(buildStateForBot(basePlaySessionResult));
      ResponseStatusBotSG resultRes = builder.build();
      responseObserver.onNext(resultRes);
      responseObserver.onCompleted();
//      log.info("After Next botResumeGameInfo game {}", codeResult);
  }
  
  @Override
  public void botLightningSpin(BotLightningSpin request, StreamObserver<ResponseStatusBotSG> responseObserver) {
//      log.info("Start lightingSpin play request={}!", request);
      LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
      logBuilder.cmdId(request.getCId())
          .actorId(request.getUId())
          .serviceId("")
          .psId("")
          .stateName("BotSlotGameGrpcServiceImpl.botLightningSpin")
          .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
          .message(request)
          .timeExe(0);
      LogsUtils.writeLogInfo(logBuilder.build());
      
//      UserJoinGame userInfo = joinGameRepository.get(userService.getServiceId(), request.getUId());
//      if(userInfo ==  null || !SlotGameConstant.BOT_TYPE.equals(userInfo.getUserType())) {
      if (!SlotGameConstant.BOT_TYPE.equalsIgnoreCase(request.getUd().getUserType())) {
          logBuilder.message("ERROR:  This service only uses for bot! " + request.getUd().getUserType())
              .timeExe(0);
          LogsUtils.writeLogError(logBuilder.build());
          ResponseStatusBotSG resultRes = ResponseStatusBotSG.newBuilder().setC(SlotGameError.INVALID_COMMAND.getErrorCode()).build();
          responseObserver.onNext(resultRes);
          responseObserver.onCompleted();
          return;
      }

      CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
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

      BasePlaySession basePlaySessionResult = slotGameService.lightingSpin(request.getCId(), userInfo);

      int codeResult = basePlaySessionResult == null ? 1 : 0;
      ResponseStatusBotSG.Builder builder = ResponseStatusBotSG.newBuilder();
      builder.setC(String.valueOf(codeResult));
      builder.setSt(buildStateForBot(basePlaySessionResult));
      ResponseStatusBotSG resultRes = builder.build();
      responseObserver.onNext(resultRes);
//      log.info("After Next lightingSpin play {}", codeResult);
      responseObserver.onCompleted();
  }
  
  @Override
  public void botPowerUpSpin(BotPowerUpSpin request, StreamObserver<ResponseStatusBotSG> responseObserver) {
//      log.info("Start powerUpSpin play request={}!", request);
      LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
      logBuilder.cmdId(request.getCId())
          .actorId(request.getUId())
          .serviceId("")
          .psId("")
          .stateName("BotSlotGameGrpcServiceImpl.botPowerUpSpin")
          .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
          .message(request)
          .timeExe(0);
      LogsUtils.writeLogInfo(logBuilder.build());
      
//      UserJoinGame userInfo = joinGameRepository.get(userService.getServiceId(), request.getUId());
//      if(userInfo ==  null || !SlotGameConstant.BOT_TYPE.equals(userInfo.getUserType())) {
      if (!SlotGameConstant.BOT_TYPE.equalsIgnoreCase(request.getUd().getUserType())) {
          logBuilder.message("ERROR:  This service only uses for bot! " + request.getUd().getUserType())
              .timeExe(0);
          LogsUtils.writeLogError(logBuilder.build());
          ResponseStatusBotSG resultRes = ResponseStatusBotSG.newBuilder().setC(SlotGameError.INVALID_COMMAND.getErrorCode()).build();
          responseObserver.onNext(resultRes);
          responseObserver.onCompleted();
          return;
      }
      CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
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

      BasePlaySession basePlaySessionResult = slotGameService.powerUpSpin(request.getCId(), userInfo, request.getCOp());

      int codeResult = basePlaySessionResult == null ? 1 : 0;
      ResponseStatusBotSG.Builder builder = ResponseStatusBotSG.newBuilder();
      builder.setC(String.valueOf(codeResult));
      builder.setSt(buildStateForBot(basePlaySessionResult));
      ResponseStatusBotSG resultRes = builder.build();
      responseObserver.onNext(resultRes);
      responseObserver.onCompleted();
//      log.info("After Next powerUpSpin play {}", codeResult);
  }
  
  @Override
  public void botGamble(BotGambleBet request, StreamObserver<ResponseStatusBotSG> responseObserver) {
//      log.info("Start gamble request={}!", request);
//      log.info("gamble bet money: " + Money.of(request.getTalB()));
      LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
      logBuilder.cmdId(request.getCId())
          .actorId(request.getUId())
          .serviceId("")
          .psId("")
          .stateName("BotSlotGameGrpcServiceImpl.botGamble")
          .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
          .message(request)
          .timeExe(0);
      LogsUtils.writeLogInfo(logBuilder.build());
      
//      UserJoinGame userInfo = joinGameRepository.get(userService.getServiceId(), request.getUId());
//      if(userInfo ==  null || !SlotGameConstant.BOT_TYPE.equals(userInfo.getUserType())) {
        if (!SlotGameConstant.BOT_TYPE.equalsIgnoreCase(request.getUd().getUserType())) {
//          log.error("ERROR:  This service only use for bot in botResumeGameInfo play request:{} -- userInfo :{}!", request, userInfo);
          logBuilder.message("ERROR:  This service only uses for bot! " + request.getUd().getUserType())
              .timeExe(0);
          LogsUtils.writeLogError(logBuilder.build());
          ResponseStatusBotSG resultRes = ResponseStatusBotSG.newBuilder().setC(SlotGameError.INVALID_COMMAND.getErrorCode()).build();
          responseObserver.onNext(resultRes);
          responseObserver.onCompleted();
          return;
      }
        CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
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

      ResponseStatusBotSG.Builder builder = ResponseStatusBotSG.newBuilder();
      BasePlaySession basePlaySessionResult = slotGameService.gamble(request.getCId(), userInfo, request.getCOp(), request.getTalB());

      int codeResult = basePlaySessionResult == null ? 1 : 0;
      builder.setC(String.valueOf(codeResult));
      builder.setSt(buildStateForBot(basePlaySessionResult));
      responseObserver.onNext(builder.build());
//      log.info("After Next gamble game {}", codeResult);
      responseObserver.onCompleted();

  }
    
    private Struct buildStateForBot(BasePlaySession basePlaySession) {
        Struct struct = null;
        try {
            if(basePlaySession != null) {
                Object viewerObj = playSessionProjection.convertToFullViewModel(basePlaySession);
                struct = StructUtil.convertToStruct(viewerObj);
            } else {
                struct = Struct.newBuilder().build();
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            log.error(e.getMessage() , e);
        }
        return struct;
    }

}

@Data
@Accessors(fluent = true)
class BaseViewerBotObj { 
    private boolean isFinished;
    private boolean hasBonusGame;
    private boolean hasFreeGameGame;
    private boolean hasFreeOptionGame;
}


