package com.io.begstd.slot.model.gamerule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
/**
 * defined free spin config such as freespin number, random wild, random multiple
 * in BDMN
 *
 */
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Component
public class FreeSpinConfig implements Serializable {

    private static final long serialVersionUID = 2132485678946960122L;

    private int id;

    private int freespin;
    
    private List<Integer> wildnbr;
    
    private List<Integer> wildrand;
    
    private List<Integer> multiple;
    
    private List<Integer> multiplerand;
}
