package com.io.begstd.slot.projection.impl;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.BigWinInfo;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.utils.BeanUtils;
import com.io.begstd.slot.utils.ConfigManager;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Accessors(fluent = true)
@NoArgsConstructor
public class BaseViewerObj {
    private String id;
//    private String uId; //userId; - removed by req
    private String sId; //serviceId;
    private String cId; //commandId;
    private String cIdt; // commandId for glt
    private int v = 0; //version = 0;
    private int s = 0; // state spin
    private String bId; //betId;
    private double wat; //winAmount;
    
    private List<String> jp; //jackpotPayline;
    
    private List<String> nMx; //normalGameMatrix;
//    private List<Integer> nTf; //normalGameTableFormat;
    private List<String> nLn; //normalGamePayLines;
    private double na; //normalGameWinAmount;

    private int fRe; //freeGameRemain;
    private int fta; //freeGameTotal;
    private List<String> fMx; //freeGameMatrix;
//    private List<Integer> fTf; //freeGameTableFormat;
    private List<String> fLn; //freeGamePayLines;
    private double fa; //freeGameWinAmount;

    private int rRe; //respinGameRemain;
    private int rta; //respinGameTotal;
    private List<String> rMx; //respinGameMatrix;
    private List<String> rLn; //respinGamePayLines;
    private double ra; //respinGameWinAmount;
    
    private int bRe; //bonusGameRemain;
    private int bTa; //bonusGameTotal;
    private int bpRe; //bonusPlayRemain;
    private double ba; //bonusGameWinAmount;
    private double baC; //bgWinAmtCurrent; // total winAmount of won bonus game

    private String isF; //isFinished = false;
    private String isT; //isTrialMode = false;
//    private double tW; //trialWallet;
    private List<String> tJ; //trialJpl;
    private List<String> tJW; //trialJplWin;
  //promotion service
    private String pro; //promotionRemain; promotionTotal
    // build time from projection to PVS
    private long bt;
    
    //field for normal and free response
    private List<String> mx;
    private List<String> pl; //freeGamePayLines;
    private double wa;
    private String wt; // winType of BigWin
    private String bwc; // big win config
    private int wo; // wallet option

    // event fields
//    private List<String> evl; // event list
//    private String qId; // quest id
//    private double we; // win event amount
//    private double wq; // win quest amount

    
    public BaseViewerObj(BasePlaySession basePlaySession) {
        
        this.bt(Instant.now().toEpochMilli());
        this.id(basePlaySession.uuid());
//        this.uId(basePlaySession.userId());
        this.sId(basePlaySession.serviceId());
        this.cId(basePlaySession.commandId());
        this.v(basePlaySession.version());

        this.bId(basePlaySession.betDenom().id());

        this.wat(basePlaySession.winAmount().value().doubleValue());
        
        //this.jp(basePlaySession.jackpotInfo());
        this.isF((basePlaySession.isFinished())?"T":"F");
        this.isT((basePlaySession.isTrialMode())?"T":"F");
        ConfigManager configManager = BeanUtils.getBean(ConfigManager.class);
        ISlotMachineConfig config = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
        BigWinInfo bigWin = basePlaySession.getBigWin(config.getBigWinInfos());
        if (bigWin != null) {
            this.wt(bigWin.winType());
            this.bwc(config.getBigWinInfos().stream()
                    .map(bigWinInfo -> "" + bigWinInfo.level())
                    .collect(Collectors.joining(",")));
        }
/*
        this.nMx(MatrixUtil.convertToMatrixCode1DByReel(basePlaySession.normalGameMatrix(), basePlaySession.normalGameTableFormat()));
        this.nTf(basePlaySession.normalGameTableFormat());
        this.nLn(basePlaySession.normalGamePayLines());
        this.na(basePlaySession.normalGameWinAmount().value().doubleValue());

        this.fRe(basePlaySession.freeGameRemain());
        this.fta(basePlaySession.freeGameTotal());
        this.fMx(MatrixUtil.convertToMatrixCode1DByReel(basePlaySession.freeGameMatrix(), basePlaySession.freeGameTableFormat()));
        this.fTf(basePlaySession.freeGameTableFormat());
        this.fLn(basePlaySession.freeGamePayLines());
        this.fa(basePlaySession.freeGameWinAmount().value().doubleValue());

        this.bRe(basePlaySession.bonusGameRemain());
        this.bTa(basePlaySession.bonusGameTotal());
        this.bpRe(basePlaySession.bonusPlayRemain());
        this.ba(basePlaySession.bonusGameWinAmount().value().doubleValue());
        this.baC(basePlaySession.bonusGameWinAmtCurrent()!= null? basePlaySession.bonusGameWinAmtCurrent().value().doubleValue():0);
        
        this.isF((basePlaySession.isFinished())?"T":"F");
        this.isT((basePlaySession.isTrialMode())?"T":"F");
        if (basePlaySession.promotionCode() != null && !basePlaySession.promotionCode().equals("")) {
            this.pro(basePlaySession.promotionRemain()+";"+basePlaySession.promotionTotal());
        }
        */
    }
}
