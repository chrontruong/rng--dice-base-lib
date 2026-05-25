package com.io.begstd.dice.services.external;

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

public interface IPlayerViewStoreClient {
    DiceResponseStatus registerState(DiceStateRegisterRequest stateRegisterRequest);

    DiceListStateRegisterRequest getAllStateType(DiceEmptyMessage emptyMessage);

    DiceResponseStatus subscribeJackpot(DiceJackpotSubscriptionRequest jackpotSubscriptionRequest);

    ListDiceJackpotSubscriptionRequest getAllSubscribeJackpot(DiceEmptyMessage emptyMessage);

    DiceResponseStatus addUserToGroup(DiceAddUserToGroupRequest addUserToGroupRequest);

    DiceResponseStatus addUserToGroupTrial(DiceAddUserToGroupRequest addUserToGroupRequest);

    DiceResponseStatus removeUserFromGroup(DiceRemoveUserFromGroupRequest removeUserFromGroupRequest);

    DiceResponseStatus updateState(DiceStateUpdateRequest stateUpdateRequest);

    DiceResponseStatus updateLatestState(DiceStateUpdateRequest stateUpdateRequest);

    DiceResponseStatus updateMultiStates(DiceMultiStateUpdateRequest multiStateUpdateRequest);

    DiceResponseStatus clearState(DiceStateClearRequest stateClearRequest);

    DiceResponseStatus clearAllState(DiceStateClearAllRequest stateClearAllRequest);

    DiceResponseStatus pushState(DiceStateRequest stateRequest);

    DiceResponseStatus pushMultiState(DiceMultiStatePushRequest multiStatePushRequest);

    DiceResponseStatus notifyMessage(DiceDirrectlyStatePush dirrectlyStatePush);

    DiceResponseStatus pushError(DiceErrorRequest errorRequest);

    DiceResponseStatus pushMessage(DiceMessageRequest messageRequest);

    void shutDown();
}
