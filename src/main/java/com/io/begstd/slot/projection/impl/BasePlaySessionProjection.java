package com.io.begstd.slot.projection.impl;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.projection.IPlaySessionProjection;
import com.io.begstd.slot.services.internal.JackpotTrialModeService;
import com.io.begstd.slot.utils.MatrixUtil;
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
        viewerObj.nMx(basePlaySession.normalGameMatrix() != null && basePlaySession.normalGameTableFormat() != null
                ? MatrixUtil.convertToMatrixCode1DByReel(basePlaySession.normalGameMatrix(), basePlaySession.normalGameTableFormat())
                : new ArrayList<>());
        viewerObj.nLn(basePlaySession.normalGamePayLines());
        viewerObj.na(basePlaySession.normalGameWinAmount() != null ? basePlaySession.normalGameWinAmount().value().doubleValue() : 0);

        // Free
        viewerObj.fRe(basePlaySession.freeGameRemain());
        viewerObj.fta(basePlaySession.freeGameTotal());
        viewerObj.fMx(basePlaySession.freeGameMatrix() != null && basePlaySession.freeGameTableFormat() != null
                ? MatrixUtil.convertToMatrixCode1DByReel(basePlaySession.freeGameMatrix(), basePlaySession.freeGameTableFormat())
                : new ArrayList<>());
        viewerObj.fLn(basePlaySession.freeGamePayLines());
        viewerObj.fa(basePlaySession.freeGameWinAmount() != null ? basePlaySession.freeGameWinAmount().value().doubleValue() : 0);

        // Bonus
        viewerObj.bRe(basePlaySession.bonusGameRemain());
        viewerObj.bTa(basePlaySession.bonusGameTotal());
        viewerObj.bpRe(basePlaySession.bonusPlayRemain());
        viewerObj.ba(basePlaySession.bonusGameWinAmount() != null ? basePlaySession.bonusGameWinAmount().value().doubleValue() : 0);
        viewerObj.baC(basePlaySession.bonusGameWinAmtCurrent()!= null ? basePlaySession.bonusGameWinAmtCurrent().value().doubleValue() : 0);

        // Promotion
        if (basePlaySession.promotionCode() != null
                && !basePlaySession.promotionCode().equals("")
                && basePlaySession.promotionBetId() != null) {
            viewerObj.pro(basePlaySession.promotionBetId() + ";" + basePlaySession.promotionRemain() + ";" + basePlaySession.promotionTotal());
        }
/*
        if (basePlaySession.quest() != null) {
            viewerObj.evl(getEventList(basePlaySession.quest()));
            viewerObj.qId(basePlaySession.quest().getQuestId());
            if (basePlaySession.quest().isComplete()) {
                if (basePlaySession.questAmount() != null && basePlaySession.questAmount().isGreaterThan(Money.ZERO)) {
                    viewerObj.wq(basePlaySession.questAmount().value().doubleValue());
                }
                if (basePlaySession.eventAmount() != null && basePlaySession.eventAmount().isGreaterThan(Money.ZERO)) {
                    viewerObj.we(basePlaySession.eventAmount().value().doubleValue());
                }
            }
        }
*/
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
        viewerObj.fRe(basePlaySession.freeGameRemain());
        viewerObj.fta(basePlaySession.freeGameTotal());
        // Promotion
        if (basePlaySession.promotionCode() != null
                && !basePlaySession.promotionCode().equals("")
                && basePlaySession.promotionBetId() != null) {
            viewerObj.pro(basePlaySession.promotionBetId() + ";" + basePlaySession.promotionRemain() + ";" + basePlaySession.promotionTotal());
        }
/*
        if (basePlaySession.quest() != null) {
            viewerObj.evl(getEventList(basePlaySession.quest()));
            viewerObj.qId(basePlaySession.quest().getQuestId());
            if (basePlaySession.quest().isComplete()) {
                if (basePlaySession.questAmount() != null && basePlaySession.questAmount().isGreaterThan(Money.ZERO)) {
                    viewerObj.wq(basePlaySession.questAmount().value().doubleValue());
                }
                if (basePlaySession.eventAmount() != null && basePlaySession.eventAmount().isGreaterThan(Money.ZERO)) {
                    viewerObj.we(basePlaySession.eventAmount().value().doubleValue());
                }
            }
        }
        */
    }
/*
    private List<String> getEventList(Quest quest) {
        if (quest.getTasks() == null) {
            return null;
        }
        return quest.getTasks().stream()
                .map(task ->
                        task.getId() + ";" +
                        task.getName() + ";" +
                        GameUtils.formatDouble(task.getScore()) + "/" + GameUtils.formatDouble(task.getTargetScore()))
                .collect(Collectors.toList());
    }
*/
}


