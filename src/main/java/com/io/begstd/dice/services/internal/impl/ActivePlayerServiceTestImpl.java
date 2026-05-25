package com.io.begstd.dice.services.internal.impl;

import com.io.begstd.dice.model.app.ActivePlayer;
import com.io.begstd.dice.services.internal.ActivePlayerService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ActivePlayerServiceTestImpl implements ActivePlayerService {

    @Override
    public void updateLastModified(String playerId, String userType) {
        log.info("Test maintainUser {}", playerId);
    }

    @Override
    public List<ActivePlayer> getActivePlayers() {
        log.info("Test getActivePlayers");
        return new ArrayList<>();
    }

    @Override
    public List<ActivePlayer> getActivePlayersByType(String userType) {
        return new ArrayList<>();
    }
}
