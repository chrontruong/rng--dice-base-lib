package com.io.begstd.slot.services.external;

import com.io.begstd.slot.grpc.playerviewstoreservice.*;

public interface IPlayerViewStoreClient {
    ResponseStatus registerState(StateRegisterRequest stateRegisterRequest);

    ListStateRegisterRequest getAllStateType(EmptyMessage emptyMessage);

    ResponseStatus subscribeJackpot(JackpotSubscriptionRequest jackpotSubscriptionRequest);

    ListJackpotSubscriptionRequest getAllSubscribeJackpot(EmptyMessage emptyMessage);

    ResponseStatus addUserToGroup(AddUserToGroupRequest addUserToGroupRequest);

    ResponseStatus addUserToGroupTrial(AddUserToGroupRequest addUserToGroupRequest);

    ResponseStatus removeUserFromGroup(RemoveUserFromGroupRequest removeUserFromGroupRequest);

    ResponseStatus updateState(StateUpdateRequest stateUpdateRequest);

    ResponseStatus updateLatestState(StateUpdateRequest stateUpdateRequest);

    ResponseStatus updateMultiStates(MultiStateUpdateRequest multiStateUpdateRequest);

    ResponseStatus clearState(StateClearRequest stateClearRequest);

    ResponseStatus clearAllState(StateClearAllRequest stateClearAllRequest);

    ResponseStatus pushState(StateRequest stateRequest);

    ResponseStatus pushMultiState(MultiStatePushRequest multiStatePushRequest);

    ResponseStatus notifyMessage(DirrectlyStatePush dirrectlyStatePush);

    ResponseStatus pushError(ErrorRequest errorRequest);

    ResponseStatus pushMessage(MessageRequest messageRequest);

    void shutDown();
}
