package com.io.begstd.dice.config.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.dice.config.StartTestProperty;
import com.io.begstd.dice.factory.GameRuleFactory;
import com.io.begstd.dice.model.config.ICommonDiceConfig;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.repository.IRedisDiceConfigRepository;
import com.io.begstd.dice.utils.ConfigManager;
import com.io.begstd.dice.utils.JsonParseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.io.FileReader;

@Slf4j
public class RedisKeyListener implements MessageListener {

    public static final String CHANNEL_RTP_CONFIG = "RtpConfig";
    public static final String CHANNEL_GAME_CONFIG = "GameConfig";
    public static final String CHANNEL_SECOND_GAME_CONFIG = "SecondGameConfig";
    public static final String REDIS_KEY_SPACE = "__keyspace@0__:";

    @Value("${game.normal.config.file}")
    private Resource normalGameConfig;

    @Autowired
    private GameRuleFactory gameRuleFactory;

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private IRedisDiceConfigRepository redisDiceConfigRepository;

    @Autowired
    @Qualifier("objectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private Environment env;

    @Autowired
    private StartTestProperty testProperty;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        try {
            IDiceMachineConfig slotMachineConfigForNormal = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
            String serviceId = slotMachineConfigForNormal.serviceId();
            String keyspace = new String(bytes);
            String channel = keyspace.replace(REDIS_KEY_SPACE, "");

            if (redisDiceConfigRepository.isExistedChannel(channel)) {
                String channelId = channel.replace("_" + serviceId, "");
                switch (channelId) {
                    case CHANNEL_GAME_CONFIG: {
                        String data = redisDiceConfigRepository.get(channel, serviceId);
                        log.info("Channel {} has been changed, data: {}", channel, data);
                        RedisNotification redisNoti = JsonParseUtils.deserializeFromJson(RedisNotification.class, data);
                        DiceConfigMode mode = DiceConfigMode.getByName(redisNoti.mode());
                        if (DiceConfigMode.NORMAL == mode && normalGameConfig.isReadable()) {
                            updateMainConfig(redisNoti.clazz(), redisNoti.json(), mode, normalGameConfig);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LogsUtils.writeLogException(LogMessage.builder()
                    .message(String.format("Error listening on Redis config change %s", new String(bytes)))
                    .build(), e);
        }
    }

    private void updateMainConfig(String clazz, String json, DiceConfigMode mode, Resource configResource) throws Exception {
        Gson gson = new Gson();
        JsonObject jsonConfig = gson.fromJson(new FileReader(configResource.getFile()), JsonObject.class);
        JsonObject jsonChange = gson.fromJson(json, JsonObject.class);
        boolean isChanged = false;

        for (String keyChange : jsonChange.keySet()) {
            if (jsonConfig.has(keyChange)) {
                log.info("Key: {}, before: {}, after: {}",
                        keyChange, jsonConfig.get(keyChange).toString(), jsonChange.get(keyChange).toString());

                jsonConfig.add(keyChange, jsonChange.get(keyChange));
                isChanged = true;
            }
        }

        if (isChanged) {
            ICommonDiceConfig slotConfig = (ICommonDiceConfig) objectMapper.readValue(jsonConfig.toString(), Class.forName(clazz));
            slotConfig.initInRuntime(gameRuleFactory);
            configManager.updateMainConfig(mode, slotConfig);
        }

        log.info("Done update main config, mode {}, clazz {}", mode.name(), clazz);
    }
}
