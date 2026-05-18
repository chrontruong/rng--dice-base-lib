package com.io.begstd.slot.model.gamerule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Component
public class FreeSpinOption implements Serializable {

    private static final long serialVersionUID = 2132485678946960122L;

    private int id;

    private String code;
    
    private int freeSpin;
    
    private List<Float> paytable;
    
    private List<Integer> percent;
}
