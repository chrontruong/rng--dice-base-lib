package com.io.begstd.dice.model.gamerule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Component
public class InitJackpotChild implements Serializable {

    private static final long serialVersionUID = -6717547793919763859L;

    private int id;

    private String code;
    
    private double initAmount;
    
    private float progressive;

    private boolean hasEvent;
    
}
