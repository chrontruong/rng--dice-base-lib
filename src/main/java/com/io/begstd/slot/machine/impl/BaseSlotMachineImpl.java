
package com.io.begstd.slot.machine.impl;

import com.io.begstd.extension.loader.ExtensionLoader;
import com.io.begstd.extension.loader.ExtensionManagerImpl;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.machine.ISlotGameMachine;
import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.GameState;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.ISlotConfigGamble;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.model.gamerule.*;
import com.io.begstd.slot.model.matrix.MatrixScreen;
import com.io.begstd.slot.model.playsession.FreeGameProb;
import com.io.begstd.slot.model.playsession.ISymbol;
import com.io.begstd.slot.rules.matrix.pregenerate.IAfterGenerate;
import com.io.begstd.slot.rules.matrix.pregenerate.IDefineMatrixFormatRule;
import com.io.begstd.slot.rules.matrix.transform.IAfterTransform;
import com.io.begstd.slot.services.extension.common.SpinExtension;
import com.io.begstd.slot.services.internal.IRandomService;
import com.io.begstd.slot.services.matrix.generate.IGenerateMatrix;
import com.io.begstd.slot.services.matrix.transform.ITransformMatrixService;
import com.io.begstd.slot.utils.BeanUtils;
import com.io.begstd.slot.utils.MatrixUtil;
import org.apache.logging.log4j.util.Strings;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BaseSlotMachineImpl implements ISlotGameMachine {

    @Autowired
    @Qualifier("normalGameGenerateMatrix")
    protected IGenerateMatrix normalGameGenerateMatrix;

    @Autowired
    @Qualifier("normalGameTransformMatrix")
    protected ITransformMatrixService normalGameTransformMatrix;

    @Autowired
    @Qualifier("freeGameGenerateMatrix")
    protected IGenerateMatrix freeGameGenerateMatrix;

    @Autowired
    @Qualifier("freeGameTransformMatrix")
    protected ITransformMatrixService freeGameTransformMatrix;


    //@Autowired
    //private StartTestProperty startTestProperty;

    @Autowired(required = false)
    private IAfterGenerate afterGenerate;

    @Autowired(required = false)
    private IAfterTransform afterTransform;

    @Autowired
    private ExtensionLoader<ExtensionManagerImpl> extensionLoader;

    @Autowired (required = false)
    protected Map<String, IDefineMatrixFormatRule> defineMatrixFormatRuleMap;

    protected ThreadLocal<ExtensionManagerImpl> extensionManagerThreadLocal = new ThreadLocal<>();

    public void bindExtension(ExtensionManagerImpl extensionManagerImpl) {
        extensionManagerThreadLocal.set(extensionManagerImpl);
    }

    public void unBindExtension() {
        extensionManagerThreadLocal.remove();
    }

    public BasePlaySession spin(BasePlaySession currentBasePlaySession, Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        ExtensionManagerImpl extensionManager = extensionLoader.getExtensionManager();
        SpinExtension spinExtension  = extensionManager.spinExtension();
        BasePlaySession updatedBasePlaySession = spinExtension.spin(
                currentBasePlaySession,
                configMapper,
                this,
                normalGameGenerateMatrix,
                normalGameTransformMatrix,
                freeGameGenerateMatrix,
                freeGameTransformMatrix
        );

        writeLog(updatedBasePlaySession);

        return updatedBasePlaySession;
    }

    private IRandomService getRandomService() {
        return BeanUtils.getBean(IRandomService.class);
    }

    public BasePlaySession playGame(DenominationLevel betDemon,
                                    List<BettingLine> bettingLineList,
                                    BasePlaySession currentBasePlaySession,
                                    ISlotMachineConfig config,
                                    List<InitJackpotChild> initJackpotList,
                                    IGenerateMatrix generateMatrixService,
                                    ITransformMatrixService transformMatrixService) {

        //boolean startTest = startTestProperty.isStartTest();

        BasePlaySession updatedBasePlaySession = currentBasePlaySession.toBuilder().build();

        DataCell<Symbol>[][] originVerticalMatrix = null;
        List<Integer> tableFormat = this.defineMatrixFormat(updatedBasePlaySession, config);
        //log builder
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(updatedBasePlaySession.commandId())
                .actorId(updatedBasePlaySession.userId())
                .serviceId(updatedBasePlaySession.serviceId())
                .psId(updatedBasePlaySession.uuid())
                .stateName("BaseSlotMachineImpl")
                .stepName("playGame").owner(LogMessage.OWNER_GAME)
                .message("Test flag: " + (getRandomService() != null ? getRandomService().toString() : "randomService is NULL"))
                .timeExe(0);
        LogsUtils.writeLogInfo(logBuilder.build());

        if (getRandomService() != null) {
            String matrixCodes = (String) this.getRandomService().random("GAME_MATRIX", String.class, new Object[]{updatedBasePlaySession.userId()});
            if (Strings.isNotBlank(matrixCodes)) {
                Symbol[] symbols = config.getSymbolsForTest(matrixCodes.split(","));
                originVerticalMatrix = MatrixUtil.initVerticalDataCellMatrix(Symbol.class, tableFormat);
                int idx = 0;

                for(int i = 0; i < tableFormat.size(); ++i) {
                    for(int j = 0; j < (Integer) tableFormat.get(i); ++j) {
                        originVerticalMatrix[i][j] = DataCell.builder(Symbol.class).symbol(symbols[idx]).build();
                        ++idx;
                    }
                }

                MatrixUtil.logMatrixTest(originVerticalMatrix, "Using matrix custom here", tableFormat);
            }
        }

        if (originVerticalMatrix == null) {
            Pair<DataCell<Symbol>[][], List<Integer>> generatedMatrixData = generateMatrixService
                    .generateMatrix(updatedBasePlaySession, config);

            originVerticalMatrix = generatedMatrixData.getValue0();
            tableFormat = generatedMatrixData.getValue1();

        }

        if (afterGenerate != null) {
            updatedBasePlaySession = afterGenerate.doAfterGenerate(updatedBasePlaySession, originVerticalMatrix, config);
        }

        /*
         * listTransformedMatrix = [originHorizontalMatrix, matrixTransform1, matrixTransform2,...]
         *
         * or listTransformedMatrix = [originHorizontalMatrix]
         *
         */
        List<DataCell<Symbol>[][]> listTransformedMatrix = transformMatrixService
                .transformMatrix(originVerticalMatrix, updatedBasePlaySession, config);

        if (afterTransform != null) {
            updatedBasePlaySession = afterTransform.doAfterTransform(updatedBasePlaySession, listTransformedMatrix, config);
        }

        // convert list vertical matrix to list horizontal matrix
        for (int i = 0; i < listTransformedMatrix.size(); i++) {
            if (listTransformedMatrix.get(i) != null) {
                listTransformedMatrix.set(i, MatrixUtil.reverseMatrix(listTransformedMatrix.get(i)));
            }
        }

        MatrixUtil.logMatrix(listTransformedMatrix.get(0), "original", tableFormat);

        if (listTransformedMatrix.size() > 1) {
            for (int i = 1; i < listTransformedMatrix.size(); i++) {
                MatrixUtil.logMatrix(listTransformedMatrix.get(i), "transform " + i, tableFormat);
            }
        }

        // latest matrix
        DataCell<Symbol>[][] horizontalMatrix = this.getLatestMatrix(listTransformedMatrix);
        
        // update vertex index for all node in matrix - CLUSTER GAME
        horizontalMatrix = MatrixUtil.updateVertexInMatrix(horizontalMatrix, tableFormat);
        
        updatedBasePlaySession = updateMatrixInPlaySession(horizontalMatrix, listTransformedMatrix, tableFormat,
                updatedBasePlaySession);

        CalculatePayoutArgs calculatePayoutArgs = buildCalculatePayoutArgs(betDemon, bettingLineList, initJackpotList, horizontalMatrix,
                config, updatedBasePlaySession);

        return calculatePayoutAndUpdateRewardToPlaysesion(config, calculatePayoutArgs, updatedBasePlaySession);
    }

    protected CalculatePayoutArgs buildCalculatePayoutArgs(DenominationLevel betDemon,
                                                           List<BettingLine> bettingLineList, List<InitJackpotChild> initJackpotList,
                                                           DataCell<Symbol>[][] matrix, ISlotMachineConfig config,
                                                           BasePlaySession currentBasePlaySession) {

        FreeGameProb freeGameProb = (FreeGameProb) currentBasePlaySession.freeSpinProb();
        float multiplier = freeGameProb != null ? freeGameProb.wonMultiplier() : 1;

        return CalculatePayoutArgs.builder().matrixScreen(new MatrixScreen(matrix)).bettingLines(bettingLineList)
                .denomLevel(betDemon)
                .multiplier(multiplier)
                .initJackpotList(initJackpotList)
                /*
                
                .numSymbolWinBonusGame(config.numSymbolWinBonusGame())
                .numSymbolWinFreeGame(config.numSymbolWinFreeGame())
                .reelCheckForBonusGame(config.reelCheckForBonusGame())
                .reelCheckForFreeGame(config.reelCheckForFreeGame())
                .jackpotCount(config.jackpotLineSize())
                .numSymbolWinLightningGame(config.numSymbolWinLightningGame())
                .numPlayTotalInLightningGame(config.numPlayTotalInLightningGame())
                .reelCheckForLightningGame(config.reelCheckForLightningGame())
                .rightCheckPayLineSymbol(config.rightCheckPayLineSymbol())
                .lightningCodeSymbols(config.getLightningSymbolCodes())
                .reelCheckForPowerUpGame(config.reelCheckForPowerUpGame())
                */
                .config(config)
                .build();
    }

    /**
     * calculate amount total and awards based on ruleList in configuration
     *
     * @return Money
     */
    protected BasePlaySession calculatePayoutAndUpdateRewardToPlaysesion(ISlotMachineConfig slotConfig,
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
                        .stateName("BaseSlotMachineImpl")
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


    public BaseSlotMachineImpl() {
        super();
    }

    // listMatrixTransformed = [original matrix, matrixTransform1, matrixTransform2
    // != lastestMatrix]
    // lastestMatrix was removed from list
    protected BasePlaySession updateMatrixInPlaySession(DataCell<Symbol>[][] matrix, List<DataCell<Symbol>[][]> listMatrixTransformed,
                                                        List<Integer> tableFormat, BasePlaySession currentBasePlaySession) {
        BasePlaySession.BasePlaySessionBuilder<?, ?> builder = currentBasePlaySession.toBuilder();

        //matrix data cell to list matrix abstract data cell
        DataCell<ISymbol>[][] matrixAbstractDataCell = MatrixUtil.convertToAbstractMatrixDataCell(ISymbol.class, matrix);

        List<DataCell<ISymbol>[][]> listMatrixAbstractDataCell = new ArrayList<>();
        for (DataCell<Symbol>[][] matrixDataCell : listMatrixTransformed) {
            listMatrixAbstractDataCell.add(MatrixUtil.convertToAbstractMatrixDataCell(ISymbol.class, matrixDataCell));
        }

        if (currentBasePlaySession.state() == GameState.NORMAL_GAME) {
            builder.normalGameMatrix(matrixAbstractDataCell);
            builder.listNormalGameTransformMatrix(listMatrixAbstractDataCell);
            builder.normalGameTableFormat(tableFormat);
        } else if (currentBasePlaySession.state() == GameState.FREE_GAME) {
            builder.freeGameMatrix(matrixAbstractDataCell);
            builder.listFreeGameTransformMatrix(listMatrixAbstractDataCell);
            builder.freeGameTableFormat(tableFormat);
        }
        return builder.build();
    }

    /**
     * In listTransformedMatrix, latest matrix is last not null matrix.
     *
     * @param listTransformedMatrix
     * @return
     */
    protected DataCell<Symbol>[][] getLatestMatrix(List<DataCell<Symbol>[][]> listTransformedMatrix) {
        DataCell<Symbol>[][] latestMatrix = null;

        for (int i = listTransformedMatrix.size() - 1; i >= 0; i--) {
            if ((latestMatrix = listTransformedMatrix.get(i)) != null) {
                break;
            }
        }
        return latestMatrix;
    }

    protected void writeLog(BasePlaySession playSS) {
        StringBuffer logBuffer = new StringBuffer();
        logBuffer.append("State: ").append(playSS.state()).append(";");
        logBuffer.append("Bet Denomination: ").append(playSS.betDenom().amount()).append(";");
        logBuffer.append("Total Amount: ").append(playSS.winAmount()).append(";");
        logBuffer.append("Normal game Amount: ").append(playSS.normalGameWinAmount()).append(";");
        logBuffer.append("Free Spin:").append(playSS.freeGameRemain()).append(";");
        logBuffer.append("Free Spin Amount:").append(playSS.latestWinAmount()).append(";");
        logBuffer.append("Free Spin Total Amount:").append(playSS.freeGameWinAmount()).append(";");
        logBuffer.append("Mini game:").append(playSS.bonusGameRemain()).append(";");
        if (playSS.state() == GameState.NORMAL_GAME) {
            logBuffer.append("Normal Payline: ").append(playSS.normalGamePayLines());
        } else {
            logBuffer.append("Free Payline: ").append(playSS.freeGamePayLines());
        }

        //log builder
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(playSS.commandId())
                .actorId(playSS.userId())
                .serviceId(playSS.serviceId())
                .psId(playSS.uuid())
                .stateName("BaseSlotMachineImpl")
                .stepName("writeLog").owner(LogMessage.OWNER_GAME)
                .message(logBuffer.toString())
                .timeExe(0);
        LogsUtils.writeLogDebug(logBuilder.build());
    }

    //-------------------------------------------------------------------------------------
    //gamble play
    public BasePlaySession playGamble(BasePlaySession currentPlaySession, ISlotConfigGamble gambleConfig) {
        BasePlaySession updatedPlaySession = currentPlaySession.toBuilder().build();

        return calculatePayoutAndUpdateRewardToPlaysesionInGamble(gambleConfig, null, updatedPlaySession);
    }

    private BasePlaySession calculatePayoutAndUpdateRewardToPlaysesionInGamble(ISlotConfigGamble gambleConfig,
            CalculatePayoutArgs calculatePayoutArgs, BasePlaySession currentPlaySession) {

        List<WonRuleExtension> wonRules = extensionManagerThreadLocal.get().wonRuleExtensionManager()
                .getWonRules(gambleConfig.wonRules(), currentPlaySession.state());
        
        BasePlaySession updatedPlaySession = currentPlaySession;

        for (WonRuleExtension wRuleItem : wonRules) {
            if (Objects.isNull(wRuleItem)) {
                //log builder
                LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
                logBuilder.cmdId(updatedPlaySession.commandId())
                        .actorId(updatedPlaySession.userId())
                        .serviceId(updatedPlaySession.serviceId())
                        .psId(updatedPlaySession.uuid())
                        .stateName("BaseSlotMachineImpl")
                        .stepName("updatePlaySessionWithWonRule").owner(LogMessage.OWNER_GAME)
                        .message("Not exist rule name: " + wRuleItem)
                        .timeExe(0);
                LogsUtils.writeLogError(logBuilder.build());

                continue;
            }
            updatedPlaySession = wRuleItem.calculatorPayout(calculatePayoutArgs, updatedPlaySession);
        }
        return updatedPlaySession;
    }


    protected List<Integer> defineMatrixFormat(BasePlaySession basePlaySession, ISlotMachineConfig config) {
        String defineMatrixFormatRule = config.defineMatrixFormatRule();
        if (StringUtils.isEmpty(defineMatrixFormatRule)) {
            return config.tableFormat();
        } else {
            return this.defineMatrixFormatRuleMap.get(defineMatrixFormatRule).defineMatrixFormat(config, basePlaySession);
        }

    }
}
