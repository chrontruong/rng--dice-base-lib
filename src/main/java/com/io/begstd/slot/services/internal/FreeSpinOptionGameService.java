package com.io.begstd.slot.services.internal;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;

public interface FreeSpinOptionGameService {
    BasePlaySession spin(String commandId, UserInfo userInfo, int selectedOption);
    BasePlaySession spinTrial(String commandId, UserInfo userInfo, int selectedOption);
}
