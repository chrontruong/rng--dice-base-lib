package com.io.begstd.dice.services.internal.impl;

import com.io.begstd.dice.services.internal.StartTestSchedulerService;
import com.io.begstd.dice.config.StartTestProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class StartTestSchedulerServiceTestImpl implements StartTestSchedulerService {

    @Autowired
    private StartTestProperty startTestProperty;

    @Value(value = "${service.intervalCheckStartTestSeconds:600}")
    private int intervalCheckStartTestSeconds;

    private ScheduledExecutorService scheduler;

    @Override
    @PostConstruct
    public void init() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(this::checkStartTest, 0, intervalCheckStartTestSeconds, SECONDS);
    }

    private void checkStartTest() {
        boolean enableStartTest = true;
        startTestProperty.setStartTest(enableStartTest);
    }
}
