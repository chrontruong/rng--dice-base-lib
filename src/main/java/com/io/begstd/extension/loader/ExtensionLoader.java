package com.io.begstd.extension.loader;

import com.io.begstd.extension.factory.ExtensionManagerFactory;
import com.io.begstd.extension.manager.ExtensionManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ExtensionLoader <T extends ExtensionManager> {

    @Autowired
    private ExtensionManagerFactory extensionManagerFactory;

    @Getter
    private T extensionManager;

    @Autowired
    public void ExtensionLoader() {
        extensionManager = extensionManagerFactory.getInstance();

        extensionManager.verify();
    }

}
