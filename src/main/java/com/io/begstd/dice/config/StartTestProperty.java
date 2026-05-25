package com.io.begstd.dice.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Setter
public class StartTestProperty {
    @Value("${service.startTest}")
    private boolean startTest;
    private String rtp;
    @JsonIgnore
    private int dbType;
    @JsonIgnore
    private String genId;
}
