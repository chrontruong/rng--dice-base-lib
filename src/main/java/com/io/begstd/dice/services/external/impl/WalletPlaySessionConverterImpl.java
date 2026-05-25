package com.io.begstd.dice.services.external.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.JackpotInfo;
import com.io.begstd.wallet.model.JackpotWin;
import com.io.begstd.wallet.model.WalletPlaySession;
import com.io.begstd.wallet.model.slot.Money;
import com.io.begstd.wallet.service.WalletPlaySessionConverter;

import java.util.List;
import java.util.stream.Collectors;

public class WalletPlaySessionConverterImpl implements WalletPlaySessionConverter {

    @Override
    public WalletPlaySession convertToWalletPlaySession(Object slotBasePlaySession) {
        try {
            BasePlaySession basePlaySession = (BasePlaySession) slotBasePlaySession;
            return WalletPlaySession.builder()
                    .uuid(basePlaySession.uuid())
                    .userId(basePlaySession.userId())
                    .userType(basePlaySession.userType())
                    .agentWallet(basePlaySession.agentWallet())
                    .serviceId(basePlaySession.serviceId())
                    .commandId(basePlaySession.commandId())
                    .totalBet(basePlaySession.totalBet() != null ? Money.of(basePlaySession.totalBet().value()) : Money.ZERO)
                    .gambleBet(basePlaySession.gambleBet() != null ? Money.of(basePlaySession.gambleBet().value()) : Money.ZERO)
                    .winAmount(basePlaySession.winAmount() != null ? Money.of(basePlaySession.winAmount().value()) : Money.ZERO)
                    .state(basePlaySession.state())
                    .promotionCode(basePlaySession.promotionCode())
                    .startTime(basePlaySession.startTime())
                    .ticketIdForWallet(basePlaySession.ticketIdForWallet())
                    .ip(basePlaySession.ip())
                    .walletOption(basePlaySession.walletOption())
                    .jackpotWins(buildJackpotWinInfo(basePlaySession.jackpotHistory()))
                    .currency(basePlaySession.currency())
//                    .eventId(basePlaySession.quest() != null ? basePlaySession.quest().getEventId() : "")
//                    .eventId(basePlaySession.quest() != null ? basePlaySession.quest().getQuestId() : "")
//                    .questAmount(basePlaySession.questAmount() != null ? Money.of(basePlaySession.questAmount().value()) : Money.ZERO)
//                    .eventAmount(basePlaySession.eventAmount() != null ? Money.of(basePlaySession.eventAmount().value()) : Money.ZERO)
//                    .isMission(basePlaySession.quest() != null && basePlaySession.quest().isMission())
                    .build();
        } catch (Exception e) {
            LogsUtils.writeLogException(LogMessage.builder()
                    .owner(LogMessage.OWNER_GAME)
                    .stateName("PlaySessionAdapterImpl")
                    .stepName("convertToWalletPlaySession")
                    .build(), e);
        }
        return null;
    }

    private List<JackpotWin> buildJackpotWinInfo(List<JackpotInfo> jackpotHistory) {
        if (jackpotHistory != null) {
            return jackpotHistory.stream()
                    .map(jackpotInfo -> {
                        JackpotWin jackpotWin = new JackpotWin();
                        try {
                            if (jackpotInfo.jackpotId() != null) {
                                int begin = jackpotInfo.jackpotId().lastIndexOf("_") + 1;
                                if (begin > 0) {
                                    String type = jackpotInfo.jackpotId().substring(begin);
                                    jackpotWin.setType(type);
                                }
                            }

                            if (jackpotInfo.jackpotAmount() != null) {
                                jackpotWin.setValue(jackpotInfo.jackpotAmount().value().doubleValue());
                            }
                        } catch (Exception e) {
                            LogsUtils.writeLogException(LogMessage.builder()
                                    .owner(LogMessage.OWNER_GAME)
                                    .stateName("WalletPlaySessionConverterImpl.buildJackpotWinInfo")
                                    .stepName("buildJackpotWinInfo")
                                    .build(), e);
                        }
                        return jackpotWin;
                    })
                    .collect(Collectors.toList());
        }
        return null;
    }
}
