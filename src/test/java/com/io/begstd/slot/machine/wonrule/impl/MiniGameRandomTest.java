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

public class MiniGameRandomTest extends AbstractBaseSlotMockTest {

    @Autowired
    MiniGameRandom miniGameRandom;

    @Test
    public void getName() {
        assertEquals("bonusGameRandom", miniGameRandom.getName());
    }

    @Test
    public void testNoWon_NoBonus() {
        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S9, S6, S8, S10, S3},
                {S2, S5, S4, S9, S5},
                {S4, S4, S7, S4 , S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        ISlotMachineConfig config = basePlaySession.state() == GameState.NORMAL_GAME
                ? configForNormal
                : configForFree;
        runAndCheck(basePlaySession, matrix, config, 0, 0);
    }

    @Test
    public void testNoWon_OneBonus() {
        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {SR, S6, S8, S10, S3},
                {S2, S5, S4, S9, S5},
                {S4, S4, S7, S4 , S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.FREE_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        ISlotMachineConfig config = basePlaySession.state() == GameState.NORMAL_GAME
                ? configForNormal
                : configForFree;
        runAndCheck(basePlaySession, matrix, config, SR.symbol().paytable().get(0), 0);
    }

    @Test
    public void testNoWon_TwoBonus_OneReel() {
        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {SR, S6, S8, S10, S3},
                {SR, S5, S4, S9, S5},
                {S4, S4, S7, S4 , S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.FREE_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        ISlotMachineConfig config = basePlaySession.state() == GameState.NORMAL_GAME
                ? configForNormal
                : configForFree;
        runAndCheck(basePlaySession, matrix, config, SR.symbol().paytable().get(1), 0);
    }

    @Test
    public void testNoWon_TwoBonus() {
        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {SR, SR, S8, S10, S3},
                {SA, S5, S4, S9, S5},
                {S4, S4, S7, S4 , S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.BONUS_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        ISlotMachineConfig config = basePlaySession.state() == GameState.NORMAL_GAME
                ? configForNormal
                : configForFree;
        runAndCheck(basePlaySession, matrix, config, SR.symbol().paytable().get(1), 0);
    }

    @Test
    public void testWon_ThreeBonus() {
        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {SR, SR, S8, S10, S3},
                {SA, S5, SR, S9, S5},
                {S4, S4, S7, S4 , S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.BONUS_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        ISlotMachineConfig config = basePlaySession.state() == GameState.NORMAL_GAME
                ? configForNormal
                : configForFree;
        runAndCheck(basePlaySession, matrix, config, SR.symbol().paytable().get(2), config.numPlayTotalInBonusGame());
    }

    @Test
    public void testWon_ThreeBonus_one_reel() {
        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {SR, S5, S6, S10, S3},
                {SR, S5, S2, S9, S5},
                {SR, S4, S7, S4 , S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.BONUS_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        ISlotMachineConfig config = basePlaySession.state() == GameState.NORMAL_GAME
                ? configForNormal
                : configForFree;
        runAndCheck(basePlaySession, matrix, config, SR.symbol().paytable().get(2), config.numPlayTotalInBonusGame());
    }

    @Test
    public void testWon_FourBonus() {
        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {SR, SR, S6, S10, S3},
                {SR, S5, S2, S9, S5},
                {SR, S4, S7, S4 , S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.BONUS_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        ISlotMachineConfig config = basePlaySession.state() == GameState.NORMAL_GAME
                ? configForNormal
                : configForFree;
        runAndCheck(basePlaySession, matrix, config, SR.symbol().paytable().get(3), config.numPlayTotalInBonusGame());
    }

    @Test
    public void testWon_FiveBonus() {
        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {SR, SR, S6, S10, S3},
                {SR, S5, S2, S9, SR},
                {SR, S4, S7, S4 , S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.BONUS_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        ISlotMachineConfig config = basePlaySession.state() == GameState.NORMAL_GAME
                ? configForNormal
                : configForFree;
        runAndCheck(basePlaySession, matrix, config, SR.symbol().paytable().get(4), config.numPlayTotalInBonusGame());
    }

    @Test
    public void testWon_FullBonus() {
        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {SR, SR, SR, SR, SR},
                {SR, SR, SR, SR, SR},
                {SR, SR, SR, SR, SR}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.BONUS_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        ISlotMachineConfig config = basePlaySession.state() == GameState.NORMAL_GAME
                ? configForNormal
                : configForFree;
        runAndCheck(basePlaySession, matrix, config, SR.symbol().paytable().get(4), config.numPlayTotalInBonusGame());
    }

    private void runAndCheck(BasePlaySession basePlaySession, DataCell<Symbol>[][] matrix, ISlotMachineConfig config, int bonusGame, int bonusPlay){
        final CalculatePayoutArgs calculatePayoutArgs = buildCalculatePayoutArgs(basePlaySession.betDenom(), basePlaySession.bettingLines(), matrix, configForNormal, basePlaySession);
        final BasePlaySession basePlaySession1 = miniGameRandom.calculatorPayout(calculatePayoutArgs, basePlaySession);
        assertEquals(bonusGame, basePlaySession1.bonusGameTotal());
        assertEquals(bonusGame, basePlaySession1.bonusGameRemain());
        assertEquals(bonusPlay, basePlaySession1.bonusPlayRemain());

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
