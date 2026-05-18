package com.io.begstd.slot.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.io.begstd.slot.common.SlotGameConstant;
import com.io.begstd.slot.config.StartTestProperty;
import com.io.begstd.slot.grpc.slotgame.*;
import com.io.begstd.slot.model.app.ActivePlayer;
import com.io.begstd.slot.services.internal.ActivePlayerService;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GRpcService
public class HelperGrpcServiceImpl extends HelperServiceGrpc.HelperServiceImplBase {

//    public final static String USER_TYPE = "USER";
//    public final static String BOT_TYPE = "BOT";
    @Autowired
    private ActivePlayerService activePlayerService;

    @Autowired
    private IHelperGameConfig helperExtend;

    @Autowired
    private StartTestProperty property;

    @Override
    public void getActivePlayers(ActivePlayerRequest request, StreamObserver<ActivePlayerResponse> responseObserver) {
        List<ActivePlayer> activePlayers = activePlayerService.getActivePlayers();
        ActivePlayerResponse.Builder builder = ActivePlayerResponse.newBuilder();

        for (ActivePlayer activePlayer : activePlayers) {
            ActivePlayerSG.Builder activeBuilder = ActivePlayerSG.newBuilder();
            activeBuilder.setPlayerId(activePlayer.playerId());
            activeBuilder.setStatus(activePlayer.status().getCode());
            builder.addActivePlayers(activeBuilder);
        }

        ActivePlayerResponse resultRes = builder.build();
        responseObserver.onNext(resultRes);
        responseObserver.onCompleted();
    }

    @SneakyThrows
    @Override
    public void getServiceGameConfig(ServiceGameConfigRequest request, StreamObserver<ServiceGameConfigResponse> responseObserver) {
        ObjectNode gameConfig = helperExtend.getServiceGameConfig();
        ServiceGameConfigResponse response = ServiceGameConfigResponse.newBuilder().setConfig(gameConfig.toString()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @SneakyThrows
    @Override
    public void getServiceProperty(ServicePropertyRequest request, StreamObserver<ServicePropertyResponse> responseObserver) {
        ObjectMapper objectMapper = new ObjectMapper();
        String property = objectMapper.writeValueAsString(this.property);
        ServicePropertyResponse response = ServicePropertyResponse.newBuilder().setProperty(property).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    @SneakyThrows
    @Override
    public void getTotalActivePlayers(GetTotalActivePlayersReq request, StreamObserver<GetTotalActivePlayersResp> responseObserver) {
        GetTotalActivePlayersResp.Builder resp = GetTotalActivePlayersResp.newBuilder();

        List<ActivePlayer> activePlayers = activePlayerService.getActivePlayersByType(SlotGameConstant.USER_TYPE);
        List<ActivePlayer> activeBots = activePlayerService.getActivePlayersByType(SlotGameConstant.BOT_TYPE);
        
        resp.setTotalUsers(activePlayers.size());
        resp.setTotalBots(activeBots.size());

        responseObserver.onNext(resp.build());
        responseObserver.onCompleted();
    }
}
