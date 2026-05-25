package com.io.begstd.dice.config.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletMappingConfig {
    String agent;
    String host;
    int port;
}
