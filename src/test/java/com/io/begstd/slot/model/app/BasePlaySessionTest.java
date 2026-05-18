package com.io.begstd.slot.model.app;

import com.io.begstd.slot.model.domain.Money;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
public class BasePlaySessionTest {
    @Test
    public void setDefaultValueTest() {
        List<String> normalGamePayLines = new ArrayList<>();
        normalGamePayLines.add("abc");
        normalGamePayLines.add("bcd");
        
        Money winAmount = Money.of(1000);
        
        BasePlaySession.BasePlaySessionBuilder psBuilder = BasePlaySession.builder();
        psBuilder.normalGamePayLines(normalGamePayLines);
        psBuilder.winAmount(winAmount);
        
        BasePlaySession basePlaySession = psBuilder.build();
        
        psBuilder = basePlaySession.toBuilder(); // new builder
        
        BasePlaySession newBasePlaySession = psBuilder.build();
        
        Assert.assertEquals(normalGamePayLines, newBasePlaySession.normalGamePayLines());
        Assert.assertEquals(winAmount, newBasePlaySession.winAmount());
    }
}
