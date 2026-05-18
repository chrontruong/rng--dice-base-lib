package com.io.begstd.slot.services.extension.gameplay.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.common.SlotGameError;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.MiniGame;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.IMiniSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.SymbolBonusGame;
import com.io.begstd.slot.model.playsession.ISymbolBonusGame;
import com.io.begstd.slot.services.extension.gameplay.BonusGamePlayExtension;
import com.io.begstd.slot.services.internal.impl.MiniGameServiceImpl;
import com.io.begstd.slot.utils.GameUtils;
import com.io.begstd.slot.utils.MatrixUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BonusGamePlayExtensionImpl implements BonusGamePlayExtension{
    
    @Override
    public BasePlaySession playBonus(BasePlaySession basePlaySession, UserInfo userInfo, String commandId, int openCell,
                                     LogMessage.LogMessageBuilder logBuilder,
                                     Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        IMiniSlotConfig slotMachineConfigForMini = (IMiniSlotConfig) configMapper.get(SlotConfigMode.MINI);

        BasePlaySession.BasePlaySessionBuilder<?,?> builder = basePlaySession.toBuilder();
        int bonusPlayRemain = basePlaySession.bonusPlayRemain();
        List<ISymbolBonusGame> currentBaseReelOfBonusGame;
        
        List<Float> bonusGameMatrix = null; // list result bonus game's id.

        if (bonusPlayRemain == slotMachineConfigForMini.numPlayTotalInBonusGame()) {
            // Initial new bonus game session.
            bonusGameMatrix = initBonusMatrix(slotMachineConfigForMini);
            currentBaseReelOfBonusGame = new ArrayList<>(slotMachineConfigForMini.baseBonusSymbolReel());
            builder.bonusGameTableFormat(slotMachineConfigForMini.bonusGameTableFormat());
            
        } else {
            // existing bonus matrix
            bonusGameMatrix = GameUtils.convertBonusMatrixToListFloatValue(basePlaySession.bonusGameMatrix(),
                basePlaySession.bonusGameTableFormat());
            currentBaseReelOfBonusGame = new ArrayList<>(basePlaySession.baseReelBonusGameRemain());
        }
        int openedPos = countOpenMiniGame(bonusGameMatrix);

        // If reopen -> return 0.
        if (bonusGameMatrix.get(openCell) != MiniGameServiceImpl.DEFAULT_VALE) {
            logBuilder.stepName("Opened cell " + openCell)
                .owner(LogMessage.OWNER_GAME)
                .message("Error - "+ SlotGameError.CELL_OPENED_ALREADY)
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.CELL_OPENED_ALREADY, userInfo.userId(), basePlaySession.userType(), commandId);
        }
        if (openedPos < slotMachineConfigForMini.numPlayTotalInBonusGame()) {
            builder.decreaseBonusPlayRemain();
            
            MiniGame mini = new MiniGame();
            SymbolBonusGame resultMini = (SymbolBonusGame)mini.playMiniGame(currentBaseReelOfBonusGame);
            bonusGameMatrix.set(openCell, resultMini.value());
            
            builder.bonusGameMatrix(buildBonusGameMatrix(bonusGameMatrix, slotMachineConfigForMini));
        
            if(Objects.nonNull(resultMini.code())) {
                currentBaseReelOfBonusGame.remove(resultMini);
                builder.baseReelBonusGameRemain(currentBaseReelOfBonusGame);
                builder.latestSymbolBonusGameOpend(resultMini);
            }
        
            Money afterMiniAmount = builder.build().betDenom().amount().multiply(resultMini.value());
            builder.addBonusGameWinAmount(afterMiniAmount);
            
            bonusPlayRemain --;
            if (bonusPlayRemain == 0) {
                // player has already finished MiniGame
                builder.decreaseBonusGameRemain();
                if (builder.build().bonusGameRemain() > 0) {
                    builder.bonusPlayRemain(slotMachineConfigForMini.numPlayTotalInBonusGame());
                }
            }
            StringBuffer sbf = new StringBuffer();
            sbf.append("Result: ").append(resultMini.value()).append(", value:").append(afterMiniAmount).append(", bonusPlayRemain:").append(bonusPlayRemain)
                .append(", bonusRemain:").append(builder.build().bonusGameRemain());
            
            logBuilder.stepName("Mini result")
                .owner(LogMessage.OWNER_GAME)
                .message(sbf.toString());
        }
        return builder.build();
    }
    private int countOpenMiniGame(List<Float> miniMatrix) {
        int count = 0;
        for (float item : miniMatrix) {
            if (item != MiniGameServiceImpl.DEFAULT_VALE)
                count++;
        }
        return count;
    }
    
    private List<Float> initBonusMatrix(IMiniSlotConfig slotMachineConfigForMini) {
        List<Float> bonusMatrix = new ArrayList<Float>();
        for (int i = 0; i < slotMachineConfigForMini.bonusMatrixSize(); i++) {
            bonusMatrix.add(MiniGameServiceImpl.DEFAULT_VALE);
        }
        return bonusMatrix;
    }

    private DataCell<ISymbolBonusGame>[][] buildBonusGameMatrix(List<Float> bonusGameMatrix,
                                                                IMiniSlotConfig slotMachineConfigForMini) {
        List<Integer> tableFormat = slotMachineConfigForMini.bonusGameTableFormat();
        DataCell<ISymbolBonusGame>[][] bonusGameMatrixDataCell = MatrixUtil
                .reverseMatrix(MatrixUtil.initVerticalDataCellMatrix(ISymbolBonusGame.class, tableFormat));
        int count = 0;
        for (int col = 0; col < tableFormat.size(); col++) {
            for (int row = 0; row < tableFormat.get(col); row++) {
                if (count == bonusGameMatrix.size())
                    break;
                SymbolBonusGame item = slotMachineConfigForMini.getSymbolMiniByValue(bonusGameMatrix.get(count));
                bonusGameMatrixDataCell[row][col] = DataCell.builder(ISymbolBonusGame.class).symbol(item).build();
                count++;
            }
        }
        return bonusGameMatrixDataCell;
    }
    

}
