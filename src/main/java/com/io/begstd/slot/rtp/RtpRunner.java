package com.io.begstd.slot.rtp;

import com.io.begstd.slot.common.SlotGameConstant;
import com.io.begstd.slot.rtp.executor.RtpExecutor;
import com.io.begstd.slot.rtp.model.RtpArgs;
import com.io.begstd.slot.rtp.model.RtpResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("rtp")
public class RtpRunner implements CommandLineRunner {

    @Autowired(required = false)
    private RtpExecutor rtpExecutor;

    @Override
    public void run(String... args) throws Exception {
        if (args != null && args.length >= 8) {

            if ("rtp".equals(args[1])) {
                int totalUser = 100;
                long totalSpin = 1000000;
                String totalBet = "10";
                int freeSpinOption = -1;
                int needStep = 0;
                int modeDisplay = 1;
                String betLines = "all";
                String currency = SlotGameConstant.CURRENCY_DEFAULT;
                if (args.length >= 3) {
                    totalUser = Integer.parseInt(args[2]);
                }
                if (args.length >= 4) {
                    totalSpin = Integer.parseInt(args[3]);
                }
                if (args.length >= 5) {
                    totalBet = args[4];
                }
                if (args.length >= 6) {
                    freeSpinOption = Integer.parseInt(args[5]);
                }
                if (args.length >= 7) {
                    needStep = Integer.parseInt(args[6]);
                }
                if (args.length >= 8) {
                    modeDisplay = Integer.parseInt(args[7]);
                }
                //betLines
                if (args.length >= 9) {
                    betLines = args[8];
                }

                if (args.length >= 10) {
                    currency = args[9];
                }
                
                RtpArgs rtpArgs = new RtpArgs(totalUser, totalSpin, totalBet, freeSpinOption,
                        args[0], needStep, modeDisplay, betLines, currency);
                RtpResult rtpResult = new RtpResult();
                rtpExecutor.execute(rtpArgs, rtpResult);
                System.exit(1);
            }
        }
    }
}
