package com.io.begstd.dice.services.internal.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.log.util.IGameAnalysis;
import com.io.begstd.dice.model.app.ActivePlayer;
import com.io.begstd.dice.model.app.PlayerStatus;
import com.io.begstd.dice.services.internal.ActivePlayerService;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActivePlayerServiceImpl implements ActivePlayerService, IGameAnalysis {

    public final static String USER_TYPE = "USER";
    
    @Value("${service.player.activeTime}")
    private int ACTIVE_TIME;
    @Value("${service.player.idleTime}")
    private int IDLE_TIME;
    @Value("${service.player.deactiveTime}")
    private int DEACTIVE_TIME;

//    @Autowired
//    private ApplicationContext applicationContext;

    private final Map<String, Long> activePlayerMap;
    private final Map<String, Long> activePlayerBotMap;

    public ActivePlayerServiceImpl() {
        activePlayerMap = new HashMap<>();
        activePlayerBotMap = new HashMap<>();
    }

    @Override
    public void updateLastModified(String playerId, String userType) {
        long now = Instant.now().toEpochMilli();
        if (USER_TYPE.equalsIgnoreCase(userType)) {
            activePlayerMap.put(playerId, now);
        } else {
            activePlayerBotMap.put(playerId, now);
        }
        LogsUtils.writeLogInfo(LogMessage.builder()
                .stateName("ActivePlayerServiceImpl.updateLastModified")
                .stepName("updateLastModified")
                .message(String.format("playerId %s, lastModified %s", playerId, now))
                .build());
    }

    /**
     * @return active and idle players
     */
    @Override
    public List<ActivePlayer> getActivePlayers() {
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder()
                .stateName("ActivePlayerServiceImpl.getActivePlayers")
                .stepName("getActivePlayers");

        long now = Instant.now().toEpochMilli();
        List<ActivePlayer> activePlayers = new ArrayList<>();
        List<ActivePlayer> idlePlayers = new ArrayList<>();
        List<String> deactivePlayers = new ArrayList<>();
        for (String playerId : activePlayerMap.keySet()) {
            long lastModified = activePlayerMap.get(playerId);
            int lastActionMinutes = (int) (now - lastModified) / 1000 / 60;
            if (lastActionMinutes < ACTIVE_TIME) {
                activePlayers.add(ActivePlayer.builder()
                        .playerId(playerId)
                        .status(PlayerStatus.ACTIVE)
                        .build());
            } else if (lastActionMinutes < IDLE_TIME) {
                idlePlayers.add(ActivePlayer.builder()
                        .playerId(playerId)
                        .status(PlayerStatus.IDLE)
                        .build());
            } else if (lastActionMinutes > DEACTIVE_TIME) {
                deactivePlayers.add(playerId);
            }
        }
        
        for (String deactivePlayer : deactivePlayers) {
            activePlayerMap.remove(deactivePlayer);
        }
        // for bot
        List<ActivePlayer> activePlayersBot = new ArrayList<>();
        List<ActivePlayer> idlePlayersBot = new ArrayList<>();
        List<String> deactivePlayersBot = new ArrayList<>();
        for (String playerId : activePlayerBotMap.keySet()) {
            long lastModified = activePlayerBotMap.get(playerId);
            int lastActionMinutes = (int) (now - lastModified) / 1000 / 60;
            if (lastActionMinutes < ACTIVE_TIME) {
                activePlayersBot.add(ActivePlayer.builder()
                        .playerId(playerId)
                        .status(PlayerStatus.ACTIVE)
                        .build());
            } else if (lastActionMinutes < IDLE_TIME) {
                idlePlayersBot.add(ActivePlayer.builder()
                        .playerId(playerId)
                        .status(PlayerStatus.IDLE)
                        .build());
            } else if (lastActionMinutes > DEACTIVE_TIME) {
                deactivePlayersBot.add(playerId);
            }
        }
        
        for (String deactivePlayer : deactivePlayersBot) {
            activePlayerBotMap.remove(deactivePlayer);
        }
        
        logBuilder.message(String.format("Players active: %s, idle: %s, deactive: %s; Bot active: %s, Bot idle: %s, Bot deactive: %s",
                activePlayers.stream().map(ActivePlayer::playerId).collect(Collectors.toList()),
                idlePlayers.stream().map(ActivePlayer::playerId).collect(Collectors.toList()),
                deactivePlayers,
                activePlayersBot.stream().map(ActivePlayer::playerId).collect(Collectors.toList()),
                idlePlayersBot.stream().map(ActivePlayer::playerId).collect(Collectors.toList()),
                deactivePlayersBot));
        LogsUtils.writeLogInfo(logBuilder.build());

        activePlayers.addAll(idlePlayers);
        activePlayers.addAll(activePlayersBot);
        activePlayers.addAll(idlePlayersBot);
        
        return activePlayers;
    }

    @Override
    public List<ActivePlayer> getActivePlayersByType(String userType) {
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder()
                .stateName("ActivePlayerServiceImpl.getActivePlayersByType")
                .stepName("getActivePlayersByType");
        
        long now = Instant.now().toEpochMilli();
        List<ActivePlayer> activePlayers = new ArrayList<>();
        List<ActivePlayer> idlePlayers = new ArrayList<>();
        List<String> deactivePlayers = new ArrayList<>();
        
        if (USER_TYPE.equalsIgnoreCase(userType)) {
            
            for (String playerId : activePlayerMap.keySet()) {
                long lastModified = activePlayerMap.get(playerId);
                int lastActionMinutes = (int) (now - lastModified) / 1000 / 60;
                if (lastActionMinutes < ACTIVE_TIME) {
                    activePlayers.add(ActivePlayer.builder()
                            .playerId(playerId)
                            .status(PlayerStatus.ACTIVE)
                            .build());
                } else if (lastActionMinutes < IDLE_TIME) {
                    idlePlayers.add(ActivePlayer.builder()
                            .playerId(playerId)
                            .status(PlayerStatus.IDLE)
                            .build());
                } else if (lastActionMinutes > DEACTIVE_TIME) {
                    deactivePlayers.add(playerId);
                }
            }
            
            for (String deactivePlayer : deactivePlayers) {
                activePlayerMap.remove(deactivePlayer);
            }
        } else {
            // for bot
            for (String playerId : activePlayerBotMap.keySet()) {
                long lastModified = activePlayerBotMap.get(playerId);
                int lastActionMinutes = (int) (now - lastModified) / 1000 / 60;
                if (lastActionMinutes < ACTIVE_TIME) {
                    activePlayers.add(ActivePlayer.builder()
                            .playerId(playerId)
                            .status(PlayerStatus.ACTIVE)
                            .build());
                } else if (lastActionMinutes < IDLE_TIME) {
                    idlePlayers.add(ActivePlayer.builder()
                            .playerId(playerId)
                            .status(PlayerStatus.IDLE)
                            .build());
                } else if (lastActionMinutes > DEACTIVE_TIME) {
                    deactivePlayers.add(playerId);
                }
            }
            
            for (String deactivePlayer : deactivePlayers) {
                activePlayerBotMap.remove(deactivePlayer);
            }
        }
        
        logBuilder.message(String.format("Players active: %s, idle: %s, deactive: %s",
                activePlayers.stream().map(ActivePlayer::playerId).collect(Collectors.toList()),
                idlePlayers.stream().map(ActivePlayer::playerId).collect(Collectors.toList()),
                deactivePlayers));
        LogsUtils.writeLogInfo(logBuilder.build());

        activePlayers.addAll(idlePlayers);
        
        return activePlayers;
    }

    @Override
    public int getCCUCount() {
        long now = Instant.now().toEpochMilli();
        int count = 0;
        for (String playerId : activePlayerMap.keySet()) {
            long lastModified = activePlayerMap.get(playerId);
            int lastActionMinutes = (int) (now - lastModified) / 1000 / 60;
            if (lastActionMinutes < 5) {
                count++;
            }
        }
        for (String playerId : activePlayerBotMap.keySet()) {
            long lastModified = activePlayerBotMap.get(playerId);
            int lastActionMinutes = (int) (now - lastModified) / 1000 / 60;
            if (lastActionMinutes < 5) {
                count++;
            }
        }
        return count;
    }
}
