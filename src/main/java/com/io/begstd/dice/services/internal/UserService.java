package com.io.begstd.dice.services.internal;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.CommandIdHistory;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.grpc.promotionservice.DicePromotionData;

public interface UserService {
    int joinGame(String commandId, UserInfo userInfo, DicePromotionData promotionData, int env);
    int leaveGame(String commandId, UserInfo userInfo);
    boolean isJoinedGame(String serviceId, UserInfo userInfo);

    BasePlaySession getPlaySession(String serviceId, UserInfo userInfo);
    void removePlaySession(BasePlaySession basePlaySession);
    void savePlaySession(BasePlaySession basePlaySession);

    void saveCommandIdInRedis(CommandIdHistory commandIdHistory);
    CommandIdHistory getCommandIdInRedis(String commandId, String serviceId);
    String getServiceId();


}
