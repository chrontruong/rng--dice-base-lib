package com.io.begstd.slot.model.args;

import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.gamerule.BettingLine;
import com.io.begstd.slot.model.gamerule.DenominationLevel;
import com.io.begstd.slot.model.gamerule.InitJackpotChild;
import com.io.begstd.slot.model.matrix.IMatrixScreen;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculatePayoutArgs {

    private IMatrixScreen matrixScreen;
    
    private List<InitJackpotChild> initJackpotList; //only normal
    private List<BettingLine> bettingLines;
    private DenominationLevel denomLevel;
    private float multiplier;
    /*
    private int numSymbolWinFreeGame;
    private int numSymbolWinBonusGame;
    private List<Integer> reelCheckForFreeGame;
    private List<Integer> reelCheckForBonusGame;
    private int jackpotCount;
    
    private boolean isWinFullLine;
    private List<Integer> reelCheckForLightningGame;
    private int numSymbolWinLightningGame;
    private int numPlayTotalInLightningGame;
    private List<ISymbol> rightCheckPayLineSymbol;
    private List<String> lightningCodeSymbols;
    private List<Integer> reelCheckForPowerUpGame;
    */
    private ISlotMachineConfig config;
}
