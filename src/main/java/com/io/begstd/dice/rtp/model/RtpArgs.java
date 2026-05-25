package com.io.begstd.dice.rtp.model;

import com.io.begstd.dice.common.DiceGameConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RtpArgs {
    private int totalUser = 1000;
    private long totalSpin = 1000;
    private String totalBet = "10";
    private int freeSpinOption = -1;
    private String uuid;
    private int needSteps = 0;
    private int modeDisplay = 0;
    private String betLines = "";
    private String currency = DiceGameConstant.CURRENCY_DEFAULT;
}
