package com.io.begstd.slot.services.extension.common.impl;

import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.services.extension.common.ExtraDataInitGameExtension;

import java.util.List;

public class ExtraDataInitGameExtensionImpl implements ExtraDataInitGameExtension{

    @Override
    public  List<String> getExtraDataForInit(ISlotMachineConfig slotMachineConfig) {

        return null;
    }

    @Override
    public String getName() {
        return ExtraDataInitGameExtensionImpl.class.getSimpleName();
    }
}
