package com.io.begstd.dice.utils.kafka.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class LogInfo {
    
//    private PayLoad payLoad;
    //result value is JSon of playSession
    private PlaySessionToKafka result;
}
