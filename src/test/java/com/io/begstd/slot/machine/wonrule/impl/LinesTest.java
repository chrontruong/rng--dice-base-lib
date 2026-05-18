
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class LinesTest extends AbstractBaseSlotMockTest {

    @Autowired
    Lines lines;
    @Test
    public void getName() {
        assertEquals("line", lines.getName());
    }

    @Test
    public void WinPlayLine1_Bet10_S4_Normal_Reel3() {

        String userID = "WinPlayLine1";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S5, S7, S9, SJ},
                {S4, S4, S4, S3, S5},
                {S9, S6, S8, S10, S3}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(2));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("1;3;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine1_Bet10_S4_Free_Reel3() {

        String userID = "WinPlayLine1";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S5, S7, S9, SJ},
                {S4, S4, S4, S3, S5},
                {S9, S6, S8, S10, S3}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.FREE_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(2));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("1;3;%s;4", wonMoney)));

    }

    @Test
    public void WinMultiPlayLine_Bet10_S2_Normal() {

        String userID = "WinPlayLine1";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S5, S7, S9, SJ},
                {S2, S2, S2, S3, S5},
                {S9, S6, S8, S10, S3}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money denomAmount = basePlaySession.betDenom().amount();
        List<String> payLines = new ArrayList<>();
        Money wonLine1 = denomAmount.multiply(S2.symbol().paytable().get(2));
        payLines.add(String.format("1;3;%s;2", wonLine1));
        Money wonLine4 = denomAmount.multiply(S2.symbol().paytable().get(1));
        payLines.add(String.format("4;2;%s;2", wonLine4));
        Money wonLine8 = denomAmount.multiply(S2.symbol().paytable().get(1));
        payLines.add(String.format("8;2;%s;2", wonLine8));
        Money wonLine15 = denomAmount.multiply(S2.symbol().paytable().get(2));
        payLines.add(String.format("15;3;%s;2", wonLine15));
        Money wonLine18 = denomAmount.multiply(S2.symbol().paytable().get(1));
        payLines.add(String.format("18;2;%s;2", wonLine18));
        Money wonLine19 = denomAmount.multiply(S2.symbol().paytable().get(1));
        payLines.add(String.format("19;2;%s;2", wonLine19));
        runAndCheck(basePlaySession, matrix, Money.ZERO.add(wonLine1).add(wonLine4).add(wonLine8).add(wonLine15).add(wonLine18).add(wonLine19) ,
                payLines);

    }

    @Test
    public void WinMultiPlayLine_Bet10_S2_Free() {

        String userID = "WinPlayLine1";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S5, S7, S9, SJ},
                {S2, S2, S2, S3, S5},
                {S9, S6, S8, S10, S3}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.FREE_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money denomAmount = basePlaySession.betDenom().amount();
        List<String> payLines = new ArrayList<>();
        Money wonLine1 = denomAmount.multiply(S2.symbol().paytable().get(2));
        payLines.add(String.format("1;3;%s;2", wonLine1));
        Money wonLine4 = denomAmount.multiply(S2.symbol().paytable().get(1));
        payLines.add(String.format("4;2;%s;2", wonLine4));
        Money wonLine8 = denomAmount.multiply(S2.symbol().paytable().get(1));
        payLines.add(String.format("8;2;%s;2", wonLine8));
        Money wonLine15 = denomAmount.multiply(S2.symbol().paytable().get(2));
        payLines.add(String.format("15;3;%s;2", wonLine15));
        Money wonLine18 = denomAmount.multiply(S2.symbol().paytable().get(1));
        payLines.add(String.format("18;2;%s;2", wonLine18));
        Money wonLine19 = denomAmount.multiply(S2.symbol().paytable().get(1));
        payLines.add(String.format("19;2;%s;2", wonLine19));
        runAndCheck(basePlaySession, matrix, Money.ZERO.add(wonLine1).add(wonLine4).add(wonLine8).add(wonLine15).add(wonLine18).add(wonLine19) ,
                payLines);

    }

    @Test
    public void WinPlayLine1_Bet30_S4_Normal_Reel4() {
        // Win line 1 reel 4, symbol 4
        String userID = "WinPlayLine1_Bet30_S4_Normal_Reel4";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S5, S7, S9, SJ},
                {S4, S4, S4, S4, S5},
                {S9, S6, S8, S10, S3}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("30"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(3));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("1;4;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine1_Bet40_S4_Normal_Reel5() {
        // Win line 1 reel 4, symbol 4
        String userID = "WinPlayLine1_Bet40_S4_Normal_Reel5";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S5, S7, S9, SJ},
                {S4, S4, S4, S4, S4},
                {S9, S6, S8, S10, S3}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("40"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("1;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine2_Bet10_S4_Normal_Reel3() {

        String userID = "WinPlayLine1";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S4, S4, S4, S9, SJ},
                {S2, S5, S7, S3, S5},
                {S9, S6, S8, S10, S3}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(2));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("2;3;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine2_Bet10_S4_Normal_Reel4() {

        String userID = "WinPlayLine2_Bet10_S4_Normal_Reel4";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S4, S4, S4, S4, SJ},
                {S2, S5, S7, S3, S5},
                {S9, S6, S8, S10, S3}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(3));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("2;4;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine2_Bet10_S4_Normal_Reel5() {

        String userID = "WinPlayLine2_Bet10_S4_Normal_Reel5";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S4, S4, S4, S4, S4},
                {S2, S5, S7, S3, S5},
                {S9, S6, S8, S10, S3}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("2;5;%s;4", wonMoney)));

    }


    @Test
    public void WinPlayLine3_Bet10_S4_Normal_Reel3() {

        String userID = "WinPlayLine3_Bet10_S4_Normal_Reel3";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S9, S6, S8, S10, S3},
                {S2, S5, S7, S3, S5},
                {S4, S4, S4, S9, SJ}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(2));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("3;3;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine3_Bet10_S4_Normal_Reel4() {

        String userID = "WinPlayLine3_Bet10_S4_Normal_Reel4";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S5, S7, S3, S5},
                {S9, S6, S8, S10, S3},
                {S4, S4, S4, S4, SJ}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(3));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("3;4;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine3_Bet10_S4_Normal_Reel5() {

        String userID = "WinPlayLine3_Bet10_S4_Normal_Reel5";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S5, S7, S3, S5},
                {S9, S6, S8, S10, S3},
                {S4, S4, S4, S4, S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("3;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine4_Bet10_S4_Normal_Reel3() {

        String userID = "WinPlayLine3_Bet10_S4_Normal_Reel3";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S4, S6, S8, S10, S3},
                {S2, S4, S7, S3, S5},
                {S9, S5, S4, S9, SJ}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(2));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("4;3;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine4_Bet10_S4_Normal_Reel4() {

        String userID = "WinPlayLine3_Bet10_S4_Normal_Reel4";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S4, S6, S8, S10, S3},
                {S2, S4, S7, S4, S5},
                {S9, S5, S4, S9, SJ}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(3));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("4;4;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine4_Bet10_S4_Normal_Reel5() {

        String userID = "WinPlayLine3_Bet10_S4_Normal_Reel5";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S4, S6, S8, S10, S4},
                {S2, S4, S7, S4, S5},
                {S9, S5, S4, S9, SJ}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("4;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine5_Bet10_S4_Normal_Reel3() {

        String userID = "WinPlayLine5_Bet10_S4_Normal_Reel3";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S9, S6, S4, S10, S3},
                {S2, S4, S7, S3 , S5},
                {S4, S5, S8, S9, SJ}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(2));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("5;3;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine5_Bet10_S4_Normal_Reel4() {

        String userID = "WinPlayLine5_Bet10_S4_Normal_Reel4";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S9, S6, S4, S10, S3},
                {S2, S4, S7, S4 , S5},
                {S4, S5, S8, S9, SJ}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(3));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("5;4;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine5_Bet10_S4_Normal_Reel5() {

        String userID = "WinPlayLine5_Bet10_S4_Normal_Reel5";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S9, S6, S4, S10, S3},
                {S2, S4, S7, S4 , S5},
                {S4, S5, S8, S9, S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("5;5;%s;4", wonMoney)));

    }


    @Test
    public void WinPlayLine6_Bet10_S4_Normal_Reel3() {

        String userID = "WinPlayLine6_Bet10_S4_Normal_Reel3";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S4, S7, S3 , S5},
                {S4, S5, S4, S9, SJ},
                {S9, S6, S8, S10, S3}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(2));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("6;3;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine6_Bet10_S4_Normal_Reel4() {

        String userID = "WinPlayLine6_Bet10_S4_Normal_Reel4";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S4, S7, S4 , S5},
                {S4, S5, S4, S9, SJ},
                {S9, S6, S8, S10, S3}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(3));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("6;4;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine6_Bet10_S4_Normal_Reel5() {

        String userID = "WinPlayLine6_Bet10_S4_Normal_Reel5";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S4, S7, S4 , S5},
                {S4, S5, S4, S9, S4},
                {S9, S6, S8, S10, S3}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("6;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine7_Bet10_S4_Normal_Reel5() {

        String userID = "WinPlayLine6_Bet10_S4_Normal_Reel5";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S9, S6, S8, S10, S3},
                {S4, S5, S4, S9, S4},
                {S2, S4, S7, S4 , S5}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("7;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine8_Bet10_S4_Normal_Reel5() {

        String userID = "WinPlayLine6_Bet10_S4_Normal_Reel5";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S4, S5, S4, S9, S4},
                {S2, S4, S7, S4 , S5},
                {S9, S6, S8, S10, S3}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("8;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine9_Bet10_S4_Normal_Reel5() {

        String userID = "WinPlayLine6_Bet10_S4_Normal_Reel5";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S9, S6, S8, S10, S3},
                {S2, S4, S7, S4 , S5},
                {S4, S5, S4, S9, S4}
                };

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("9;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine10_Bet10_S4_Normal_Reel5() {

        String userID = "WinPlayLine6_Bet10_S4_Normal_Reel5";
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S4, S4, S4 , S5},
                {S4, S5, S7, S9, S4},
                {S9, S6, S8, S10, S3}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("10;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine11_Bet10_S4_Normal_Reel5() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S9, S6, S8, S10, S3},
                {S4, S5, S7, S9, S4},
                {S2, S4, S4, S4 , S5}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("11;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine12_Bet10_S4_Normal_Reel5() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S9, S6, S8, S10, S3},
                {S2, S5, S4, S9, S5},
                {S4, S4, S7, S4 , S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("12;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine13_Bet10_S4_Normal_Reel5() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S4, S4, S7, S4 , S4},
                {S2, S5, S4, S9, S5},
                {S9, S6, S8, S10, S3}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("13;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine14_Bet10_S4_Normal_Reel5() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S9, S6, S8, S10, S3},
                {S2, S4, S4, S4 , S5},
                {S4, S5, S7, S9, S4}};

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("14;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine15_Bet10_S4_Normal_Reel5() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S4, S5, S7, S9, S4},
                {S2, S4, S4, S4 , S5},
                {S9, S6, S8, S10, S3}
        };

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("15;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine16_Bet10_S4_Normal_Reel5() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S4, S5, S4, S9, S4},
                {S9, S6, S8, S10, S3},
                {S2, S4, S7, S4 , S5}
        };

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("16;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine17_Bet10_S4_Normal_Reel5() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S4, S7, S4 , S5},
                {S9, S6, S8, S10, S3},
                {S4, S5, S4, S9, S4}
        };

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("17;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine18_Bet10_S4_Normal_Reel5() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S5, S4, S9, S5},
                {S4, S4, S7, S4 , S4},
                {S9, S6, S8, S10, S3}
        };

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("18;5;%s;4", wonMoney)));

    }

    @Test
    public void WinPlayLine19_Bet10_S4_Normal_Reel5() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S9, S6, S8, S10, S3},
                {S4, S4, S7, S4 , S4},
                {S2, S5, S4, S9, S5}
        };

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("19;5;%s;4", wonMoney)));

    }
    @Test
    public void WinPlayLine20_Bet10_S4_Normal_Reel5() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S5, S4, S9, S5},
                {S9, S6, S8, S10, S3},
                {S4, S4, S7, S4 , S4}
        };

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S4.symbol().paytable().get(4));
        runAndCheck(basePlaySession, matrix, wonMoney ,
                Arrays.asList(String.format("20;5;%s;%s", wonMoney, S4.symbol().code())));

    }

    @Test
    public void Win20PlayLine_Bet10_S2_Normal_Reel2_Full_MATRIX() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S2, S8, S9, S5},
                {S2, S2, S9, S10, S3},
                {S2, S2, S10, S8 , S6}
        };

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S2.symbol().paytable().get(1));
        Money winAll  = wonMoney.multiply(20);
        String code = S2.symbol().code();
        List<String> payLines = new ArrayList<>();
        for(int i = 0; i < 20; i++){
            payLines.add(String.format("%s;2;%s;%s", i+1,  wonMoney, code));
        }
        runAndCheck(basePlaySession, matrix, winAll, payLines);

    }

    @Test
    public void Win20PlayLine_Bet10_S2_Normal_Reel3_Full_MATRIX() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S2, S2, S9, S5},
                {S2, S2, S2, S10, S3},
                {S2, S2, S2, S8 , S6}
        };

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        int reel = 3;
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S2.symbol().paytable().get(reel-1));
        Money winAll  = wonMoney.multiply(20);
        String code = S2.symbol().code();
        List<String> payLines = new ArrayList<>();
        for(int i = 0; i < 20; i++){
            payLines.add(String.format("%s;%s;%s;%s", i+1, reel, wonMoney, code));
        }
        runAndCheck(basePlaySession, matrix, winAll, payLines);

    }

    @Test
    public void Win20PlayLine_Bet10_S2_Normal_Reel4_Full_MATRIX() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S2, S2, S2, S5},
                {S2, S2, S2, S2, S3},
                {S2, S2, S2, S2 , S6}
        };

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        int reel = 4;
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S2.symbol().paytable().get(reel-1));
        Money winAll  = wonMoney.multiply(20);
        String code = S2.symbol().code();
        List<String> payLines = new ArrayList<>();
        for(int i = 0; i < 20; i++){
            payLines.add(String.format("%s;%s;%s;%s", i+1, reel, wonMoney, code));
        }
        runAndCheck(basePlaySession, matrix, winAll, payLines);

    }

    @Test
    public void Win20PlayLine_Bet10_S2_Normal_Reel5_Full_MATRIX() {

        String userID = testName.getMethodName();
        DataCell<Symbol>[][] matrix = (DataCell<Symbol>[][]) new DataCell[][]{
                {S2, S2, S2, S2, S2},
                {S2, S2, S2, S2, S2},
                {S2, S2, S2, S2, S2}
        };

        final BasePlaySession.BasePlaySessionBuilder basePlaySessionBuilder = createPlaySession(userID, new SpinCmd().totalBetId("10"));
        basePlaySessionBuilder.state(GameState.NORMAL_GAME);
        final BasePlaySession basePlaySession = basePlaySessionBuilder.build();
        int reel = 5;
        Money wonMoney = basePlaySession.betDenom().amount().multiply(S2.symbol().paytable().get(reel-1));
        Money winAll  = wonMoney.multiply(20);
        String code = S2.symbol().code();
        List<String> payLines = new ArrayList<>();
        for(int i = 0; i < 20; i++){
            payLines.add(String.format("%s;%s;%s;%s", i+1, reel, wonMoney, code));
        }
        runAndCheck(basePlaySession, matrix, winAll, payLines);

    }

    private void runAndCheck(BasePlaySession basePlaySession, DataCell<Symbol>[][] matrix, Money amount, List<String> payLines){
        final CalculatePayoutArgs calculatePayoutArgs = buildCalculatePayoutArgs(basePlaySession.betDenom(), basePlaySession.bettingLines(), matrix, configForNormal, basePlaySession);
        final BasePlaySession basePlaySession1 = lines.calculatorPayout(calculatePayoutArgs, basePlaySession);
        if(basePlaySession1.state() == GameState.NORMAL_GAME){
            assertEquals(amount, basePlaySession1.normalGameWinAmount());
            assertEquals(payLines, basePlaySession1.normalGamePayLines());
        }else{
            assertEquals(amount, basePlaySession1.freeGameWinAmount());
            assertEquals(payLines, basePlaySession1.freeGamePayLines());
        }

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
