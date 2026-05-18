package com.io.begstd.slot.model.gamerule.cluster;

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
public class FeatureConfig implements Serializable {

    private static final long serialVersionUID = 2132485678946960122L;
    
    private int id;

    private int freespin;
    private int csymwin;
    
    private List<Integer> vertex;
    
    private List<String> msymbol;
    private List<String> esymbol;
    
    private List<Integer> percent;
    
}
