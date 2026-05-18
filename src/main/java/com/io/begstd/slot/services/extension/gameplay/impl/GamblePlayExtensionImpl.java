package com.io.begstd.slot.services.extension.gameplay.impl;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.GameState;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.ISlotConfigGamble;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.services.extension.gameplay.GamblePlayExtension;

import java.util.Map;
import java.util.UUID;

public class GamblePlayExtensionImpl implements GamblePlayExtension{

    /**
     * openCell is id of SymbolGamble in SlotGame_Gamble_Config.json
     */
    @Override
    public BasePlaySession updateEndGamble(BasePlaySession basePlaySession, double totalBet, int openCell, String commandId,
                                           Map<SlotConfigMode, ICommonSlotConfig> configMapper, UserInfo userInfo) {
        
        ISlotConfigGamble slotMachineConfigGamble = (ISlotConfigGamble) configMapper.get(SlotConfigMode.GAMBLE);

        BasePlaySession.BasePlaySessionBuilder<?,?> builder = basePlaySession.toBuilder();

        builder.increaseVersion();
        builder.commandId(commandId);
        builder.state(GameState.GAMBLE_GAME);
        builder.ticketIdForWallet(UUID.randomUUID().toString());
        builder.gamblerUserSymbol(slotMachineConfigGamble.getSymbolById(openCell));
        builder.gambleGameRemain(0);
        builder.gamblerUserBet(Money.of(totalBet));
        builder.gambleBet(Money.of(totalBet));
        builder.gamblerSystemSymbol(null);
        builder.isFinishGamble(true);
        builder.ip(userInfo.ip());
        
        return builder.build();
    }

}
