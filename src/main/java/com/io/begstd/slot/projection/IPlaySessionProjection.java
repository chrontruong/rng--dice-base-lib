package com.io.begstd.slot.projection;

import com.io.begstd.slot.model.app.BasePlaySession;

public interface IPlaySessionProjection {
    Object convertToFullViewModel(BasePlaySession basePlaySession);
    Object convertToSmallViewModel(BasePlaySession basePlaySession);
}
