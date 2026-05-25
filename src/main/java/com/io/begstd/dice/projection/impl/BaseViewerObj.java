package com.io.begstd.dice.projection.impl;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.BigWinInfo;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.utils.BeanUtils;
import com.io.begstd.dice.utils.ConfigManager;
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
    
    private double na; //normalGameWinAmount;

    private List<String> tJ; //trialJpl;
    private List<String> tJW; //trialJplWin;
  //promotion service
    private String pro; //promotionRemain; promotionTotal
    // build time from projection to PVS
    private long bt;
    
    //field for normal and free response
    private double wa;
    private String wt; // winType of BigWin
    private String bwc; // big win config
    private int wo; // wallet option
    
    public BaseViewerObj(BasePlaySession basePlaySession) {
        
        this.bt(Instant.now().toEpochMilli());
        this.id(basePlaySession.uuid());
        this.sId(basePlaySession.serviceId());
        this.cId(basePlaySession.commandId());
        this.v(basePlaySession.version());

        this.bId(basePlaySession.betDenom().id());

        this.wat(basePlaySession.winAmount().value().doubleValue());
        ConfigManager configManager = BeanUtils.getBean(ConfigManager.class);
        IDiceMachineConfig config = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
        BigWinInfo bigWin = basePlaySession.getBigWin(config.getBigWinInfos());
        if (bigWin != null) {
            this.wt(bigWin.winType());
            this.bwc(config.getBigWinInfos().stream()
                    .map(bigWinInfo -> "" + bigWinInfo.level())
                    .collect(Collectors.joining(",")));
        }
    }
}
