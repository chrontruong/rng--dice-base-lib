package com.io.begstd.slot.utils.kafka.impl;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.io.begstd.slot.config.ExternalServiceEndPointConfiguration;
import com.io.begstd.slot.utils.BeanUtils;

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
