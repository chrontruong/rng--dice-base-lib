package com.io.begstd.slot.extension;

import com.io.begstd.extension.factory.ExtensionManagerFactory;
import com.io.begstd.extension.loader.ExtensionManagerImpl;
import com.io.begstd.extension.manager.ExtensionManager;
import org.springframework.stereotype.Component;

@Component
public class ExtensionManagerFactoryImpl implements ExtensionManagerFactory {

    @Override
    public ExtensionManager getInstance() {
        return new ExtensionManagerImpl();
    }

}
