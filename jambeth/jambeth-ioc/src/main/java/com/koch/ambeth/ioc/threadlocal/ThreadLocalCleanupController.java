package com.koch.ambeth.ioc.threadlocal;

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

import com.koch.ambeth.ioc.DefaultExtendableContainer;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.factory.BeanContextInitializer;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityWeakHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedConsumer;
import com.koch.ambeth.util.objectcollector.ThreadLocalObjectCollector;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.StateRollback;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.concurrent.locks.Lock;

public class ThreadLocalCleanupController implements IInitializingBean, IDisposableBean, IThreadLocalCleanupBeanExtendable, IThreadLocalCleanupController {
    protected final DefaultExtendableContainer<IThreadLocalCleanupBean> listeners = new DefaultExtendableContainer<>(IThreadLocalCleanupBean.class, "threadLocalCleanupBean");
    protected final IdentityHashMap<IThreadLocalCleanupBean, Reference<IServiceContext>> extensionToContextMap = new IdentityHashMap<>();
    protected final IdentityWeakHashMap<IServiceContext, ParamHolder<Boolean>> alreadyHookedContextSet = new IdentityWeakHashMap<>();
    protected ForkStateEntry[] cachedThreadLocalStateEntries;
    protected ForkStateEntry[] cachedForkStateEntries;
    protected final CheckedConsumer<IServiceContext> foreignContextHook = state -> {
        Lock writeLock = listeners.getWriteLock();
        writeLock.lock();
        try {
            cachedForkStateEntries = null;
            cachedThreadLocalStateEntries = null;
        } finally {
            writeLock.unlock();
        }
    };
    protected IServiceContext beanContext;
    protected ThreadLocalObjectCollector objectCollector;

    @Override
    public void afterPropertiesSet() throws Throwable {
        // Intended blank
    }

    @Override
    public void destroy() throws Throwable {
        cleanupThreadLocal();
    }

    public void setObjectCollector(ThreadLocalObjectCollector objectCollector) {
        this.objectCollector = objectCollector;
    }

