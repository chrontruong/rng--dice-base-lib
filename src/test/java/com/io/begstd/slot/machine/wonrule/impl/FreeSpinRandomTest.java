package com.io.begstd.slot.machine.wonrule.impl;

import com.io.begstd.slot.AbstractBaseSlotMockTest;
import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.GameState;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.BettingLine;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.DenominationLevel;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.matrix.MatrixScreen;
import com.io.begstd.slot.model.playsession.FreeGameProb;
import com.io.begstd.slot.utils.GameUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class FreeSpinRandomTest extends AbstractBaseSlotMockTest {

    @Autowired
    private FreeSpinRandom freeSpinRandom;

    @Test
    public void getName() {
        assertEquals("freeGameRandom", freeSpinRandom.getName());
    }

    @Test
    public void testNoWon_NoScatter() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S9, S6, S8, S10, S3},
                {S2, S5, S4, S9, S5},
                {S4, S4, S7, S4 , S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        runAndCheck(basePlaySession, matrix, 0);
    }

    @Test
    public void testNoWon_OneScatter() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S9, SA, S8, S10, S3},
                {S2, S5, S4, S9, S5},
                {S4, S4, S7, S4 , S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        runAndCheck(basePlaySession, matrix, SA.symbol().freespin().get(0));
    }

    @Test
    public void testNoWon_TwoScatter() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S9, SA, S8, S10, S3},
                {SA, S5, S4, S9, S5},
                {S4, S4, S7, S4 , S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        runAndCheck(basePlaySession, matrix, SA.symbol().freespin().get(1));
    }

    @Test
    public void testWin_ThreeScatter_No_Begin() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, SA, S8, S10, S3},
                {S2, S5, SA, SA, S5},
                {S4, S4, S7, S4 , S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        runAndCheck(basePlaySession, matrix, SA.symbol().freespin().get(2));
    }

    @Test
    public void testWin_ThreeScatter_OneReel() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, SA, S8, S10, S3},
                {S2, SA, S10, S2, S5},
                {S4, SA, S7, S4 , S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        runAndCheck(basePlaySession, matrix, SA.symbol().freespin().get(2));
    }

    @Test
    public void testWin_ThreeScatter() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {SA, SA, S8, S10, S3},
                {S2, S5, SA, S9, S5},
                {S4, S4, S7, S4 , S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        runAndCheck(basePlaySession, matrix, SA.symbol().freespin().get(2));
    }

    @Test
    public void testWin_FourScatter() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {SA, SA, S8, S10, S3},
                {S2, S5, SA, S9, S5},
                {S4, S4, S7, S4 , SA}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        runAndCheck(basePlaySession, matrix, SA.symbol().freespin().get(3));
    }

    @Test
    public void testWin_FiveScatter() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {SA, SA, S8, S10, S3},
                {S2, S5, SA, S9, SA},
                {S4, S4, S7, S4 , SA}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        runAndCheck(basePlaySession, matrix, SA.symbol().freespin().get(4));
    }

    @Test
    public void testWin_FullScatter() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {SA, SA, SA, SA, SA},
                {SA, SA, SA, SA, SA},
                {SA, SA, SA, SA, SA}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        runAndCheck(basePlaySession, matrix, SA.symbol().freespin().get(4));
    }

    private void runAndCheck(BasePlaySession basePlaySession, DataCell<Symbol>[][] matrix, int freeGame){
        final CalculatePayoutArgs calculatePayoutArgs = buildCalculatePayoutArgs(basePlaySession.betDenom(), basePlaySession.bettingLines(), matrix, configForNormal, basePlaySession);
        final BasePlaySession basePlaySession1 = freeSpinRandom.calculatorPayout(calculatePayoutArgs, basePlaySession);
        assertEquals(freeGame, basePlaySession1.freeGameTotal());
        assertEquals(freeGame, basePlaySession1.freeGameRemain());


    }

    private BasePlaySession.BasePlaySessionBuilder createPlaySession(String userId, SpinCmd cmd){
        UUID uuid = UUID.randomUUID();
        BasePlaySession.BasePlaySessionBuilder builder = BasePlaySession.builder();
        builder
                .uuid(uuid.toString())
                .userId(userId)
                .serviceId(this.configForNormal.serviceId())
                .commandId("cmd");

        DenominationLevel betDemon = cmd.toBetPerLineModel(this.configForNormal, builder.build());

        // bet per line
        List<BettingLine> bettingLines = cmd.toBettingLine(this.configForNormal, betDemon);
        if (Objects.isNull(bettingLines)) {
            builder.bettingLines(new ArrayList<>());
        } else {
            builder.bettingLines(new ArrayList<>(bettingLines));
        }
        Money bettingTotal = GameUtils.calculateBettingTotal(bettingLines);

        builder.betDenom(betDemon);
        builder.totalBet(bettingTotal);
        return builder;
    }

    private CalculatePayoutArgs buildCalculatePayoutArgs(DenominationLevel betDemon,
                                                         List<BettingLine> bettingLineList, DataCell<Symbol>[][] matrix, ISlotMachineConfig config,
                                                         BasePlaySession currentBasePlaySession) {
        FreeGameProb freeGameProb = (FreeGameProb) currentBasePlaySession.freeSpinProb();
        float multiplier = freeGameProb != null ? freeGameProb.wonMultiplier() : 1;
        return CalculatePayoutArgs.builder().matrixScreen(new MatrixScreen(matrix)).bettingLines(bettingLineList)
                .denomLevel(betDemon)
                .multiplier(multiplier)
                .config(config)
                .build();
    }
}
