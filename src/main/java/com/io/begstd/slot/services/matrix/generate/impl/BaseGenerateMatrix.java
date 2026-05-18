package com.io.begstd.slot.services.matrix.generate.impl;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.ReelGenerateConfig;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.rules.matrix.generate.reel.IGenerateReelRule;
import com.io.begstd.slot.rules.matrix.pregenerate.IDefineMatrixFormatRule;
import com.io.begstd.slot.rules.matrix.pregenerate.IPreGenerateMatrixRule;
import com.io.begstd.slot.services.matrix.generate.IGenerateMatrix;
import com.io.begstd.slot.utils.MatrixUtil;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

public class BaseGenerateMatrix implements IGenerateMatrix {

    @Autowired
    protected Map<String, IGenerateReelRule> generateReelRuleMap;

    @Autowired (required = false)
    protected Map<String, IPreGenerateMatrixRule> preGenerateMatrixRuleMap;

    @Autowired (required = false)
    protected Map<String, IDefineMatrixFormatRule> defineMatrixFormatRuleMap;

    public BaseGenerateMatrix() {
    }

    protected List<ReelGenerateConfig> loadReelConfig(ISlotMachineConfig config) {
        return config.listReelConfig();
    }

    protected Symbol[][] loadBaseReel(BasePlaySession currentBasePlaySession, ISlotMachineConfig config) {
        if(currentBasePlaySession.isTrialMode()) {
            return MatrixUtil.cloneMatrix(config.baseSymbolReelTrial());
        }
        return MatrixUtil.cloneMatrix(config.baseSymbolReel());
    }

    @Override
    public Pair<DataCell<Symbol>[][], List<Integer>> generateMatrix(BasePlaySession currentBasePlaySession,
                                                                    ISlotMachineConfig config) {

        List<Integer> tableFormat = this.defineMatrixFormat(currentBasePlaySession, config);

        DataCell<Symbol>[][] preVerticalMatrix = this.preGenerateMatrix(
                MatrixUtil.initVerticalDataCellMatrix(Symbol.class, tableFormat),
                currentBasePlaySession,
                config);

        Symbol[][] baseSymbolReel = this.loadBaseReel(currentBasePlaySession, config);

        DataCell<Symbol>[][] verticalMatrix = MatrixUtil.initDataCellMatrix(Symbol.class, tableFormat.size(), 0);;

        List<ReelGenerateConfig> listReelConfig = this.loadReelConfig(config);
        for (ReelGenerateConfig reelConfig : listReelConfig) {
            IGenerateReelRule generateReelRule = generateReelRuleMap.get(reelConfig.rule());
            verticalMatrix[reelConfig.reelIdx()] = generateReelRule.generateReel(
                    preVerticalMatrix[reelConfig.reelIdx()],
                    loadReelForIndex(baseSymbolReel, reelConfig.reelIdx())
            );
        }

        return new Pair<DataCell<Symbol>[][], List<Integer>>(verticalMatrix, tableFormat);
    }
    
    protected  Symbol[] loadReelForIndex(Symbol[][] baseSymbolReel, int index) {
        return baseSymbolReel[index];
    }

    protected List<Integer> defineMatrixFormat(BasePlaySession basePlaySession, ISlotMachineConfig config) {
        String defineMatrixFormatRule = config.defineMatrixFormatRule();
        if (StringUtils.isEmpty(defineMatrixFormatRule)) {
            return config.tableFormat();
        } else {
            return this.defineMatrixFormatRuleMap.get(defineMatrixFormatRule).defineMatrixFormat(config, basePlaySession);
        }

    }

    protected DataCell<Symbol>[][] preGenerateMatrix(DataCell<Symbol>[][] initVerticalMatrix,
                                                     BasePlaySession currentBasePlaySession,
                                                     ISlotMachineConfig config) {

        DataCell<Symbol>[][] preVerticalMatrix = initVerticalMatrix;

        List<String> listPreGenerateMatrixRule = config.listPreGenerateMatrixRule();

        for (String preGenerateRule : listPreGenerateMatrixRule) {
            preVerticalMatrix = preGenerateMatrixRuleMap.get(preGenerateRule).preGenerateMatrix(
                    currentBasePlaySession,
                    config,
                    MatrixUtil.cloneMatrix(preVerticalMatrix));
        }

        return preVerticalMatrix;
    }
}