    public void setBeanContext(IServiceContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public void cleanupThreadLocal() {
        for (IThreadLocalCleanupBean extension : listeners.getExtensionsShared()) {
            extension.cleanupThreadLocal();
        }
        if (objectCollector != null) {
            objectCollector.clearThreadLocal();
        }
    }

    protected ForkStateEntry[] getForkStateEntries() {
        ForkStateEntry[] cachedForkStateEntries = this.cachedForkStateEntries;
        if (cachedForkStateEntries != null) {
            return cachedForkStateEntries;
        }
        Lock writeLock = listeners.getWriteLock();
        writeLock.lock();
        try {
            // check again: concurrent thread might have been faster
            cachedForkStateEntries = this.cachedForkStateEntries;
            if (cachedForkStateEntries != null) {
                return cachedForkStateEntries;
            }
            acquireStateEntries();
            return this.cachedForkStateEntries;
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        } finally {
            writeLock.unlock();
        }
    }

    protected ForkStateEntry[] getThreadLocalStateEntries() {
        ForkStateEntry[] cachedThreadLocalStateEntries = this.cachedThreadLocalStateEntries;
        if (cachedThreadLocalStateEntries != null) {
            return cachedThreadLocalStateEntries;
        }
        Lock writeLock = listeners.getWriteLock();
        writeLock.lock();
        try {
            // check again: concurrent thread might have been faster
            cachedThreadLocalStateEntries = this.cachedThreadLocalStateEntries;
            if (cachedThreadLocalStateEntries != null) {
                return cachedThreadLocalStateEntries;
            }
            acquireStateEntries();
            return this.cachedThreadLocalStateEntries;
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        } finally {
            writeLock.unlock();
        }
    }

    @SuppressWarnings("resource")
    private void acquireStateEntries() throws IllegalArgumentException, IllegalAccessException {
        IThreadLocalCleanupBean[] extensions = listeners.getExtensionsShared();
        ArrayList<ForkStateEntry> forkedStateEntries = new ArrayList<>(extensions.length);
        ArrayList<ForkStateEntry> threadLocalStateEntries = new ArrayList<>(extensions.length);
        for (IThreadLocalCleanupBean extension : extensions) {
            Field[] fields = ReflectUtil.getDeclaredFieldsInHierarchy(extension.getClass());
            for (Field field : fields) {
                if (!ThreadLocal.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                ThreadLocal<?> valueTL = (ThreadLocal<?>) field.get(extension);
                if (valueTL == null) {
                    continue;
                }
                IForkProcessor forkProcessor = null;
                ForkableType forkableType = ForkableType.REFERENCE;
                Forkable forkable = field.getAnnotation(Forkable.class);
                if (forkable == null) {
                    threadLocalStateEntries.add(new ForkStateEntry(extension, field.getName(), valueTL, forkableType, forkProcessor));
                    continue;
                }
                forkableType = forkable.value();
                Class<? extends IForkProcessor> forkProcessorType = forkable.processor();
                if (forkProcessorType != null && !IForkProcessor.class.equals(forkProcessorType)) {
                    Reference<IServiceContext> beanContextOfExtensionR = extensionToContextMap.get(extension);
                    IServiceContext beanContextOfExtension = beanContextOfExtensionR != null ? beanContextOfExtensionR.get() : null;
                    if (beanContextOfExtension == null) {
                        beanContextOfExtension = beanContext;
                    }
                    forkProcessor = beanContextOfExtension.registerBean(forkProcessorType).finish();
                }
                ForkStateEntry forkStateEntry = new ForkStateEntry(extension, field.getName(), valueTL, forkableType, forkProcessor);
                forkedStateEntries.add(forkStateEntry);
                threadLocalStateEntries.add(forkStateEntry);
            }
        }
        cachedForkStateEntries = forkedStateEntries.size() > 0 ? forkedStateEntries.toArray(ForkStateEntry.class) : ForkStateEntry.EMPTY_ENTRIES;
        cachedThreadLocalStateEntries = threadLocalStateEntries.size() > 0 ? threadLocalStateEntries.toArray(ForkStateEntry.class) : ForkStateEntry.EMPTY_ENTRIES;
    }

    @Override
    public IForkState createForkState() {
        ForkStateEntry[] forkStateEntries = getForkStateEntries();

        IForkedValueResolver[] oldValues = new IForkedValueResolver[forkStateEntries.length];
        for (int a = 0, size = forkStateEntries.length; a < size; a++) {
            ForkStateEntry forkStateEntry = forkStateEntries[a];
            IForkProcessor forkProcessor = forkStateEntry.forkProcessor;
            if (forkProcessor != null) {
                Object value = forkProcessor.resolveOriginalValue(forkStateEntry.tlBean, forkStateEntry.fieldName, forkStateEntry.valueTL);
                oldValues[a] = new ForkProcessorValueResolver(value, forkProcessor);
                continue;
            }
            Object value = forkStateEntry.valueTL.get();
            if (value != null && ForkableType.SHALLOW_COPY.equals(forkStateEntry.forkableType)) {
                if (value instanceof Cloneable) {
                    oldValues[a] = new ShallowCopyValueResolver(value);
                } else {
                    throw new IllegalStateException("Could not clone " + value);
                }
            } else {
                oldValues[a] = new ReferenceValueResolver(value, value);
            }
        }
        return new ForkState(forkStateEntries, oldValues);
    }

    @Override
    public void registerThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean) {
        Lock writeLock = listeners.getWriteLock();
        writeLock.lock();
        try {
            listeners.register(threadLocalCleanupBean);
            cachedForkStateEntries = null;
            cachedThreadLocalStateEntries = null;
            IServiceContext currentBeanContext = BeanContextInitializer.getCurrentBeanContext();
            if (currentBeanContext != null) {
                extensionToContextMap.put(threadLocalCleanupBean, new WeakReference<>(currentBeanContext));
                if (alreadyHookedContextSet.putIfNotExists(currentBeanContext, null)) {
                    final ParamHolder<Boolean> inactive = new ParamHolder<>();

                    currentBeanContext.registerDisposeHook(beanContext -> {
                        if (Boolean.TRUE.equals(inactive.getValue())) {
                            return;
                        }
                        CheckedConsumer.invoke(foreignContextHook, beanContext);
                    });
                    alreadyHookedContextSet.put(currentBeanContext, inactive);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void unregisterThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean) {
        Lock writeLock = listeners.getWriteLock();
        writeLock.lock();
        try {
            listeners.unregister(threadLocalCleanupBean);
            cachedForkStateEntries = null;
            cachedThreadLocalStateEntries = null;
            extensionToContextMap.remove(threadLocalCleanupBean);
        } finally {
            writeLock.unlock();
        }
        // clear this threadlocal a last time before letting the bean alone...
        threadLocalCleanupBean.cleanupThreadLocal();
    }

    @Override
    public IStateRollback pushThreadLocalState() {
        var threadLocalStateEntries = getThreadLocalStateEntries();
        if (threadLocalStateEntries.length == 0) {
            return StateRollback.empty();
        }
        var oldValues = new Object[threadLocalStateEntries.length];
        for (int a = threadLocalStateEntries.length; a-- > 0; ) {
            oldValues[a] = threadLocalStateEntries[a].valueTL.get();
        }
        return () -> {
            for (int a = threadLocalStateEntries.length; a-- > 0; ) {
                ((ThreadLocal<Object>) threadLocalStateEntries[a].valueTL).set(oldValues[a]);
            }
        };
    }
}
