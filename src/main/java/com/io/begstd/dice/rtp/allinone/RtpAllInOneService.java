package com.io.begstd.dice.rtp.allinone;

import com.io.begstd.dice.rtp.model.RtpPlaySessionStore;

import java.util.List;
import java.util.Map;

public interface RtpAllInOneService {
    Map<String, ?> rtpAllInOneConfig();
    List<Integer> getNumberTimes();
    void addStore(RtpPlaySessionStore store);
    void addStore(Map<String, Object> store);
    void writeReport(String fileName, String path);
    default Object[] customizeData(Object[] data) {
        return data;
    };
}
