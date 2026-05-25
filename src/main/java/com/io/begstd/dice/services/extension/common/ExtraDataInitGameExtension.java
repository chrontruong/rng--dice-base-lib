package com.io.begstd.dice.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.model.config.IDiceMachineConfig;

import java.util.ArrayList;
import java.util.List;

public interface ExtraDataInitGameExtension extends Extension{
    default List<String> getExtraDataForInit(IDiceMachineConfig slotMachineConfig) {
        return new ArrayList<>();
    }

    default List<String> getExtraDataForInit(IDiceMachineConfig slotMachineConfig, UserInfo userInfo) {
        return getExtraDataForInit(slotMachineConfig);
    }
}
