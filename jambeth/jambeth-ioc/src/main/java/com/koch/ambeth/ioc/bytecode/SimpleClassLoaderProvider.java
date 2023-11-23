package com.koch.ambeth.ioc.bytecode;

/*-
 * #%L
 * jambeth-ioc
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.state.IStateRollback;

public class SimpleClassLoaderProvider implements IClassLoaderProvider, IInitializingBean {

    public static final String CLASS_LOADER_PROP_NAME = "ClassLoader";

    protected ClassLoader classLoader;

    @Override
    public void afterPropertiesSet() throws Throwable {
        if (classLoader == null) {
            classLoader = Thread.currentThread()
                                .getContextClassLoader();
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public IStateRollback pushClassLoader() {
        final ClassLoader oldCL = Thread.currentThread()
                                        .getContextClassLoader();
        Thread.currentThread()
              .setContextClassLoader(getClassLoader());
        return () -> Thread.currentThread()
                           .setContextClassLoader(oldCL);
    }
}
