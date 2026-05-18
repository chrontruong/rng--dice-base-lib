package com.io.begstd.slot.services.external.impl;

import com.io.begstd.slot.config.ExternalServiceEndPointConfiguration;
import com.io.begstd.slot.grpc.playerviewstoreservice.*;
import com.io.begstd.slot.projection.IPlaySessionProjection;
import com.io.begstd.slot.services.external.IPlayerViewStoreClient;
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

    private PlayerViewStoreServiceGrpc.PlayerViewStoreServiceBlockingStub stub = null;

    private static final ReentrantLock lock = new ReentrantLock();

    public PlayerViewStoreServiceGrpc.PlayerViewStoreServiceBlockingStub intial() {
        if (stub == null) {
            log.error("stub start is null: {}", stub == null);
            try {
                lock.lock();
                if (stub == null) {
                    log.error("stub before is null: {}", stub == null);
                    ManagedChannel channel = ManagedChannelBuilder.forAddress(externalServiceEndPointConfiguration.getPlayerViewStoreHostName(),
                            externalServiceEndPointConfiguration.getPlayerViewStoreHostPort()).usePlaintext().build();
                    stub = PlayerViewStoreServiceGrpc.newBlockingStub(channel);
                }
            } finally {
                log.error("stub after is null: {}", stub == null);
                lock.unlock();
            }
        }
        return stub;
    }

    @Override
    public ResponseStatus registerState(StateRegisterRequest stateRegisterRequest) {
        return intial().registerState(stateRegisterRequest);
    }

    @Override
    public ListStateRegisterRequest getAllStateType(EmptyMessage emptyMessage) {
        return intial().getAllStateType(emptyMessage);
    }

    @Override
    public ResponseStatus subscribeJackpot(JackpotSubscriptionRequest jackpotSubscriptionRequest) {
        return intial().subscribeJackpot(jackpotSubscriptionRequest);
    }

    @Override
    public ListJackpotSubscriptionRequest getAllSubscribeJackpot(EmptyMessage emptyMessage) {
        return intial().getAllSubscribeJackpot(emptyMessage);
    }

    @Override
    public ResponseStatus addUserToGroup(AddUserToGroupRequest addUserToGroupRequest) {
        return intial().addUserToGroup(addUserToGroupRequest);
    }

    @Override
    public ResponseStatus addUserToGroupTrial(AddUserToGroupRequest addUserToGroupRequest) {
        return intial().addUserToGroupTrial(addUserToGroupRequest);
    }

    @Override
    public ResponseStatus removeUserFromGroup(RemoveUserFromGroupRequest removeUserFromGroupRequest) {
        return intial().removeUserFromGroup(removeUserFromGroupRequest);
    }

    @Override
    public ResponseStatus updateState(StateUpdateRequest stateUpdateRequest) {
        return intial().updateState(stateUpdateRequest);
    }

    @Override
    public ResponseStatus updateLatestState(StateUpdateRequest stateUpdateRequest) {
        return intial().updateLatestState(stateUpdateRequest);
    }

    @Override
    public ResponseStatus updateMultiStates(MultiStateUpdateRequest multiStateUpdateRequest) {
        return intial().updateMultiStates(multiStateUpdateRequest);
    }

    @Override
    public ResponseStatus clearState(StateClearRequest stateClearRequest) {
        return intial().clearState(stateClearRequest);
    }

    @Override
    public ResponseStatus clearAllState(StateClearAllRequest stateClearAllRequest) {
        return intial().clearAllState(stateClearAllRequest);
    }

    @Override
    public ResponseStatus pushState(StateRequest stateRequest) {
        return intial().pushState(stateRequest);
    }

    @Override
    public ResponseStatus pushMultiState(MultiStatePushRequest multiStatePushRequest) {
        return intial().pushMultiState(multiStatePushRequest);
    }

    @Override
    public ResponseStatus notifyMessage(DirrectlyStatePush dirrectlyStatePush) {
        return intial().pushStateDirrectly(dirrectlyStatePush);
    }

    @Override
    public ResponseStatus pushError(ErrorRequest errorRequest) {
        return intial().pushError(errorRequest);
    }

    @Override
    public ResponseStatus pushMessage(MessageRequest messageRequest) {
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
