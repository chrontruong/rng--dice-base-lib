package com.io.begstd.slot.services.extension.common.impl;

import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.BasePlaySession.BasePlaySessionBuilder;
import com.io.begstd.slot.model.app.GameState;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.ISlotConfigGamble;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.wallet.WalletOption;
import com.io.begstd.slot.services.extension.common.PlaySessionExtension;

import java.util.Map;
import java.util.UUID;

public abstract class AbstractPlaySessionExtension implements PlaySessionExtension {

    public abstract BasePlaySession.BasePlaySessionBuilder<?, ?> createPlaySession(UserInfo userInfo, SpinCmd cmd);

    @Override
    public final BasePlaySession.BasePlaySessionBuilder<?, ?> createPlaySessionBuilderForNormal(String serviceId,
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

    @Override
    public BasePlaySession.BasePlaySessionBuilder<?, ?> updatePlaySessionBuilderForFree(BasePlaySession basePlaySession,
                                                                                        String commandId,
                                                                                        UserInfo userInfo) {
        return basePlaySession.toBuilder()
                .increaseVersion()
                .commandId(commandId)
                .decreaseFreeGameRemain()
                .state(GameState.FREE_GAME)
                .resetErrorCodeList()
                .resetLastFreeGameWinData()
                .ip(userInfo.ip())
                .env(userInfo.env())
                .ssid(userInfo.ssid());
    }

    @Override
    public BasePlaySession.BasePlaySessionBuilder<?, ?> updatePlaySessionBuilderForBonus(BasePlaySession basePlaySession,
                                                                                         String commandId,
                                                                                         UserInfo userInfo) {
        return basePlaySession.toBuilder()
                .increaseVersion()
                .commandId(commandId)
                .state(GameState.BONUS_GAME)
                .resetErrorCodeList()
                .resetLatestBonusGame()
                .ip(userInfo.ip())
                .env(userInfo.env())
                .ssid(userInfo.ssid());

    }

    @Override
    public BasePlaySession.BasePlaySessionBuilder<?, ?> updatePlaySessionBuilderForFreeOption(BasePlaySession basePlaySession,
                                                                                              String commandId,
                                                                                              UserInfo userInfo) {
        return basePlaySession.toBuilder()
                .increaseVersion()
                .commandId(commandId)
                .state(GameState.FREE_OPTION_GAME)
                .resetErrorCodeList()
                .ip(userInfo.ip())
                .env(userInfo.env())
                .ssid(userInfo.ssid());
    }

    @Override
    public BasePlaySession.BasePlaySessionBuilder<?, ?> updatePlaySessionBuilderForLightning(BasePlaySession basePlaySession,
                                                                                             String commandId,
                                                                                             UserInfo userInfo) {
        return basePlaySession.toBuilder()
                .increaseVersion()
                .commandId(commandId)
                .decreaseLightningGameRemain()
                .winJackpotAmount(Money.ZERO)
                .jpInfoAmt(null)
                .state(GameState.LIGHTNING_GAME)
                .resetErrorCodeList()
                .resetLastLightningGameWinData()
                .resetLastPowerUpGameWinData()
                .ip(userInfo.ip())
                .env(userInfo.env())
                .ssid(userInfo.ssid());

    }

    @Override
    public BasePlaySession.BasePlaySessionBuilder<?, ?> updatePlaySessionBuilderForPowerUp(BasePlaySession basePlaySession,
                                                                                           String commandId,
                                                                                           UserInfo userInfo) {
        return basePlaySession.toBuilder()
                .increaseVersion()
                .commandId(commandId)
                .decreasePowerUpGameRemain()
                .state(GameState.POWERUP_GAME)
                .resetErrorCodeList()
                .ip(userInfo.ip())
                .env(userInfo.env())
                .ssid(userInfo.ssid());
    }

    /**
     * openCell is id of SymbolGamble in SlotGame_Gamble_Config.json
     */
    @Override
    public BasePlaySessionBuilder<?, ?> updatePlaySessionBuilderForGamble(BasePlaySession basePlaySession,
            String commandId, double totalBet, int openCell, Map<SlotConfigMode, ICommonSlotConfig> configMapper, UserInfo userInfo) {

        ISlotConfigGamble slotMachineConfigGamble = (ISlotConfigGamble) configMapper.get(SlotConfigMode.GAMBLE);

        UUID uuid = UUID.randomUUID();

        return basePlaySession.toBuilder()
                .increaseVersion()
                .commandId(commandId)
                .state(GameState.GAMBLE_GAME)
                .ticketIdForWallet(uuid.toString())
                .resetErrorCodeList()
                .decreaseGambleGameRemain()
                .gamblerUserSymbol(slotMachineConfigGamble.getSymbolById(openCell))
                .gamblerUserBet(Money.of(totalBet))
                .increaseGambleGameLevel()
                .gambleBet(Money.of(totalBet))
                .ip(userInfo.ip())
                .env(userInfo.env())
                .ssid(userInfo.ssid());
    }
}
