package com.io.begstd.slot.services.extension.gameplay.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.common.SlotGameError;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.gamerule.FreeSpinOption;
import com.io.begstd.slot.model.playsession.FreeGameProb;
import com.io.begstd.slot.model.playsession.IFreeGameProb;
import com.io.begstd.slot.services.extension.gameplay.FreeGameOptionPlayExtension;
import com.io.begstd.slot.services.internal.IRandomService;
import com.io.begstd.slot.utils.BeanUtils;

import java.util.List;

public class FreeGameOptionPlayExtensionImpl implements FreeGameOptionPlayExtension{
    
    @Override
    public BasePlaySession playFreeGameOption(BasePlaySession basePlaySession, UserInfo userInfo, String commandId, int selectedOption,
                                              ISlotMachineConfig slotMachineConfigNormal, LogMessage.LogMessageBuilder logBuilder) {
        
        BasePlaySession.BasePlaySessionBuilder<?,?> builder = basePlaySession.toBuilder();
        
        List<FreeSpinOption> optList = slotMachineConfigNormal.freeSpinOption();
        FreeSpinOption foundItem = null;
        int i = 0;
        while (i < optList.size()) {
            FreeSpinOption item = optList.get(i);
            if (item.id() == selectedOption) {
                foundItem = item;
                break;
            }
            i++;
        }
        if (foundItem == null) {
            logBuilder.stepName("Check freegameoption").owner(LogMessage.OWNER_JACKPOT)
                .message("Error Overexceed- "+SlotGameError.ERROR_FREE_SPIN_OPTION_OVER)
                .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
        
            throw new SlotGameException(SlotGameError.ERROR_FREE_SPIN_OPTION_OVER, userInfo.userId(), userInfo.userType(), commandId,String.valueOf(selectedOption));
        }
        
        //random to get result
        int length = foundItem.percent().get(foundItem.percent().size()-1);
        int randItem = getRandomService().random("FREEGAME-OPTION", Integer.class, basePlaySession.userId(), length, basePlaySession.uuid());
        int wonIndex =  (randItem >= foundItem.percent().get(0))? 1:0;
        //update PlaySession
        IFreeGameProb freeSpinProb = new FreeGameProb(foundItem.id(), foundItem.freeSpin(), foundItem.paytable().get(wonIndex));
        builder.freeSpinProb(freeSpinProb);
        builder.addFreeGameCount(foundItem.freeSpin());
        builder.clearFreeGameOption();
        return builder.build();
    }


    private IRandomService getRandomService() {
        return BeanUtils.getBean(IRandomService.class);
    }
}
