package com.io.begstd.slot.utils.kafka.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class PlaySessionToKafka {
    private String playSessionId;
    private String commandId;
    private String state;
    private String betId;
    private double betAmt;
    private float betDenom; 
    private int freeGameRemain;
    private int freeGameTotal;
    private int bonusGameRemain;
    private int bonusGameTotal;
    private int bonusGamePlayRemain;
    private List<String> normalMatrixName;
    private List<Integer> normalGameTableFormat;
    private List<String> normalMatrixCode;
    private List<String> freeMatrixName;
    private List<Integer> freeGameTableFormat;
    private List<String> freeMatrixCode;
    private List<String> bonusMatrixCode;
    private List<String> bonusMatrixName;
    private List<Integer> bonusGameTableFormat;
    private List<String> respinMatrixName;
    private List<Integer> respinGameTableFormat;
    private List<String> respinMatrixCode;
    
    private double totalWinAmount;
    private double normalWinAmount;
    private double freeGameWinAmount;
    private double bonusGameWinAmount;
    private double respinGameWinAmount;
    private double jackpotWinAmount;
    private double lastWinAmount;
    
    private int selectedOption;
    private boolean isFinish;
    private double multiplier;
    private int wonWildCount;
    
    private String paylineNormal;
    private String latestPaylines;
    private List<JackpotInfoKafka> jackpotInfo;
    // change object to list in 4.0.0
    private List<JackpotInfoKafka> latestJackpotInfo;
    private String lastJPLine;
    
    private String messageWallet;
    private String bettingLines;

    //for promotion
    private int promotionRemain;
    private int promotionTotal;
    private String promotionCode;
    private String promotionBetId;
    //send jpId and Jackpot Amount at plusing progressive
    private String jpInfoAmt;

    // event
//    private QuestLog quest;
    private int wo;
    private String currency;
    private String lang;
}
