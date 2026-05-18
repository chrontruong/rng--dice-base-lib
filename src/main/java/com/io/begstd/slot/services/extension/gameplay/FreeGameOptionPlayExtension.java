package com.io.begstd.slot.services.extension.gameplay;

import com.io.begstd.extension.Extension;
import com.io.begstd.log.LogMessage;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ISlotMachineConfig;

public interface FreeGameOptionPlayExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }
    BasePlaySession playFreeGameOption(BasePlaySession basePlaySession, UserInfo userInfo, String commandId, int openCell,
                                       ISlotMachineConfig slotMachineConfigNormal, LogMessage.LogMessageBuilder logBuilder);
}
