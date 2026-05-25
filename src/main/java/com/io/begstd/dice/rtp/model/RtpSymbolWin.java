package com.io.begstd.dice.rtp.model;

import com.io.begstd.dice.model.domain.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class RtpSymbolWin {
    private long count;
    private Money payWin = Money.ZERO;
    private String code;
    
    public void addRtpSymbolWin(RtpSymbolWin symbolWin) {
        this.count += symbolWin.count;
        this.payWin = this.payWin.add(symbolWin.getPayWin());
    }
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("SymbolWin { code="+code+"\n");
        buf.append(" payWin="+payWin+"\n");
        buf.append(" count="+count+"}\n");
        return buf.toString();
    }
}
