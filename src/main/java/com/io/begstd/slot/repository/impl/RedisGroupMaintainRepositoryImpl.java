package com.io.begstd.slot.repository.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.model.app.GroupUserMaintaince;
import com.io.begstd.slot.repository.RedisGroupMaintainRepository;
import com.io.begstd.slot.utils.JsonParseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;

@Repository
public class RedisGroupMaintainRepositoryImpl implements RedisGroupMaintainRepository {

    private final static String GROUP_MAINTAINANCE = "GROUPMAINTAIN_";
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private HashOperations hashOperations;

    public RedisGroupMaintainRepositoryImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public void save(String serviceId, GroupUserMaintaince userGroup) {
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId("")
            .actorId(userGroup.prefixUser())
            .serviceId(userGroup.serviceId())
            .psId("")
            .stateName("RedisGroupMaintainRepositoryImpl.save")
            .owner(LogMessage.OWNER_GAME);
        
        try {
            this.hashOperations.put(GROUP_MAINTAINANCE + userGroup.serviceId(), userGroup.prefixUser(), 
                JsonParseUtils.serializeToJson(userGroup));
        } catch (Exception e) {
            logBuilder.stepName("ParseJSon")
                .message("Exception - ")
                .timeExe(0);
            LogsUtils.writeLogException(logBuilder.build(), e);
        }
        
        logBuilder
            .stepName("Save Group Maintaince").owner(LogMessage.OWNER_GAME)
            .message("Save " + userGroup.toString())
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
    }
    
    @Override
    public GroupUserMaintaince get(String serviceId, String userId) {

        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId("")
            .actorId(userId)
            .serviceId(serviceId)
            .psId("")
            .stateName("RedisGroupMaintainRepositoryImpl.get")
            .stepName("Get userGroup").owner(LogMessage.OWNER_GAME);
            
        GroupUserMaintaince userGroupObj = null;
        //check case for all
        String userGroup = (String)  this.hashOperations.get(GROUP_MAINTAINANCE + serviceId, "all");
        if (userGroup == null) {
            Map<String, String> entries = this.hashOperations.entries(GROUP_MAINTAINANCE + serviceId);
            for (String key : entries.keySet()) {
                if (userId.startsWith(key)) {
                    userGroup = entries.get(key);
                }
            }
//            userGroup = (String)  this.hashOperations.get(GROUP_MAINTAINANCE + serviceId, prefixUser);
        }
        
        if (userGroup != null) {
            try {
                userGroupObj = JsonParseUtils.deserializeFromJson(GroupUserMaintaince.class, userGroup);
                
                logBuilder
                    .stepName("Get End")
                    .message("User of " + userGroupObj.prefixUser())
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
                LogsUtils.writeLogDebug(logBuilder.build());
            
            } catch (Exception e) {
                logBuilder
                    .cmdId("")
                    .stepName("ParseJSException")
                    .message("Exception - ")
                    .timeExe(0);
                LogsUtils.writeLogException(logBuilder.build(), e);
            }
         }
        
        logBuilder.message("Get User Join Game" + (userGroupObj == null? null:userGroupObj.prefixUser()))
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
        return userGroupObj;
    }

    @Override
    public void removeGroupMaintain(String serviceId, String prefixUser) {

        long lStartTimeGolbal = Instant.now().toEpochMilli();
        
        this.hashOperations.delete(GROUP_MAINTAINANCE + serviceId, prefixUser);
        
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId("")
            .actorId(prefixUser)
            .serviceId(serviceId)
            .psId("")
            .stateName("RedisGroupMaintainRepositoryImpl.removeUserJoin")
            .stepName("Remove GroupMaintain").owner(LogMessage.OWNER_GAME)
            .message("Remove GroupMaintain")
            .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
    }

    @Override
    public boolean isExistedUserId(String serviceId, String userId) {
        return get(serviceId, userId) != null;
    }

   }
