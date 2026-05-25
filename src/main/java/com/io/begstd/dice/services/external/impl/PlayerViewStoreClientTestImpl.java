package com.io.begstd.dice.services.external.impl;


import com.io.begstd.dice.services.external.IPlayerViewStoreClient;
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
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceRemoveUserFromGroupRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceResponseStatus;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateClearAllRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateClearRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateRegisterRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateRequest;
import com.io.begstd.dice.grpc.playerviewstoreservice.DiceStateUpdateRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlayerViewStoreClientTestImpl implements IPlayerViewStoreClient {

    public DiceResponseStatus responseSuccess() {
        return DiceResponseStatus.newBuilder().setCode("0").build();
    }

    @Override
    public DiceResponseStatus registerState(DiceStateRegisterRequest stateRegisterRequest) {
        return responseSuccess();
    }

    @Override
    public DiceListStateRegisterRequest getAllStateType(DiceEmptyMessage emptyMessage) {
        return DiceListStateRegisterRequest.newBuilder().build();
    }

    @Override
    public DiceResponseStatus subscribeJackpot(DiceJackpotSubscriptionRequest jackpotSubscriptionRequest) {
        return responseSuccess();
    }

    @Override
    public ListDiceJackpotSubscriptionRequest getAllSubscribeJackpot(DiceEmptyMessage emptyMessage) {
        return ListDiceJackpotSubscriptionRequest.newBuilder().build();
    }

    @Override
    public DiceResponseStatus addUserToGroup(DiceAddUserToGroupRequest addUserToGroupRequest) {
        return responseSuccess();
    }

    @Override
    public DiceResponseStatus addUserToGroupTrial(DiceAddUserToGroupRequest addUserToGroupRequest) {
        return responseSuccess();
    }

    @Override
    public DiceResponseStatus removeUserFromGroup(DiceRemoveUserFromGroupRequest removeUserFromGroupRequest) {
        return responseSuccess();
    }

    @Override
    public DiceResponseStatus updateState(DiceStateUpdateRequest stateUpdateRequest) {
        return responseSuccess();
    }

    @Override
    public DiceResponseStatus updateLatestState(DiceStateUpdateRequest stateUpdateRequest) {
        return responseSuccess();
    }

    @Override
    public DiceResponseStatus updateMultiStates(DiceMultiStateUpdateRequest multiStateUpdateRequest) {
        return responseSuccess();
    }

    @Override
    public DiceResponseStatus clearState(DiceStateClearRequest stateClearRequest) {
        return responseSuccess();
    }

    @Override
    public DiceResponseStatus clearAllState(DiceStateClearAllRequest stateClearAllRequest) {
        return responseSuccess();
    }

    @Override
    public DiceResponseStatus pushState(DiceStateRequest stateRequest) {
        return responseSuccess();
    }

    @Override
    public DiceResponseStatus pushMultiState(DiceMultiStatePushRequest multiStatePushRequest) {
        return responseSuccess();
    }

    @Override
    public DiceResponseStatus notifyMessage(DiceDirrectlyStatePush dirrectlyStatePush) {
        return responseSuccess();
    }

    @Override
    public DiceResponseStatus pushError(DiceErrorRequest errorRequest) {
        return responseSuccess();
    }

    @Override
    public DiceResponseStatus pushMessage(DiceMessageRequest messageRequest) {
        return responseSuccess();
    }

    @Override
    public void shutDown() {

    }

}
