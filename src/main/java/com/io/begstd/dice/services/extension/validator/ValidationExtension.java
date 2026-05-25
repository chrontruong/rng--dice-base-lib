package com.io.begstd.dice.services.extension.validator;

import com.io.begstd.extension.Extension;

public interface ValidationExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }
}
