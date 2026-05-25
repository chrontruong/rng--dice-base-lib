package com.io.begstd.dice.utils.kafka;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.utils.kafka.impl.PlaySessionToKafka;

public interface IProducerKafkaLog {
    public String getKafkaMessage(BasePlaySession basePlaySession, String prefixService, String messageWallet);

    default PlaySessionToKafka initPlaySessionKafka() {
        return new PlaySessionToKafka();
    }
}
