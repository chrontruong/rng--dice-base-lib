package com.io.begstd.slot.services.internal;

import java.util.function.Function;

public interface IRandomService {
    void register(String cmd, Function<Object[], Object> data);

    void clearExtension();

    <T> T random(String cmd, Class<T> tClass, Object... data);
}
