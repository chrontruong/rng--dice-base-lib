package com.io.begstd.slot.services.matrix.transform.impl;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.TransformMatrixConfig;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.rules.matrix.transform.ITransformMatrixRule;
import com.io.begstd.slot.services.matrix.transform.ITransformMatrixService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseTransformMatrixService implements ITransformMatrixService {

    @Autowired (required = false)
    protected Map<String, ITransformMatrixRule> transformMatrixRuleMap;

    protected List<TransformMatrixConfig> loadCondfig(ISlotMachineConfig config) {
        return config.listTransformMatrixConfig();
    }

    @Override
    public final List<DataCell<Symbol>[][]> transformMatrix(DataCell<Symbol>[][] verticalMatrix,
                                                            BasePlaySession basePlaySession,
                                                            ISlotMachineConfig config) {
        // verticalMatrix == original matrix
        List<TransformMatrixConfig> listTransformConfig = loadCondfig(config);

        List<DataCell<Symbol>[][]> transformHistory = new ArrayList<>();
        transformHistory.add(verticalMatrix); // push original matrix at index 0
        
        DataCell<Symbol>[][] matrixTransformed ;
        
        for (TransformMatrixConfig transformConfig : listTransformConfig) {
            transformConfig.slotMachineConfig(config);

            matrixTransformed = transformMatrixRuleMap.get(transformConfig.name())
                    .transformMatrix(verticalMatrix, transformConfig, basePlaySession, config);
            if (matrixTransformed != null) {
                verticalMatrix = matrixTransformed;
            }
            transformHistory.add(matrixTransformed);
        }

        return transformHistory;
    }

}
