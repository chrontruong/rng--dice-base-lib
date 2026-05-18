package com.io.begstd.slot.model.config;

import lombok.*;
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
