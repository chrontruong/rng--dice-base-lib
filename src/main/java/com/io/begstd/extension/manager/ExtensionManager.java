package com.io.begstd.extension.manager;

import com.io.begstd.extension.Extension;

public interface ExtensionManager {
    void registerExtension(String type, Class clazz) throws Exception;
    void verify();
    ExtensionManager cloneObject();

    <T extends Extension> T getBean(String name);
    <T> T getObject(String name);
    void putObject(String name, Object o);
    void removeObject(String name, Object o);
}
