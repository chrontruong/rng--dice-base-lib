package com.io.begstd.slot.services.extension.common.impl;

import com.io.begstd.slot.machine.impl.BaseSlotMachineImpl;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.GameState;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.model.gamerule.InitJackpotChild;
import com.io.begstd.slot.services.extension.common.SpinExtension;
import com.io.begstd.slot.services.matrix.generate.IGenerateMatrix;
import com.io.begstd.slot.services.matrix.transform.ITransformMatrixService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class SpinExtensionImpl implements SpinExtension {

    @Override
    public BasePlaySession spin(BasePlaySession currentBasePlaySession,
                                Map<SlotConfigMode, ICommonSlotConfig> configMapper,
                                BaseSlotMachineImpl baseSlotMachine,
                                IGenerateMatrix normalGameGenerateMatrix,
                                ITransformMatrixService normalGameTransformMatrix,
                                IGenerateMatrix freeGameGenerateMatrix,
                                ITransformMatrixService freeGameTransformMatrix) {
        
        ISlotMachineConfig slotMachineConfigNormal = (ISlotMachineConfig) configMapper.get(SlotConfigMode.NORMAL);
        List<InitJackpotChild> initJackpotList = slotMachineConfigNormal.initJackpotList();
        BasePlaySession updatedBasePlaySession = null;
        if (currentBasePlaySession.state() == GameState.NORMAL_GAME) {
            updatedBasePlaySession = baseSlotMachine.playGame(
                    currentBasePlaySession.betDenom(),
                    currentBasePlaySession.bettingLines(),
                    currentBasePlaySession,
                    slotMachineConfigNormal,
                    initJackpotList,
                    normalGameGenerateMatrix,
                    normalGameTransformMatrix);
        } else if (currentBasePlaySession.state() == GameState.FREE_GAME) {
            ISlotMachineConfig slotMachineConfigFree = (ISlotMachineConfig) configMapper.get(SlotConfigMode.FREE);
            updatedBasePlaySession = baseSlotMachine.playGame(
                    currentBasePlaySession.betDenom(),
                    currentBasePlaySession.bettingLines(),
                    currentBasePlaySession,
                    slotMachineConfigFree,
                    initJackpotList,
                    freeGameGenerateMatrix,
                    freeGameTransformMatrix);
        } 
        return updatedBasePlaySession;
    }

    
}
