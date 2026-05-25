
package com.io.begstd.dice.machine.impl;

import com.io.begstd.extension.loader.ExtensionLoader;
import com.io.begstd.extension.loader.ExtensionManagerImpl;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.dice.machine.IDiceGameMachine;
import com.io.begstd.dice.machine.wonrule.WonRuleExtension;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.args.CalculatePayoutArgs;
import com.io.begstd.dice.model.config.ICommonDiceConfig;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.model.gamerule.DenominationLevel;
import com.io.begstd.dice.model.gamerule.InitJackpotChild;
import com.io.begstd.dice.services.extension.common.SpinExtension;
import com.io.begstd.dice.services.internal.IRandomService;
import com.io.begstd.dice.utils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BaseDiceMachineImpl implements IDiceGameMachine {

    @Autowired
    private ExtensionLoader<ExtensionManagerImpl> extensionLoader;

    protected ThreadLocal<ExtensionManagerImpl> extensionManagerThreadLocal = new ThreadLocal<>();

    public void bindExtension(ExtensionManagerImpl extensionManagerImpl) {
        extensionManagerThreadLocal.set(extensionManagerImpl);
    }

    public void unBindExtension() {
        extensionManagerThreadLocal.remove();
    }

    public BasePlaySession spin(BasePlaySession currentBasePlaySession, Map<DiceConfigMode, ICommonDiceConfig> configMapper) {
        ExtensionManagerImpl extensionManager = extensionLoader.getExtensionManager();
        SpinExtension spinExtension  = extensionManager.spinExtension();
        BasePlaySession updatedBasePlaySession = spinExtension.spin(
                currentBasePlaySession,
                configMapper,
                this
        );

        writeLog(updatedBasePlaySession);

        return updatedBasePlaySession;
    }

    private IRandomService getRandomService() {
        return BeanUtils.getBean(IRandomService.class);
    }

    public BasePlaySession playGame(DenominationLevel betDemon,
                                    BasePlaySession currentBasePlaySession,
                                    IDiceMachineConfig config,
                                    List<InitJackpotChild> initJackpotList) {

        BasePlaySession updatedBasePlaySession = currentBasePlaySession.toBuilder().build();

        //log builder
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(updatedBasePlaySession.commandId())
                .actorId(updatedBasePlaySession.userId())
                .serviceId(updatedBasePlaySession.serviceId())
                .psId(updatedBasePlaySession.uuid())
                .stateName("BaseDiceMachineImpl")
                .stepName("playGame").owner(LogMessage.OWNER_GAME)
                .message("Test flag: " + (getRandomService() != null ? getRandomService().toString() : "randomService is NULL"))
                .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());

        CalculatePayoutArgs calculatePayoutArgs = buildCalculatePayoutArgs(betDemon, initJackpotList, config, updatedBasePlaySession);

        return calculatePayoutAndUpdateRewardToPlaysesion(config, calculatePayoutArgs, updatedBasePlaySession);
    }

    protected CalculatePayoutArgs buildCalculatePayoutArgs(DenominationLevel betDemon, List<InitJackpotChild> initJackpotList,
                                                           IDiceMachineConfig config,
                                                           BasePlaySession currentBasePlaySession) {

        return CalculatePayoutArgs.builder()
                .denomLevel(betDemon)
                .initJackpotList(initJackpotList)
                .config(config)
                .build();
    }

    /**
     * calculate amount total and awards based on ruleList in configuration
     *
     * @return Money
     */
    protected BasePlaySession calculatePayoutAndUpdateRewardToPlaysesion(IDiceMachineConfig slotConfig,
                                                                         CalculatePayoutArgs calculatePayoutArgs,
                                                                         BasePlaySession currentBasePlaySession) {

        List<WonRuleExtension> wonRules = extensionManagerThreadLocal.get().wonRuleExtensionManager().getWonRules(slotConfig,
                currentBasePlaySession.state());

        BasePlaySession updatedBasePlaySession = currentBasePlaySession;

        for (WonRuleExtension wRuleItem : wonRules) {
            if (Objects.isNull(wRuleItem)) {
                //log builder
                LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
                logBuilder.cmdId(updatedBasePlaySession.commandId())
                        .actorId(updatedBasePlaySession.userId())
                        .serviceId(updatedBasePlaySession.serviceId())
                        .psId(updatedBasePlaySession.uuid())
                        .stateName("BaseDiceMachineImpl")
                        .stepName("updatePlaySessionWithWonRule").owner(LogMessage.OWNER_GAME)
                        .message("Not exist rule name: " + wRuleItem)
                        .timeExe(0);
                LogsUtils.writeLogError(logBuilder.build());

                continue;
            }
            updatedBasePlaySession = wRuleItem.calculatorPayout(calculatePayoutArgs, updatedBasePlaySession);
        }
        return updatedBasePlaySession;
    }


    public BaseDiceMachineImpl() {
        super();
    }

    protected void writeLog(BasePlaySession playSS) {
        StringBuffer logBuffer = new StringBuffer();
        logBuffer.append("State: ").append(playSS.state()).append(";");
        logBuffer.append("Bet Denomination: ").append(playSS.betDenom().amount()).append(";");
        logBuffer.append("Total Amount: ").append(playSS.winAmount()).append(";");
        logBuffer.append("Normal game Amount: ").append(playSS.normalGameWinAmount()).append(";");
        logBuffer.append("Free Spin Amount:").append(playSS.latestWinAmount()).append(";");

        //log builder
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(playSS.commandId())
                .actorId(playSS.userId())
                .serviceId(playSS.serviceId())
                .psId(playSS.uuid())
                .stateName("BaseDiceMachineImpl")
                .stepName("writeLog").owner(LogMessage.OWNER_GAME)
                .message(logBuffer.toString())
                .timeExe(0);
        LogsUtils.writeLogDebug(logBuilder.build());
    }

    //-------------------------------------------------------------------------------------
}
