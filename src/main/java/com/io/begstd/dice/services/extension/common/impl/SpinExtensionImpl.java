package com.io.begstd.dice.services.extension.common.impl;

import com.io.begstd.dice.machine.impl.BaseDiceMachineImpl;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.GameState;
import com.io.begstd.dice.model.config.ICommonDiceConfig;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.model.gamerule.InitJackpotChild;
import com.io.begstd.dice.services.extension.common.SpinExtension;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class SpinExtensionImpl implements SpinExtension {

    @Override
    public BasePlaySession spin(BasePlaySession currentBasePlaySession,
                                Map<DiceConfigMode, ICommonDiceConfig> configMapper,
                                BaseDiceMachineImpl baseDiceMachine) {
        
        IDiceMachineConfig diceMachineConfigNormal = (IDiceMachineConfig) configMapper.get(DiceConfigMode.NORMAL);
        List<InitJackpotChild> initJackpotList = diceMachineConfigNormal.initJackpotList();
        BasePlaySession updatedBasePlaySession = null;
        if (currentBasePlaySession.state() == GameState.NORMAL_GAME) {
            updatedBasePlaySession = baseDiceMachine.playGame(
                    currentBasePlaySession.betDenom(),
                    currentBasePlaySession,
                    diceMachineConfigNormal,
                    initJackpotList);
        }
        return updatedBasePlaySession;
    }

    
}
