package com.io.begstd.dice.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.io.begstd.dice.grpc.dicegame.ServiceDiceGameConfigResponse;
import com.io.begstd.dice.model.app.ActivePlayer;
import com.io.begstd.dice.services.internal.ActivePlayerService;
import com.io.begstd.dice.common.DiceGameConstant;
import com.io.begstd.dice.config.StartTestProperty;
import com.io.begstd.dice.grpc.dicegame.ActiveDicePlayerRequest;
import com.io.begstd.dice.grpc.dicegame.ActiveDicePlayerResponse;
import com.io.begstd.dice.grpc.dicegame.ActiveDicePlayerSG;
import com.io.begstd.dice.grpc.dicegame.GetTotalActiveDicePlayersReq;
import com.io.begstd.dice.grpc.dicegame.GetTotalActiveDicePlayersResp;
import com.io.begstd.dice.grpc.dicegame.HelperDiceServiceGrpc;
import com.io.begstd.dice.grpc.dicegame.ServiceDiceGameConfigRequest;
import com.io.begstd.dice.grpc.dicegame.ServiceDicePropertyRequest;
import com.io.begstd.dice.grpc.dicegame.ServiceDicePropertyResponse;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GRpcService
public class HelperGrpcServiceImpl extends HelperDiceServiceGrpc.HelperDiceServiceImplBase {

//    public final static String USER_TYPE = "USER";
//    public final static String BOT_TYPE = "BOT";
    @Autowired
    private ActivePlayerService activePlayerService;

    @Autowired
    private IHelperGameConfig helperExtend;

    @Autowired
    private StartTestProperty property;

    @Override
    public void getActivePlayers(ActiveDicePlayerRequest request, StreamObserver<ActiveDicePlayerResponse> responseObserver) {
        List<ActivePlayer> activePlayers = activePlayerService.getActivePlayers();
        ActiveDicePlayerResponse.Builder builder = ActiveDicePlayerResponse.newBuilder();

        for (ActivePlayer activePlayer : activePlayers) {
            ActiveDicePlayerSG.Builder activeBuilder = ActiveDicePlayerSG.newBuilder();
            activeBuilder.setPlayerId(activePlayer.playerId());
            activeBuilder.setStatus(activePlayer.status().getCode());
            builder.addActivePlayers(activeBuilder);
        }

        ActiveDicePlayerResponse resultRes = builder.build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
    }

    @SneakyThrows
    @Override
    public void getServiceGameConfig(ServiceDiceGameConfigRequest request, StreamObserver<ServiceDiceGameConfigResponse> responseObserver) {
        ObjectNode gameConfig = helperExtend.getServiceGameConfig();
        ServiceDiceGameConfigResponse response = ServiceDiceGameConfigResponse.newBuilder().setConfig(gameConfig.toString()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @SneakyThrows
    @Override
    public void getServiceProperty(ServiceDicePropertyRequest request, StreamObserver<ServiceDicePropertyResponse> responseObserver) {
        ObjectMapper objectMapper = new ObjectMapper();
        String property = objectMapper.writeValueAsString(this.property);
        ServiceDicePropertyResponse response = ServiceDicePropertyResponse.newBuilder().setProperty(property).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @SneakyThrows
    @Override
    public void getTotalActivePlayers(GetTotalActiveDicePlayersReq request, StreamObserver<GetTotalActiveDicePlayersResp> responseObserver) {
        GetTotalActiveDicePlayersResp.Builder resp = GetTotalActiveDicePlayersResp.newBuilder();

        List<ActivePlayer> activePlayers = activePlayerService.getActivePlayersByType(DiceGameConstant.USER_TYPE);
        List<ActivePlayer> activeBots = activePlayerService.getActivePlayersByType(DiceGameConstant.BOT_TYPE);

        resp.setTotalUsers(activePlayers.size());
        resp.setTotalBots(activeBots.size());

        responseObserver.onNext(resp.build());
        responseObserver.onCompleted();
    }
}
