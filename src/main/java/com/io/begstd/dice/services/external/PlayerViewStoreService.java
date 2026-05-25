package com.io.begstd.dice.services.external;

import com.io.begstd.dice.grpc.playerviewstoreservice.DiceDirrectlyStatePush;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.Promotion;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.model.domain.Money;

import java.util.List;
import java.util.Map;

public interface PlayerViewStoreService {

//    boolean createPrivateChannel(String userId);


    boolean addUserToGroup(String commandId, String userId, String userType, String groupId, String serviceId, String stateType,
                           Map<String, Money> JACKPOTS, String userName, Money currentAmount, Promotion promotion,
                           BasePlaySession basePlaySession, List<String> betIdList, List<String> exBetIdList, List<String> errorList);

    boolean addUserToGroupWithExtraData(String commandId, String userId, String userType, String groupId, String serviceId, String stateType,
                                        Map<String, Money> JACKPOTS, String userName, Money currentAmount, Promotion promotion,
                                        BasePlaySession basePlaySession, List<String> betIdList, List<String> exBetIdList,
                                        List<String> errorList, List<String> exDataList, List<String> exCommonData);

    boolean removeUserFromGroup(String commandId, String userId, String userType, String groupId, String serviceId);

    boolean registerState(String serviceId, int intervalGame);

    boolean subscribeJackpot(String serviceId, List<String> jackPotList);

    boolean updateState(BasePlaySession basePlaySession);

    boolean updateLatestState(BasePlaySession basePlaySession, int isFull);

    boolean pushError(String serviceId, String userId, String userType, String commandId, List<String> errorList);

    boolean pushErrorHasMeta(String serviceId, String userId, String userType, String commandId, List<String> errorList, String metaData);

    boolean clearState(String serviceId, String userId);

    public boolean pushMessage(String serviceId, String userId, String userType, String commandId, String errorCode);

    boolean addUserToGroupTrial(String commandId, String userId, String groupId, String serviceId, String stateType,
                                Map<String, Money> JACKPOTS, String userName, Money currentAmount, Promotion promotion,
                                BasePlaySession basePlaySession, List<String> betIdList);


    boolean pushState(String serviceId, String userId, String userType, String commandId);

    boolean notifyMessage(String serviceId, UserInfo userId, String commandId, String event, DiceDirrectlyStatePush message);
}
