package com.io.begstd.slot.services.extension.validator.impl;

import com.io.begstd.slot.common.SlotGameError;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.IMiniSlotConfig;
import com.io.begstd.slot.model.config.ISlotConfigGamble;
import com.io.begstd.slot.services.extension.validator.ValidationExtension;

public class ValidationExtensionImpl implements ValidationExtension{
    
    @Override
    public void validateInRespin(BasePlaySession basePlaySession, UserInfo userInfo, String commandId) {
        if (basePlaySession == null) {
            throw new SlotGameException(SlotGameError.PLAYSESSION_NOT_EXISTED, userInfo.userId(), "", commandId);
        }

        if (!basePlaySession.hasRespin()) {
            throw new SlotGameException(SlotGameError.NO_RESPIN, userInfo.userId(), basePlaySession.userType(), commandId);
        }
    }
    
    @Override
    public void validateInFree(BasePlaySession basePlaySession, UserInfo userInfo, String commandId) {
        if (basePlaySession == null) {
            throw new SlotGameException(SlotGameError.PLAYSESSION_NOT_EXISTED, userInfo.userId(), "", commandId);
        } 
        if (!basePlaySession.hasFreeGame()) {
            throw new SlotGameException(SlotGameError.NO_FREE_SPIN, userInfo.userId(), basePlaySession.userType(), commandId);
        }
    }

    @Override
    public void validateInBonus(BasePlaySession basePlaySession, UserInfo userInfo, String commandId, int openCell, IMiniSlotConfig configMini) {
        if (basePlaySession == null) {
            throw new SlotGameException(SlotGameError.PLAYSESSION_NOT_EXISTED, userInfo.userId(), "", commandId);
        }

        if (!basePlaySession.hasBonusGame() || basePlaySession.bonusPlayRemain() <= 0) {
            throw new SlotGameException(SlotGameError.NO_MINI_GAME, userInfo.userId(), basePlaySession.userType(), commandId);
        }
        
        if (openCell < 0 || openCell >= configMini.bonusMatrixSize()) {
            throw new SlotGameException(SlotGameError.EXCEED_CELL_INBONUS, userInfo.userId(), basePlaySession.userType(), commandId);
        }
        
    }

    @Override
    public void validateInFreeSpinOption(BasePlaySession basePlaySession, UserInfo userInfo, String commandId,
            int openCell) {
        if (basePlaySession == null) {
            throw new SlotGameException(SlotGameError.PLAYSESSION_NOT_EXISTED, userInfo.userId(), "", commandId);
        }
        
        if (basePlaySession.freeGameOption() == null || basePlaySession.freeGameOption().size() == 0) {
            throw new SlotGameException(SlotGameError.NO_FREE_SPIN_OPTION, userInfo.userId(), basePlaySession.userType(), commandId);
        }
    }

    @Override
    public void validateInLightning(BasePlaySession basePlaySession, UserInfo userInfo, String commandId) {
        if (basePlaySession == null) {
            throw new SlotGameException(SlotGameError.PLAYSESSION_NOT_EXISTED, userInfo.userId(), "", commandId);
        }
        
        if (!basePlaySession.hasLightningGame()) {
            throw new SlotGameException(SlotGameError.NO_LIGHTING_SPIN, userInfo.userId(), userInfo.userType(), commandId);
        }
        if (basePlaySession.hasBonusGame()) {
          throw new SlotGameException(SlotGameError.BONUS_MODE_MUST_PROCESS_BEFORE, userInfo.userId(), userInfo.userType(), commandId);
      }
      
      if (basePlaySession.hasPowerUpGame()) {
//          log.error("==== POWER MODE MUST PROCESS BEFORE  ==== "+playSession);
          throw new SlotGameException(SlotGameError.POWERUP_MODE_MUST_PROCESS_BEFORE, userInfo.userId(), userInfo.userType(), commandId);
      }
        
    }

    @Override
    public void validateInPowerUp(BasePlaySession basePlaySession, UserInfo userInfo, String commandId) {
        
        if (basePlaySession == null) {
            throw new SlotGameException(SlotGameError.PLAYSESSION_NOT_EXISTED, userInfo.userId(), "", commandId);
        }
        
        if (!basePlaySession.hasPowerUpGame() || basePlaySession.lightningGameMatrix() == null) {
//          log.error("==== No POWER UP GAME  ==== userId="+userId +" -- commandId="+commandId + " -- openCell=" + openCell);
          throw new SlotGameException(SlotGameError.NO_POWER_UP_SPIN, userInfo.userId(), userInfo.userType(), commandId);
      }
      
      if (basePlaySession.hasBonusGame()) {
//          log.error("==== BONUS MODE MUST PROCESS BEFORE  ==== userId="+userId +" -- commandId="+commandId + " -- openCell=" + openCell);
          throw new SlotGameException(SlotGameError.BONUS_MODE_MUST_PROCESS_BEFORE, userInfo.userId(), userInfo.userType(), commandId);
      }
    }

    /**
     * openCell is id of SymbolGamble in SlotGame_Gamble_Config.json
     */
    @Override
    public void validateInGamble(BasePlaySession basePlaySession, UserInfo userInfo, String commandId,
            double totalBet, int openCell, ISlotConfigGamble gambleConfigPara) {
        
        if (basePlaySession == null) {
            throw new SlotGameException(SlotGameError.PLAYSESSION_NOT_EXISTED, userInfo.userId(), "", commandId);
        }

        if (openCell < 0 || gambleConfigPara.getSymbolById(openCell) == null) {
            throw new SlotGameException(SlotGameError.EXCEED_CELL_INBONUS, basePlaySession.userId(), basePlaySession.userType(), commandId);
        }
    }
}
