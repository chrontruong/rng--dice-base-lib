package com.io.begstd.dice.model.wallet;

public enum WalletOption {
    MAIN(0),
    PROMOTION(1);

    private int code;

    WalletOption(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static WalletOption parseWalletOption(int code) {
        for (WalletOption wo : values()) {
            if (wo.getCode() == code) {
                return wo;
            }
        }
        // default is MAIN
        return MAIN;
    }
}
