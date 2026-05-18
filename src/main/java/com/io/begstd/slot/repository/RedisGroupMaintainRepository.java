package com.io.begstd.slot.repository;

import com.io.begstd.slot.model.app.GroupUserMaintaince;

public interface RedisGroupMaintainRepository {

    void save(String serviceId, GroupUserMaintaince userObj);

    GroupUserMaintaince get(String serviceId, String userID);

    void removeGroupMaintain(String serviceId, String userID);

    boolean isExistedUserId(String serviceId, String userId);

}
