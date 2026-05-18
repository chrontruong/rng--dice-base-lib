package com.io.begstd.slot.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.slot.machine.impl.BaseSlotMachineImpl;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.services.matrix.generate.IGenerateMatrix;
import com.io.begstd.slot.services.matrix.transform.ITransformMatrixService;

import java.util.Map;

public interface SpinExtension extends Extension {
    
    default String getName() {
        return this.getClass().getName();
    }
    
    BasePlaySession spin(BasePlaySession currentBasePlaySession,
                         Map<SlotConfigMode, ICommonSlotConfig> configMapper,
                         BaseSlotMachineImpl baseSlotMachine,
                         IGenerateMatrix normalGameGenerateMatrix,
                         ITransformMatrixService normalGameTransformMatrix,
                         IGenerateMatrix freeGameGenerateMatrix,
                         ITransformMatrixService freeGameTransformMatrix);
}
