package com.io.begstd.dice.projection.impl;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.projection.IPlaySessionProjection;
import com.io.begstd.dice.services.internal.JackpotTrialModeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class BasePlaySessionProjection implements IPlaySessionProjection {

    @Autowired
    private JackpotTrialModeService jackpotTrialModeService;

    protected  <T extends BaseViewerObj> T convertToSmallViewModel(BasePlaySession basePlaySession, Class<T> clazz) {
        T viewerObj = null;
        try {
            viewerObj = clazz.getDeclaredConstructor(BasePlaySession.class).newInstance(basePlaySession);
            convertSmallCommon(basePlaySession, viewerObj);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error(e.getMessage(), e);
        }
        return viewerObj;
    }

    protected  <T extends BaseViewerObj> T convertToFullViewModel(BasePlaySession basePlaySession, Class<T> clazz) {
        T viewerObj = null;
        try {
            viewerObj = clazz.getDeclaredConstructor(BasePlaySession.class).newInstance(basePlaySession);
            convertFullCommon(basePlaySession, viewerObj);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error(e.getMessage(), e);
        }
        return viewerObj;
    }

    private void convertFullCommon(BasePlaySession basePlaySession, BaseViewerObj viewerObj) {
        if(basePlaySession.isTrialMode()) {
            List<String> jpList = jackpotTrialModeService.getUserJackpotList(basePlaySession.userId());
            if(!CollectionUtils.isEmpty(jpList)) {
                viewerObj.tJ(jpList);
            }
        }
        viewerObj.s(basePlaySession.state());
        viewerObj.cIdt(basePlaySession.commandId());
        // Normal
        viewerObj.na(basePlaySession.normalGameWinAmount() != null ? basePlaySession.normalGameWinAmount().value().doubleValue() : 0);

        // Promotion
        if (basePlaySession.promotionCode() != null
                && !basePlaySession.promotionCode().equals("")
                && basePlaySession.promotionBetId() != null) {
            viewerObj.pro(basePlaySession.promotionBetId() + ";" + basePlaySession.promotionRemain() + ";" + basePlaySession.promotionTotal());
        }
        viewerObj.wo(basePlaySession.walletOption());
    }

    private void convertSmallCommon(BasePlaySession basePlaySession, BaseViewerObj viewerObj) {
        if(basePlaySession.isTrialMode()) {
            List<String> jpList = jackpotTrialModeService.getUserJackpotList(basePlaySession.userId());
            if(!CollectionUtils.isEmpty(jpList)) {
                viewerObj.tJ(jpList);
            }

            // update trialJplWin
            if (!CollectionUtils.isEmpty(basePlaySession.lastJackpotInfo())) {
                viewerObj.tJW(new ArrayList<>(basePlaySession.lastJackpotInfo()));
            }
        }

        viewerObj.s(basePlaySession.state());
        // Promotion
        if (basePlaySession.promotionCode() != null
                && !basePlaySession.promotionCode().equals("")
                && basePlaySession.promotionBetId() != null) {
            viewerObj.pro(basePlaySession.promotionBetId() + ";" + basePlaySession.promotionRemain() + ";" + basePlaySession.promotionTotal());
        }
    }
}


