package com.io.begstd.extension.factory;

import com.io.begstd.extension.manager.ExtensionManager;

public interface ExtensionManagerFactory {
    <T extends ExtensionManager> T getInstance();
}
