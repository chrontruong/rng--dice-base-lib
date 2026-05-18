package com.io.begstd.slot.services.external.impl;


import com.io.begstd.slot.grpc.playerviewstoreservice.*;
import com.io.begstd.slot.services.external.IPlayerViewStoreClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlayerViewStoreClientTestImpl implements IPlayerViewStoreClient {

    public ResponseStatus responseSuccess() {
        return ResponseStatus.newBuilder().setCode("0").build();
    }

    @Override
    public ResponseStatus registerState(StateRegisterRequest stateRegisterRequest) {
        return responseSuccess();
    }

    @Override
    public ListStateRegisterRequest getAllStateType(EmptyMessage emptyMessage) {
        return ListStateRegisterRequest.newBuilder().build();
    }

    @Override
    public ResponseStatus subscribeJackpot(JackpotSubscriptionRequest jackpotSubscriptionRequest) {
        return responseSuccess();
    }

    @Override
    public ListJackpotSubscriptionRequest getAllSubscribeJackpot(EmptyMessage emptyMessage) {
        return ListJackpotSubscriptionRequest.newBuilder().build();
    }

    @Override
    public ResponseStatus addUserToGroup(AddUserToGroupRequest addUserToGroupRequest) {
        return responseSuccess();
    }

    @Override
    public ResponseStatus addUserToGroupTrial(AddUserToGroupRequest addUserToGroupRequest) {
        return responseSuccess();
    }

    @Override
    public ResponseStatus removeUserFromGroup(RemoveUserFromGroupRequest removeUserFromGroupRequest) {
        return responseSuccess();
    }

    @Override
    public ResponseStatus updateState(StateUpdateRequest stateUpdateRequest) {
        return responseSuccess();
    }

    @Override
    public ResponseStatus updateLatestState(StateUpdateRequest stateUpdateRequest) {
        return responseSuccess();
    }

    @Override
    public ResponseStatus updateMultiStates(MultiStateUpdateRequest multiStateUpdateRequest) {
        return responseSuccess();
    }

    @Override
    public ResponseStatus clearState(StateClearRequest stateClearRequest) {
        return responseSuccess();
    }

    @Override
    public ResponseStatus clearAllState(StateClearAllRequest stateClearAllRequest) {
        return responseSuccess();
    }

    @Override
    public ResponseStatus pushState(StateRequest stateRequest) {
        return responseSuccess();
    }

    @Override
    public ResponseStatus pushMultiState(MultiStatePushRequest multiStatePushRequest) {
        return responseSuccess();
    }

    @Override
    public ResponseStatus notifyMessage(DirrectlyStatePush dirrectlyStatePush) {
        return responseSuccess();
    }

    @Override
    public ResponseStatus pushError(ErrorRequest errorRequest) {
        return responseSuccess();
    }

    @Override
    public ResponseStatus pushMessage(MessageRequest messageRequest) {
        return responseSuccess();
    }

    @Override
    public void shutDown() {

    }

}
