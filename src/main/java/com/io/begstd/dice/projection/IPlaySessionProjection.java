package com.io.begstd.dice.projection;

import com.io.begstd.dice.model.app.BasePlaySession;

public interface IPlaySessionProjection {
    Object convertToFullViewModel(BasePlaySession basePlaySession);
    Object convertToSmallViewModel(BasePlaySession basePlaySession);
}
