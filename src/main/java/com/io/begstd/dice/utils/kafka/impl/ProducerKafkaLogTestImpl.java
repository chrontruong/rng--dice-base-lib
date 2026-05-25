package com.io.begstd.dice.utils.kafka.impl;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.utils.kafka.IProducerKafkaLog;

public class ProducerKafkaLogTestImpl implements IProducerKafkaLog {

    @Override
    public String getKafkaMessage(BasePlaySession basePlaySession, String prefixService, String messageWallet) {
        return null;
    }

}
