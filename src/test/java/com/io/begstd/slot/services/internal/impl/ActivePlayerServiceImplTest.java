package com.io.begstd.slot.services.internal.impl;

import com.io.begstd.slot.AbstractBaseSlotMockTest;
import com.io.begstd.slot.model.app.ActivePlayer;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivePlayerServiceImplTest extends AbstractBaseSlotMockTest {

    @Test
    public void testUpdateLastModified() {
        long now = Instant.now().toEpochMilli();
        String testPlayer1 = "testPlayer1";
        String testPlayer2 = "testPlayer2";
        String testPlayer3 = "testPlayer3";
        Map<String, Long> map = new HashMap<>();
        map.put(testPlayer1, now);
        map.put(testPlayer2, now - 11 * 60 * 1000);
        map.put(testPlayer3, now - 61 * 60 * 1000);
        ActivePlayerServiceImpl activePlayerService = new ActivePlayerServiceImpl();
        ReflectionTestUtils.setField(activePlayerService, "activePlayerMap", map);
        ReflectionTestUtils.setField(activePlayerService, "ACTIVE_TIME", 10);
        ReflectionTestUtils.setField(activePlayerService, "IDLE_TIME", 30);
        ReflectionTestUtils.setField(activePlayerService, "DEACTIVE_TIME", 60);

        List<ActivePlayer> activePlayers = activePlayerService.getActivePlayers();

        Assertions.assertThat(activePlayers).isNotNull();
        Assertions.assertThat(activePlayers.stream().anyMatch(activePlayer -> activePlayer.playerId().equals(testPlayer1))).isTrue();
        Assertions.assertThat(activePlayers.stream().anyMatch(activePlayer -> activePlayer.playerId().equals(testPlayer2))).isTrue();
        Assertions.assertThat(activePlayers.stream().noneMatch(activePlayer -> activePlayer.playerId().equals(testPlayer3))).isTrue();
    }

}
