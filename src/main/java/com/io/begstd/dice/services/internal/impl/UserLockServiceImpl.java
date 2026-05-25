package com.io.begstd.dice.services.internal.impl;

import com.io.begstd.dice.services.internal.UserLockService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Primary
public class UserLockServiceImpl implements UserLockService {
    private ConcurrentHashMap<String, String> userMap;
    private String LOCK = "LOCK";
    
    @Override
    public boolean isLocked2Added(String serviceId, String userId) {
        String addSuccess = null;
        synchronized (LOCK) {
           if(userMap == null) {
             userMap  = new ConcurrentHashMap<String, String>();
           }
//           log.info("Lock "+ serviceId+"_"+userId);
           addSuccess =  userMap.putIfAbsent(serviceId+"_"+userId, serviceId+"_"+userId);
           return !(serviceId+"_"+userId).equals(addSuccess);
        }
    }
    
    @Override
    public boolean isLocked(String serviceId, String userId) {
        synchronized (LOCK) {
           if(userMap == null) {
             return false;
           }
           return userMap.contains(serviceId+"_"+userId);
        }
    }

    @Override
    public boolean unLocked(String serviceId,String userId) {
        synchronized (LOCK) {
            if(userMap != null) {
                userMap.remove(serviceId+"_"+userId);
            }
//            log.info("Unlock "+ serviceId+"_"+userId);
            return true;
        }
    }

}
