package com.io.begstd.slot.services.internal;

import com.io.begstd.slot.grpc.promotionservice.PromotionData;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.CommandIdHistory;
import com.io.begstd.slot.model.app.UserInfo;

public interface UserService {
    int joinGame(String commandId, UserInfo userInfo, PromotionData promotionData, int env);
    int leaveGame(String commandId, UserInfo userInfo);
    boolean isJoinedGame(String serviceId, UserInfo userInfo);

    BasePlaySession getPlaySession(String serviceId, UserInfo userInfo);
    void removePlaySession(BasePlaySession basePlaySession);
    void savePlaySession(BasePlaySession basePlaySession);

    void saveGamblePlaySession(BasePlaySession basePlaySession);
    void removeGamblePlaySession(BasePlaySession basePlaySession);
    BasePlaySession getGamblePlaySession(String serviceId, UserInfo userInfo);
    
    void saveCommandIdInRedis(CommandIdHistory commandIdHistory);
    CommandIdHistory getCommandIdInRedis(String commandId, String serviceId);
    String getServiceId();
    

}
