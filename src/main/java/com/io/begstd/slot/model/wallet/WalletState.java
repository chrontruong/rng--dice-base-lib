package com.io.begstd.slot.model.wallet;

public class WalletState {
    public static final String PENDING = "Pending";
    public static final String RUNNING = "Running"; //using minus bet
    public static final String OPEN = "Open";
    public static final String OPENED = "Opened";

    public static final String BONUS = "Bonus"; //promotion
    public static final String DRAW = "Draw"; //winAmount = Bet
    public static final String WIN = "Win"; // winAmount > Bet
    public static final String LOSE = "Lose"; //winAmount < Bet
    public static final String CANCEL = "Cancel";
    public static final String FINISHED = "Finished";
    
}
