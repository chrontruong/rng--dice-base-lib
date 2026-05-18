package com.io.begstd.slot.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ISlotMachineConfig;

import java.util.ArrayList;
import java.util.List;

public interface ExtraDataInitGameExtension extends Extension{
    default List<String> getExtraDataForInit(ISlotMachineConfig slotMachineConfig) {
        return new ArrayList<>();
    }

    default List<String> getExtraDataForInit(ISlotMachineConfig slotMachineConfig, UserInfo userInfo) {
        return getExtraDataForInit(slotMachineConfig);
    }
}
