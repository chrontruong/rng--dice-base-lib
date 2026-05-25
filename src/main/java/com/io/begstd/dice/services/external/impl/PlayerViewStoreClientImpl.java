package com.io.begstd.dice.services.external.impl;

import com.io.begstd.dice.projection.IPlaySessionProjection;
import com.io.begstd.dice.services.external.IPlayerViewStoreClient;
import com.io.begstd.dice.config.ExternalServiceEndPointConfiguration;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceAddUserToGroupRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceDirrectlyStatePush;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceEmptyMessage;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceErrorRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceJackpotSubscriptionRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.ListDiceJackpotSubscriptionRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceListStateRegisterRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceMessageRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceMultiStatePushRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceMultiStateUpdateRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DicePlayerViewStoreServiceGrpc;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceRemoveUserFromGroupRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceResponseStatus;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateClearAllRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateClearRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateRegisterRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateUpdateRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class PlayerViewStoreClientImpl implements IPlayerViewStoreClient {

    @Autowired
    private ExternalServiceEndPointConfiguration externalServiceEndPointConfiguration;

    @Autowired
    private IPlaySessionProjection playSessionProjection;

    private DicePlayerViewStoreServiceGrpc.DicePlayerViewStoreServiceBlockingStub stub = null;

    private static final ReentrantLock lock = new ReentrantLock();

    public DicePlayerViewStoreServiceGrpc.DicePlayerViewStoreServiceBlockingStub intial() {
        if (stub == null) {
            log.error("stub start is null: {}", stub == null);
            try {
                lock.lock();
                if (stub == null) {
                    log.error("stub before is null: {}", stub == null);
                    ManagedChannel channel = ManagedChannelBuilder.forAddress(externalServiceEndPointConfiguration.getPlayerViewStoreHostName(),
                            externalServiceEndPointConfiguration.getPlayerViewStoreHostPort()).usePlaintext().build();
                    stub = DicePlayerViewStoreServiceGrpc.newBlockingStub(channel);
                }
            } finally {
                log.error("stub after is null: {}", stub == null);
                lock.unlock();
            }
        }
        return stub;
    }

    @Override
    public DiceResponseStatus registerState(DiceStateRegisterRequest stateRegisterRequest) {
        return intial().registerState(stateRegisterRequest);
    }

    @Override
    public DiceListStateRegisterRequest getAllStateType(DiceEmptyMessage emptyMessage) {
        return intial().getAllStateType(emptyMessage);
    }

    @Override
    public DiceResponseStatus subscribeJackpot(DiceJackpotSubscriptionRequest jackpotSubscriptionRequest) {
        return intial().subscribeJackpot(jackpotSubscriptionRequest);
    }

    @Override
    public ListDiceJackpotSubscriptionRequest getAllSubscribeJackpot(DiceEmptyMessage emptyMessage) {
        return intial().getAllSubscribeJackpot(emptyMessage);
    }

    @Override
    public DiceResponseStatus addUserToGroup(DiceAddUserToGroupRequest addUserToGroupRequest) {
        return intial().addUserToGroup(addUserToGroupRequest);
    }

    @Override
    public DiceResponseStatus addUserToGroupTrial(DiceAddUserToGroupRequest addUserToGroupRequest) {
        return intial().addUserToGroupTrial(addUserToGroupRequest);
    }

    @Override
    public DiceResponseStatus removeUserFromGroup(DiceRemoveUserFromGroupRequest removeUserFromGroupRequest) {
        return intial().removeUserFromGroup(removeUserFromGroupRequest);
    }

    @Override
    public DiceResponseStatus updateState(DiceStateUpdateRequest stateUpdateRequest) {
        return intial().updateState(stateUpdateRequest);
    }

    @Override
    public DiceResponseStatus updateLatestState(DiceStateUpdateRequest stateUpdateRequest) {
        return intial().updateLatestState(stateUpdateRequest);
    }

    @Override
    public DiceResponseStatus updateMultiStates(DiceMultiStateUpdateRequest multiDiceStateUpdateRequest) {
        return intial().updateMultiStates(multiDiceStateUpdateRequest);
    }

    @Override
    public DiceResponseStatus clearState(DiceStateClearRequest stateClearRequest) {
        return intial().clearState(stateClearRequest);
    }

    @Override
    public DiceResponseStatus clearAllState(DiceStateClearAllRequest stateClearAllRequest) {
        return intial().clearAllState(stateClearAllRequest);
    }

    @Override
    public DiceResponseStatus pushState(DiceStateRequest stateRequest) {
        return intial().pushState(stateRequest);
    }

    @Override
    public DiceResponseStatus pushMultiState(DiceMultiStatePushRequest multiStatePushRequest) {
        return intial().pushMultiState(multiStatePushRequest);
    }

    @Override
    public DiceResponseStatus notifyMessage(DiceDirrectlyStatePush dirrectlyStatePush) {
        return intial().pushStateDirrectly(dirrectlyStatePush);
    }

    @Override
    public DiceResponseStatus pushError(DiceErrorRequest errorRequest) {
        return intial().pushError(errorRequest);
    }

    @Override
    public DiceResponseStatus pushMessage(DiceMessageRequest messageRequest) {
        return intial().pushMessage(messageRequest);
    }

    public void shutDown() {
        long shutDownAt = System.currentTimeMillis();
        try {
            lock.lock();
            if (stub != null && stub.getChannel() != null) {
                if (stub.getChannel() instanceof ManagedChannel) {
                    try {
                        ManagedChannel mChannel = ((ManagedChannel) stub.getChannel());
                        mChannel.shutdown();
                        if (!mChannel.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                            log.error("Threads didn't finish in 500 milis!");
                        }
                    } catch (InterruptedException e) {
                        log.error("ERROR", e);
                    } catch (Exception e) {
                        log.error("Shutdown channel is error!!!", e);
                    } finally {
                        log.error("Stop pvs channel is done!!!");
                        stub = null;
                    }
                }
            }
        } finally {
            lock.unlock();
            log.error("Time shutdown: {}", (System.currentTimeMillis() - shutDownAt));
        }
    }
}
