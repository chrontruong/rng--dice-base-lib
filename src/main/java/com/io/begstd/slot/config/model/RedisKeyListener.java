package com.io.begstd.slot.config.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.config.StartTestProperty;
import com.io.begstd.slot.factory.GameRuleFactory;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.repository.IRedisSlotConfigRepository;
import com.io.begstd.slot.utils.ConfigManager;
import com.io.begstd.slot.utils.JsonParseUtils;
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
    @Value("${game.free.config.file:''}")
    private Resource freeGameConfig;
    @Value("${game.mini.config.file:''}")
    private Resource miniGameConfig;
    @Value("${game.gameble.config.file:''}")
    private Resource gamebleGameConfig;
    @Value("${game.lightning.config.file:''}")
    private Resource lightningGameConfig;
    @Value("${game.powerUp.config.file:''}")
    private Resource powerUpGameConfig;

    @Autowired
    private GameRuleFactory gameRuleFactory;

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private IRedisSlotConfigRepository redisSlotConfigRepository;

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
            ISlotMachineConfig slotMachineConfigForNormal = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
            String serviceId = slotMachineConfigForNormal.serviceId();
            String keyspace = new String(bytes);
            String channel = keyspace.replace(REDIS_KEY_SPACE, "");

            if (redisSlotConfigRepository.isExistedChannel(channel)) {
                String channelId = channel.replace("_" + serviceId, "");
                switch (channelId) {
                    case CHANNEL_GAME_CONFIG: {
                        String data = redisSlotConfigRepository.get(channel, serviceId);
                        log.info("Channel {} has been changed, data: {}", channel, data);
                        RedisNotification redisNoti = JsonParseUtils.deserializeFromJson(RedisNotification.class, data);
                        SlotConfigMode mode = SlotConfigMode.getByName(redisNoti.mode());
                        if (SlotConfigMode.NORMAL == mode && normalGameConfig.isReadable()) {
                            updateMainConfig(redisNoti.clazz(), redisNoti.json(), mode, normalGameConfig);
                        } else if (SlotConfigMode.FREE == mode && freeGameConfig.isReadable()) {
                            updateMainConfig(redisNoti.clazz(), redisNoti.json(), mode, freeGameConfig);
                        } else if (SlotConfigMode.MINI == mode && miniGameConfig.isReadable()) {
                            updateMainConfig(redisNoti.clazz(), redisNoti.json(), mode, miniGameConfig);
                        } else if (SlotConfigMode.GAMBLE == mode && gamebleGameConfig.isReadable()) {
                            updateMainConfig(redisNoti.clazz(), redisNoti.json(), mode, gamebleGameConfig);
                        } else if (SlotConfigMode.LIGHTNING == mode && lightningGameConfig.isReadable()) {
                            updateMainConfig(redisNoti.clazz(), redisNoti.json(), mode, lightningGameConfig);
                        } else if (SlotConfigMode.POWERUP == mode && powerUpGameConfig.isReadable()) {
                            updateMainConfig(redisNoti.clazz(), redisNoti.json(), mode, powerUpGameConfig);
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

    private void updateMainConfig(String clazz, String json, SlotConfigMode mode, Resource configResource) throws Exception {
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
            ICommonSlotConfig slotConfig = (ICommonSlotConfig) objectMapper.readValue(jsonConfig.toString(), Class.forName(clazz));
            slotConfig.initInRuntime(gameRuleFactory);
            configManager.updateMainConfig(mode, slotConfig);
        }

        log.info("Done update main config, mode {}, clazz {}", mode.name(), clazz);
    }
}
