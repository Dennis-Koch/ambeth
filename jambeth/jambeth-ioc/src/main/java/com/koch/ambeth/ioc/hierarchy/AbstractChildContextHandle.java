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

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.function.CheckedConsumer;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractChildContextHandle implements IInitializingBean, IContextHandle, IDisposableBean {
    protected final Lock writeLock = new ReentrantLock();
    protected IContextFactory contextFactory;

    protected CheckedConsumer<IBeanContextFactory> registerPhaseDelegate;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(contextFactory, "ContextFactory");
    }

    @Override
    public void destroy() throws Throwable {
        stop();
    }

    public void setContextFactory(IContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    public void setRegisterPhaseDelegate(CheckedConsumer<IBeanContextFactory> registerPhaseDelegate) {
        this.registerPhaseDelegate = registerPhaseDelegate;
    }

    protected abstract IServiceContext getChildContext();

    protected abstract void setChildContext(IServiceContext childContext);

    @Override
    public IServiceContext start() {
        IServiceContext childContext = null;
        Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Looking for existing child context...");
            }
            childContext = getChildContext();
            if (childContext == null || childContext.isDisposed()) {
                if (log.isDebugEnabled()) {
                    log.debug("No valid child context found. Creating new child context");
                }
                childContext = contextFactory.createChildContext(registerPhaseDelegate);
                setChildContext(childContext);
            } else if (log.isDebugEnabled()) {
                log.debug("Existing child context found and valid");
            }
            IList<IUpwakingBean> upwakingBeans = childContext.getImplementingObjects(IUpwakingBean.class);
            for (int a = 0, size = upwakingBeans.size(); a < size; a++) {
                upwakingBeans.get(a).wakeUp();
            }
        } finally {
            writeLock.unlock();
        }
        return childContext;
    }

    @Override
    public IServiceContext start(final IMap<String, Object> namedBeans) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IServiceContext start(CheckedConsumer<IBeanContextFactory> registerPhaseDelegate) {
        if (registerPhaseDelegate == null) {
            return start();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void stop() {
        writeLock.lock();
        try {
            IServiceContext childContext = getChildContext();
            if (childContext != null) {
                childContext.dispose();
                setChildContext(null);
            }
        } finally {
            writeLock.unlock();
        }
    }
}
