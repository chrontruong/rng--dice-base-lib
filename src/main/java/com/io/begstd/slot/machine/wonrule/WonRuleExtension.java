package com.io.begstd.slot.machine.wonrule;

import com.io.begstd.extension.Extension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;

public interface WonRuleExtension extends Extension {
    BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession);
}
