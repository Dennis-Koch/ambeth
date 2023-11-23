package com.koch.ambeth.ioc.hierarchy;

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

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.function.CheckedConsumer;

public class PrototypeContextHandle implements IContextHandle {
    protected final ISet<IServiceContext> childContexts = new HashSet<>();
    @Autowired
    protected IContextFactory childContextFactory;
    @LogInstance
    private ILogger log;

    @Override
    public IServiceContext start() {
        var childContext = childContextFactory.createChildContext(null);
        childContexts.add(childContext);
        return childContext;
    }

    @Override
    public IServiceContext start(final IMap<String, Object> namedBeans) {
        var childContext = childContextFactory.createChildContext(childContextFactory -> {
            for (var entry : namedBeans) {
                var beanName = entry.getKey();
                var bean = entry.getValue();
                childContextFactory.registerExternalBean(beanName, bean);
            }
        });
        childContexts.add(childContext);
        return childContext;
    }

    @Override
    public IServiceContext start(CheckedConsumer<IBeanContextFactory> registerPhaseDelegate) {
        var childContext = childContextFactory.createChildContext(registerPhaseDelegate);
        childContexts.add(childContext);
        return childContext;
    }

    @Override
    public void stop() {
        for (var childContext : childContexts) {
            try {
                childContext.dispose();
            } catch (Throwable e) {
                log.error(e);
            }
        }
        childContexts.clear();
    }
}
