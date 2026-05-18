package com.io.begstd.slot.rtp.util;

import com.io.begstd.slot.rtp.model.RtpResult;
import com.io.begstd.slot.rtp.task.RtpScenario;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j

public class TaskUtil {

    /**
     *
     * @param userName userId who play game
     * @param rtpScenario scenario to measure rtp rate.
     * @param loopCount number of spin each user will play
     * @return callable object corresponding to rtpScenario
     */

    public static Callable<RtpResult> convertRtpTaskToCallableTask(String userName, RtpScenario rtpScenario, long loopCount) {
            return () -> {
                try {
                    return rtpScenario.run(userName, loopCount);
                } catch (Exception ex) {
                    log.error("convertRtpTaskToCallableTask. ERROR : ",ex);
                    return null;
                }
            };
    }
}
