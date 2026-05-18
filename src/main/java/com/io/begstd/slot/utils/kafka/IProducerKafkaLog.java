package com.io.begstd.slot.utils.kafka;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.utils.kafka.impl.PlaySessionToKafka;

public interface IProducerKafkaLog {
    public String getKafkaMessage(BasePlaySession basePlaySession, String prefixService, String messageWallet);

    default PlaySessionToKafka initPlaySessionKafka() {
        return new PlaySessionToKafka();
    }
}
