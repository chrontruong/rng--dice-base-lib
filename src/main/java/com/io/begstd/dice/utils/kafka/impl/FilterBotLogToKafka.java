package com.io.begstd.dice.utils.kafka.impl;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.io.begstd.dice.utils.BeanUtils;
import com.io.begstd.dice.config.ExternalServiceEndPointConfiguration;

public class FilterBotLogToKafka extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        boolean isEnable = true;
        ExternalServiceEndPointConfiguration configuration = BeanUtils.getBean(ExternalServiceEndPointConfiguration.class);
        if (configuration != null)
            isEnable = configuration.isEnableKafka();

        if (isEnable && event.getMessage().contains("GameResult")
                && event.getMessage().contains("\"userType\":\"BOT\"") ) {
            return FilterReply.ACCEPT;
        } else {
            return FilterReply.DENY;
        }
    }
}
