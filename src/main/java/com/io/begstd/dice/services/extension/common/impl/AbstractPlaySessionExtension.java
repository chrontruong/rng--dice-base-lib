package com.io.begstd.dice.services.extension.common.impl;

import com.io.begstd.dice.model.app.GameState;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.model.wallet.WalletOption;
import com.io.begstd.dice.services.extension.common.PlaySessionExtension;
import com.io.begstd.dice.command.SpinCmd;
import com.io.begstd.dice.model.app.BasePlaySession.BasePlaySessionBuilder;

public abstract class AbstractPlaySessionExtension implements PlaySessionExtension {

    public abstract BasePlaySessionBuilder<?, ?> createPlaySession(UserInfo userInfo, SpinCmd cmd);

    @Override
    public final BasePlaySessionBuilder<?, ?> createPlaySessionBuilderForNormal(String serviceId,
                                                                                          String uuid,
                                                                                          String commandId,
                                                                                          UserInfo userInfo,
                                                                                          SpinCmd cmd,
                                                                                          boolean isTrialMode) {
        return createPlaySession(userInfo, cmd).uuid(uuid)
                .userId(userInfo.userId())
                .serviceId(serviceId)
                .commandId(commandId)
                .setStartTime()
                .userType(userInfo.userType())
                .displayName(userInfo.displayName())
                .isTrialMode(isTrialMode)
                .version(1)
                .state(GameState.NORMAL_GAME)
                .agentWallet(userInfo.userAgent())
                .ip(userInfo.ip())
                .avatar(userInfo.avatar())
                .env(userInfo.env())
                .ssid(userInfo.ssid())
                .walletOption(userInfo.walletOption() != null ? userInfo.walletOption().getCode() : WalletOption.MAIN.getCode());
    }
}
