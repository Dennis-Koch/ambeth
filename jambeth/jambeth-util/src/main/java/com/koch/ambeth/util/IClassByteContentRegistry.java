package com.koch.ambeth.util;

/**
 * Marker interface for custom ClassLoaders that are interested in the bytecode of dynamically generated classes - e.g. by Javassist or Bytebuddy
 */
public interface IClassByteContentRegistry {
    void registerContent(Class<?> type, byte[] content);
}
