package com.io.begstd.slot.projection.impl;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.GameState;
import com.io.begstd.slot.utils.MatrixUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

public class BasePlaySessionProjectionImpl extends BasePlaySessionProjection {

    @Override
    public Object convertToFullViewModel(BasePlaySession basePlaySession) {
        return convertToFullViewModel(basePlaySession, SlotBaseViewerObj.class);
    }

    @Override
    public Object convertToSmallViewModel(BasePlaySession basePlaySession) {
        SlotBaseViewerObj viewerObj = convertToSmallViewModel(basePlaySession, SlotBaseViewerObj.class);
        switch (basePlaySession.state()) {
            case GameState.NORMAL_GAME:
                viewerObj.mx(basePlaySession.normalGameMatrix() != null && basePlaySession.normalGameTableFormat() != null
                        ? MatrixUtil.convertToMatrixCode1DByReel(basePlaySession.normalGameMatrix(), basePlaySession.normalGameTableFormat())
                        : new ArrayList<>());
                viewerObj.fRe(basePlaySession.freeGameRemain());
                viewerObj.fg(basePlaySession.latestWinFreeGameCount());
                viewerObj.bRe(basePlaySession.bonusGameRemain());
                viewerObj.bg(basePlaySession.latestWinBonusGameCount());
                viewerObj.wa(basePlaySession.normalGameWinAmount() != null ? basePlaySession.normalGameWinAmount().value().doubleValue() : 0);
                viewerObj.pl(basePlaySession.normalGamePayLines());

                if (basePlaySession.listNormalGameTransformMatrix().size() >= 2
                        && basePlaySession.listNormalGameTransformMatrix().get(1) != null) { // has been transformed
                    viewerObj.mx0(basePlaySession.listNormalGameTransformMatrix().get(0) != null && basePlaySession.normalGameTableFormat() != null
                            ? MatrixUtil.convertToMatrixCode1DByReel(basePlaySession.listNormalGameTransformMatrix().get(0), basePlaySession.normalGameTableFormat())
                            : new ArrayList<>());
                }

                viewerObj.wa(basePlaySession.winJackpotAmount() != null ? basePlaySession.winJackpotAmount().value().doubleValue() : 0);
                break;
            case GameState.FREE_GAME:
                viewerObj.mx(basePlaySession.freeGameMatrix() != null && basePlaySession.freeGameTableFormat() != null
                        ? MatrixUtil.convertToMatrixCode1DByReel(basePlaySession.freeGameMatrix(), basePlaySession.freeGameTableFormat())
                        : new ArrayList<>());
                viewerObj.wa(basePlaySession.latestWinAmount() != null ? basePlaySession.latestWinAmount().value().doubleValue() : 0);
                viewerObj.fRe(basePlaySession.freeGameRemain());
                viewerObj.fg(basePlaySession.latestWinFreeGameCount());
                viewerObj.bRe(basePlaySession.bonusGameRemain());
                viewerObj.bg(basePlaySession.latestWinBonusGameCount());
                viewerObj.pl(basePlaySession.freeGamePayLines());

                if (basePlaySession.listFreeGameTransformMatrix().size() >= 2
                        && basePlaySession.listFreeGameTransformMatrix().get(1) != null) {
                    viewerObj.mx0(basePlaySession.listFreeGameTransformMatrix().get(0) != null && basePlaySession.freeGameTableFormat() != null
                            ? MatrixUtil.convertToMatrixCode1DByReel(basePlaySession.listFreeGameTransformMatrix().get(0), basePlaySession.freeGameTableFormat())
                            : new ArrayList<>());
                }
                viewerObj.fa(basePlaySession.freeGameWinAmount() != null ? basePlaySession.freeGameWinAmount().value().doubleValue() : 0);
                viewerObj.wa(basePlaySession.winJackpotAmount() != null ? basePlaySession.winJackpotAmount().value().doubleValue() : 0);
                break;

            default:
                break;
        }
        return viewerObj;
    }

}

@Data
@Accessors(fluent = true)
class SlotBaseViewerObj extends BaseViewerObj{
    public SlotBaseViewerObj(BasePlaySession basePlaySession) {
        super(basePlaySession);
    }
    
    private List<String> mx0;
    private int fg;
    private int bg;
}


