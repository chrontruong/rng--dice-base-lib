package com.io.begstd.dice.projection.impl;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.GameState;
import lombok.Data;
import lombok.experimental.Accessors;

public class BasePlaySessionProjectionImpl extends BasePlaySessionProjection {

    @Override
    public Object convertToFullViewModel(BasePlaySession basePlaySession) {
        return convertToFullViewModel(basePlaySession, DiceBaseViewerObj.class);
    }

    @Override
    public Object convertToSmallViewModel(BasePlaySession basePlaySession) {
        DiceBaseViewerObj viewerObj = convertToSmallViewModel(basePlaySession, DiceBaseViewerObj.class);
        switch (basePlaySession.state()) {
            case GameState.NORMAL_GAME:
                viewerObj.wa(basePlaySession.normalGameWinAmount() != null ? basePlaySession.normalGameWinAmount().value().doubleValue() : 0);

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
class DiceBaseViewerObj extends BaseViewerObj {
    public DiceBaseViewerObj(BasePlaySession basePlaySession) {
        super(basePlaySession);
    }
}


