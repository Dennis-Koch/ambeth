package com.koch.ambeth.bytecode.core;

/*-
 * #%L
 * jambeth-bytecode
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.util.IClassByteContentRegistry;
import com.koch.ambeth.util.collections.WeakHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AmbethClassLoader extends ClassLoader implements IClassByteContentRegistry {
    protected final Map<Class<?>, byte[]> classToContentMap = new WeakHashMap<>();

    protected final Map<String, byte[]> classNameToContentMap = new HashMap<>();

    public AmbethClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class<?> defineClass(String name, byte[] b) {
        try {
            Class<?> type = defineClass(name, b, 0, b.length);
            synchronized (classToContentMap) {
                classToContentMap.put(type, b);
            }
            return type;
        } catch (NoClassDefFoundError e) {
            throw RuntimeExceptionUtil.mask(e, "Error occurred while creating '" + name + "' in an Ambeth ClassLoader derived from '" + getParent() + "'");
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        var is = super.getResourceAsStream(name);
        if (is != null) {
            return is;
        }
        synchronized (classNameToContentMap) {
            var content = classNameToContentMap.get(name);
            if (content != null) {
                return new ByteArrayInputStream(content);
            }
        }
        return null;
    }

    @Override
    public void registerContent(Class<?> type, byte[] content) {
        synchronized (classToContentMap) {
            classToContentMap.put(type, content);
        }
        synchronized (classNameToContentMap) {
            classNameToContentMap.put(type.getName().replace('.', '/') + ".class", content);
        }
    }

    public byte[] getContent(Class<?> type) {
        synchronized (classToContentMap) {
            return classToContentMap.get(type);
        }
    }
}
