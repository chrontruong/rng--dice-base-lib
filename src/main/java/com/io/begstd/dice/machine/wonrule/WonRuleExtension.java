package com.io.begstd.dice.machine.wonrule;

import com.io.begstd.extension.Extension;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.args.CalculatePayoutArgs;

public interface WonRuleExtension extends Extension {
    BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession);
}
