package com.io.begstd.dice.services.internal;

import com.io.begstd.dice.model.app.ActivePlayer;

import java.util.List;

public interface ActivePlayerService {
    void updateLastModified(String playerId, String userType);
    List<ActivePlayer> getActivePlayers();
    List<ActivePlayer> getActivePlayersByType(String userType);
}
