package com.io.begstd.extension;

public interface Extension {
    default String getName() {
        return this.getClass().getName();
    }

}
