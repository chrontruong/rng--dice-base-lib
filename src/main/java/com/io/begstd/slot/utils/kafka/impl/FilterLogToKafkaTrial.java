package com.io.begstd.slot.utils.kafka.impl;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class FilterLogToKafkaTrial extends Filter<ILoggingEvent>{
    @Override
    public FilterReply decide(ILoggingEvent event) {    
      if (event.getMessage().contains("TrialGResult")) {
        return FilterReply.ACCEPT;
      } else {
        return FilterReply.DENY;
      }
    }
}
