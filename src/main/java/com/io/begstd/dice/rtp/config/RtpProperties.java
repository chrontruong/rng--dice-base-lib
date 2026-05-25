package com.io.begstd.dice.rtp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Getter
@Setter
@Profile("rtp")
public class RtpProperties {

    @Value("${rtp.data.userPrefix}")
    private String userPrefix;

    @Value("${rtp.data.gameId}")
    private String gameId;

    @Value("${rtp.data.serviceId}")
    private String serviceId;

    @Value("${rtp.data.commandId}")
    private String commandId;

    @Value("${rtp.data.needJoinGame}")
    private boolean needJoinGame = false;

    @Value("#{${rtp.data.minigame.size} - 1}")
    private int minigameSize = 14;

    @Value("${thread.corePoolSize}")
    private int corePoolSize;

    @Value("${thread.maxPoolSize}")
    private int maxPoolSize;
}
