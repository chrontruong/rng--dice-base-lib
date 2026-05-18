package com.io.begstd.slot.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Builder
@Accessors(fluent = true)
@AllArgsConstructor
@NoArgsConstructor
public class RedisNotification {
    @JsonProperty("sId")
    private String serviceId;
    @JsonProperty("uId")
    private String userId;
    @JsonProperty("m")
    private String mode;
    @JsonProperty("c")
    private String clazz;
    @JsonProperty("j")
    private String json;
    @JsonProperty("r")
    private String rtp;
}
