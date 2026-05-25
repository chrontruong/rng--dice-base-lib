package com.io.begstd.dice.model.gamerule;

import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.common.DiceGameConstant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Component
public class DenominationLevel {
    
    private int indx;

    private String id;

    private Money amount;
    
    private String jackpotID;
    
    private Money initJackpot;
    
    private List<Integer> env;
    
    private String curr = DiceGameConstant.CURRENCY_DEFAULT;

    public DenominationLevel(int indx, String id, Money amount, String jpID, Money initJp, List<Integer> env, String curr) {
        this.indx = indx;
        this.id = id;
        this.amount = amount;
        this.jackpotID = jpID;
        this.initJackpot = initJp;
        this.env = env;
        this.curr = curr;
    }
    
    public DenominationLevel copy() {
        Money copyAmount = Money.ZERO;
        List<Integer> envCopy = new ArrayList(this.env());
        if(Objects.nonNull(this.amount)) {
            copyAmount = copyAmount.add(this.amount);
        }
        return new DenominationLevel(this.indx, this.id, copyAmount, this.jackpotID, this.initJackpot, envCopy, this.curr);
    }
}
