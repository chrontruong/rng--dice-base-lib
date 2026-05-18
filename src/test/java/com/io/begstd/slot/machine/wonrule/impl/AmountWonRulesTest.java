package com.io.begstd.slot.machine.wonrule.impl;

import com.io.begstd.slot.AbstractBaseSlotMockTest;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.GameState;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotMachineConfigForNormal;
import com.io.begstd.slot.model.gamerule.BettingLine;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.DenominationLevel;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.gamerule.SymbolType;
import com.io.begstd.slot.model.matrix.MatrixScreen;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AmountWonRulesTest extends AbstractBaseSlotMockTest {

    @Autowired
    @Qualifier("slotMachineConfigForNormal")
    private ISlotMachineConfig slotMachineConfig;

    @Autowired
    private AllWayToWin allWayToWin;

    private CalculatePayoutArgs calculatePayoutArgs;

    @Before
    public void before() {
        SlotMachineConfigForNormal configNormal = (SlotMachineConfigForNormal) slotMachineConfig;
        DataCell<Symbol> K = DataCell.builder(Symbol.class).symbol(configNormal.getSymbolByType(SymbolType.WILD)).build();
        DataCell<Symbol> R = DataCell.builder(Symbol.class).symbol(configNormal.getSymbolByType(SymbolType.BONUS)).build();
        DataCell<Symbol> A = DataCell.builder(Symbol.class).symbol(configNormal.getSymbolByType(SymbolType.SCATTER)).build();

        DataCell<Symbol> S1 = DataCell.builder(Symbol.class).symbol(configNormal.getSymbolByType(SymbolType.SYMBOL)).build();
        DataCell<Symbol> S2 = DataCell.builder(Symbol.class).symbol(configNormal.getSymbolByType(SymbolType.SYMBOL)).build();
        DataCell<Symbol> S3 = DataCell.builder(Symbol.class).symbol(configNormal.getSymbolByType(SymbolType.SYMBOL)).build();
        DataCell<Symbol> S4 = DataCell.builder(Symbol.class).symbol(configNormal.getSymbolByType(SymbolType.SYMBOL)).build();
        DataCell<Symbol> S5 = DataCell.builder(Symbol.class).symbol(configNormal.getSymbolByType(SymbolType.SYMBOL)).build();
        DataCell<Symbol> S6 = DataCell.builder(Symbol.class).symbol(configNormal.getSymbolByType(SymbolType.SYMBOL)).build();
        DataCell<Symbol> S7 = DataCell.builder(Symbol.class).symbol(configNormal.getSymbolByType(SymbolType.SYMBOL)).build();
        DataCell<Symbol> N = DataCell.builder(Symbol.class).build();

        @SuppressWarnings("unchecked")
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S3, S1, S4, S2},
                {S1, K, S3, S5, S7},
                {S4, S5, S2, S3, S6}};
        List<BettingLine> bettingLineList = null;
        DenominationLevel betDemon = configNormal.getDenominationLevels().get(0);
        calculatePayoutArgs = CalculatePayoutArgs.builder().matrixScreen(new MatrixScreen(matrix))
                .bettingLines(bettingLineList)
                .denomLevel(betDemon)
                .multiplier(1)
                .config(slotMachineConfig)
                .build();
    }

    @Test
    public void calculatePayoutOfAllWayToWin() {
        BasePlaySession.BasePlaySessionBuilder psBuilder = BasePlaySession.builder();
        psBuilder.state(GameState.NORMAL_GAME);
        BasePlaySession payoutResult = allWayToWin.calculatorPayout(calculatePayoutArgs, psBuilder.build());
        Assert.assertNotEquals(0, payoutResult.normalGameWinAmount().value().doubleValue());

    }

    @Test(expected = UnsupportedOperationException.class)
    public void givenUsingTheJdk_whenUnmodifiableListIsCreated_thenNotModifiable() {
        List<String> list = new ArrayList<String>(Arrays.asList("one", "two", "three"));
        List<String> unmodifiableList = Collections.unmodifiableList(list);
        unmodifiableList.add("four");
    }
}
