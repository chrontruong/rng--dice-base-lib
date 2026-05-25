package com.io.begstd.dice.utils.kafka.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

//const message = {
//        logType: "PlayerInfo",
//        requestId: "1fc5524a-f9bf-4135-8fc8-9cf283839d69",
//        requestType: "Command",
//        service: "Wallet Service",
//        name: "CreateWallet",
//        logInfo: {
//            payload: {"userId": 10},
//            result: {"step": 1, "value": 100}
//        }
//    };
@Data
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class KafkaMessage {
    private String logType;// PlayerInfo
    private String category;
    private String requestId; //commandID
    private String requestType; //Command string
    private String service; // serviceId  = kts_9999
    private String internal;//game id
    private String action; // normal|free|bonus
    private LogInfo logInfo;
    private long timeUTC;
    private String userId;
    private String userType;
    private String displayName;
    private String playSessionId;
    private String avatar;
    private String userIP;
    private String env; // moi truong user: 1: App; 2: iframe; 3: native app; default = "N/A"
    private String ssid;
}
