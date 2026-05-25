package com.io.begstd.dice.services.extension.common.impl;

import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.services.extension.common.ExtraDataInitGameExtension;

import java.util.List;

public class ExtraDataInitGameExtensionImpl implements ExtraDataInitGameExtension {

    @Override
    public  List<String> getExtraDataForInit(IDiceMachineConfig slotMachineConfig) {

        return null;
    }

    @Override
    public String getName() {
        return ExtraDataInitGameExtensionImpl.class.getSimpleName();
    }
}
