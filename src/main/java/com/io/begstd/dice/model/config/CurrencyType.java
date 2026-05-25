package com.io.begstd.dice.model.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors(fluent = true)
@Builder(toBuilder = true)
@NoArgsConstructor()
@AllArgsConstructor()
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CurrencyType {
    String name;
    String type;
    int wo;
}
