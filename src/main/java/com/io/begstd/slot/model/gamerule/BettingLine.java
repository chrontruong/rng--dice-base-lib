package com.io.begstd.slot.model.gamerule;

import com.io.begstd.slot.model.domain.Money;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Accessors(fluent = true)
public class BettingLine implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6241676958202804919L;

    private PayLine line;

    private Money betMoney;

    private boolean isWin = false;

    public BettingLine() {

    }

    public BettingLine(PayLine line, Money betMoney) {
        this.line = line;
        this.betMoney = betMoney;
    }

    public void setWon() {
        isWin = true;
    }
}
