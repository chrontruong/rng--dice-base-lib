package com.io.begstd.slot.rtp.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserUtil {

    public static String[] generateUsers(int totalUser, String userPrefix) {
        String[] userList = new String[totalUser];

        for (int i = 0; i < totalUser; i++) {
            try {
                userList[i] = userPrefix + i;
            } catch (Exception e) {
                log.error(e.getMessage() , e);
            }
        }
        return userList;
    }
}
