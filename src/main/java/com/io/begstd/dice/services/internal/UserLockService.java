package com.io.begstd.dice.services.internal;

public interface UserLockService {
    public boolean isLocked2Added(String serviceId, String userId);
    public boolean isLocked(String serviceId, String userId);
    public boolean unLocked(String serviceId, String userId);
}
