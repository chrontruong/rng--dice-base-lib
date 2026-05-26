package com.io.begstd.dice.game;

import com.google.protobuf.Struct;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.model.config.CurrencyType;
import com.io.begstd.dice.model.config.DiceMachineConfig;
import com.io.begstd.dice.model.wallet.WalletOption;
import com.io.begstd.dice.projection.IPlaySessionProjection;
import com.io.begstd.dice.services.internal.ActivePlayerService;
import com.io.begstd.dice.services.internal.DiceGameService;
import com.io.begstd.dice.services.internal.UserService;
import com.io.begstd.dice.utils.StructUtil;
import com.io.begstd.dice.command.SpinCmd;
import com.io.begstd.dice.common.DiceGameConstant;
import com.io.begstd.dice.common.DiceGameError;
import com.io.begstd.dice.grpc.dicebot.DiceBotNormalSpin;
import com.io.begstd.dice.grpc.dicebot.DiceBotPlayerInfo;
import com.io.begstd.dice.grpc.dicebot.DiceBotResumeSpin;
import com.io.begstd.dice.grpc.dicebot.BotDiceGameServiceGrpc;
import com.io.begstd.dice.grpc.dicebot.DiceResponseStatusBotSG;
import io.grpc.stub.StreamObserver;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@GRpcService
public class BotDiceGameGrpcServiceImpl extends BotDiceGameServiceGrpc.BotDiceGameServiceImplBase {
//    public final static String BOT_USER_TYPE = "BOT";
    @Autowired
    private DiceGameService slotGameService;

    @Autowired
    private IPlaySessionProjection playSessionProjection;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivePlayerService activePlayerService;

    @Autowired
    private DiceMachineConfig configNormal;

  //=============================================BOT==========================================================
  @Override
  public void botJoinGame(DiceBotPlayerInfo player, StreamObserver<DiceResponseStatusBotSG> responseObserver) {
//      log.info("Start join game - player={}!", player);
      LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
      logBuilder.cmdId(player.getCId())
          .actorId(player.getUId())
          .stateName("BotDiceGameGrpcServiceImpl.botJoinGame")
          .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
          .message(player);
      LogsUtils.writeLogInfo(logBuilder.build());

      if(!DiceGameConstant.BOT_TYPE.equals(player.getUd().getUserType())) {
//        log.error("ERROR:  This service only use for bot in botPlayNormalSpin play request:{} -- userInfo :{}!", request, userInfo);
        logBuilder.message("ERROR:  This service only uses for bot! " + player)
            .timeExe(0);
        LogsUtils.writeLogError(logBuilder.build());

          DiceResponseStatusBotSG resultRes = DiceResponseStatusBotSG.newBuilder().setC(DiceGameError.INVALID_COMMAND.getErrorCode()).build();
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

      DiceResponseStatusBotSG resultRes = DiceResponseStatusBotSG.newBuilder().setC(String.valueOf(result)).build();
      responseObserver.onNext(resultRes);
      responseObserver.onCompleted();
//      log.info("Join successfully");
  }
  @Override
  public void botPlayNormalSpin(DiceBotNormalSpin request, StreamObserver<DiceResponseStatusBotSG> responseObserver) {
//      log.info("Start botPlayNormalSpin play request={}!", request);
      LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
      logBuilder.cmdId(request.getCId())
          .actorId(request.getUId())
          .serviceId("")
          .psId("")
          .stateName("BotDiceGameGrpcServiceImpl.botPlayNormalSpin")
          .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
          .message(request)
          .timeExe(0);
      LogsUtils.writeLogInfo(logBuilder.build());

//      UserJoinGame userInfo = joinGameRepository.get(userService.getServiceId(), request.getUId());
//      if(userInfo ==  null || !BOT_USER_TYPE.equals(userInfo.getUserType())) {
      if (!DiceGameConstant.BOT_TYPE.equalsIgnoreCase(request.getUd().getUserType())) {
          logBuilder.message("ERROR:  This service only uses for bot! " + request.getUd().getUserType())
              .timeExe(0);
          LogsUtils.writeLogError(logBuilder.build());

          DiceResponseStatusBotSG resultRes = DiceResponseStatusBotSG.newBuilder().setC(DiceGameError.INVALID_COMMAND.getErrorCode()).build();
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
          DiceResponseStatusBotSG.Builder builder = DiceResponseStatusBotSG.newBuilder();
          builder.setC(String.valueOf(codeResult));
          builder.setSt(buildStateForBot(basePlaySessionResult));
          DiceResponseStatusBotSG resultRes = builder.build();
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

          DiceResponseStatusBotSG resultRes = DiceResponseStatusBotSG.newBuilder().setC(String.valueOf(1)).build();
          responseObserver.onNext(resultRes);
          responseObserver.onCompleted();
      }
  }

  @Override
  public void botResumeGameInfo(DiceBotResumeSpin request, StreamObserver<DiceResponseStatusBotSG> responseObserver) {

      LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
      logBuilder.cmdId(request.getCId())
          .actorId(request.getUId())
          .serviceId("")
          .psId("")
          .stateName("BotDiceGameGrpcServiceImpl.botResumeGameInfo")
          .stepName("gRPC Input").owner(LogMessage.OWNER_GRPC)
          .message(request)
          .timeExe(0);
      LogsUtils.writeLogInfo(logBuilder.build());

//      log.info("Start botResumeGameInfo game request={}!", request);
//      UserJoinGame userInfo = joinGameRepository.get(userService.getServiceId(), request.getUId());
//      if(userInfo ==  null || !DiceGameConstant.BOT_TYPE.equals(userInfo.getUserType())) {
      if (!DiceGameConstant.BOT_TYPE.equalsIgnoreCase(request.getUd().getUserType())) {
          logBuilder.message("ERROR:  This service only uses for bot! " + request.getUd().getUserType())
              .timeExe(0);
          LogsUtils.writeLogError(logBuilder.build());
//          log.error("ERROR:  This service only use for bot in botResumeGameInfo play request:{} -- userInfo :{}!", request, userInfo);
          DiceResponseStatusBotSG resultRes = DiceResponseStatusBotSG.newBuilder().setC(DiceGameError.INVALID_COMMAND.getErrorCode()).build();
          responseObserver.onNext(resultRes);
          responseObserver.onCompleted();
          return;
      }
      CurrencyType currency = configNormal.getAvailableCurrency(request.getUd().getCurrency());
      BasePlaySession basePlaySessionResult = userService.getPlaySession(userService.getServiceId(), UserInfo.builder().userId(request.getUId()).currency(currency.name()).build());
      int codeResult = 0;
      DiceResponseStatusBotSG.Builder builder = DiceResponseStatusBotSG.newBuilder();
      builder.setC(String.valueOf(codeResult));
      builder.setSt(buildStateForBot(basePlaySessionResult));
      DiceResponseStatusBotSG resultRes = builder.build();
      responseObserver.onNext(resultRes);
      responseObserver.onCompleted();
//      log.info("After Next botResumeGameInfo game {}", codeResult);
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
}


