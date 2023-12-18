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

    @Override
    public void cleanupThreadLocal() {
        for (var extension : listeners.getExtensionsShared()) {
            extension.cleanupThreadLocal();
        }
        if (objectCollector != null) {
            objectCollector.clearThreadLocal();
        }
    }

    protected ForkStateEntry[] getForkStateEntries() {
        var cachedForkStateEntries = this.cachedForkStateEntries;
        if (cachedForkStateEntries != null) {
            return cachedForkStateEntries;
        }
        var writeLock = listeners.getWriteLock();
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
        var cachedThreadLocalStateEntries = this.cachedThreadLocalStateEntries;
        if (cachedThreadLocalStateEntries != null) {
            return cachedThreadLocalStateEntries;
        }
        var writeLock = listeners.getWriteLock();
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
        var extensions = listeners.getExtensionsShared();
        var forkedStateEntries = new ArrayList<ForkStateEntry>(extensions.length);
        var threadLocalStateEntries = new ArrayList<ForkStateEntry>(extensions.length);
        for (var extension : extensions) {
            var fields = ReflectUtil.getDeclaredFieldsInHierarchy(extension.getClass());
            for (var field : fields) {
                if (!ThreadLocal.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                var valueTL = (ThreadLocal<?>) field.get(extension);
                if (valueTL == null) {
                    continue;
                }
                IForkProcessor forkProcessor = null;
                var forkableType = ForkableType.REFERENCE;
                var forkable = field.getAnnotation(Forkable.class);
                if (forkable == null) {
                    threadLocalStateEntries.add(new ForkStateEntry(extension, field.getName(), valueTL, forkableType, forkProcessor));
                    continue;
                }
                forkableType = forkable.value();
                var forkProcessorType = forkable.processor();
                if (forkProcessorType != null && !IForkProcessor.class.equals(forkProcessorType)) {
                    var beanContextOfExtensionR = extensionToContextMap.get(extension);
                    var beanContextOfExtension = beanContextOfExtensionR != null ? beanContextOfExtensionR.get() : null;
                    if (beanContextOfExtension == null) {
                        throw new IllegalStateException("Not supported, yet");
                    }
                    forkProcessor = beanContextOfExtension.registerBean(forkProcessorType).finish();
                }
                var forkStateEntry = new ForkStateEntry(extension, field.getName(), valueTL, forkableType, forkProcessor);
                forkedStateEntries.add(forkStateEntry);
                threadLocalStateEntries.add(forkStateEntry);
            }
        }
        cachedForkStateEntries = forkedStateEntries.size() > 0 ? forkedStateEntries.toArray(ForkStateEntry[]::new) : ForkStateEntry.EMPTY_ENTRIES;
        cachedThreadLocalStateEntries = threadLocalStateEntries.size() > 0 ? threadLocalStateEntries.toArray(ForkStateEntry[]::new) : ForkStateEntry.EMPTY_ENTRIES;
    }

    @Override
    public IForkState createForkState() {
        ForkStateEntry[] forkStateEntries = getForkStateEntries();

        var oldValues = new IForkedValueResolver[forkStateEntries.length];
        for (int a = 0, size = forkStateEntries.length; a < size; a++) {
            var forkStateEntry = forkStateEntries[a];
            var forkProcessor = forkStateEntry.forkProcessor;
            if (forkProcessor != null) {
                var value = forkProcessor.resolveOriginalValue(forkStateEntry.tlBean, forkStateEntry.fieldName, forkStateEntry.valueTL);
                oldValues[a] = new ForkProcessorValueResolver(value, forkProcessor);
                continue;
            }
            var value = forkStateEntry.valueTL.get();
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
        var writeLock = listeners.getWriteLock();
        writeLock.lock();
        try {
            listeners.register(threadLocalCleanupBean);
            cachedForkStateEntries = null;
            cachedThreadLocalStateEntries = null;
            var currentBeanContext = BeanContextInitializer.getCurrentBeanContext();
            if (currentBeanContext != null) {
                extensionToContextMap.put(threadLocalCleanupBean, new WeakReference<>(currentBeanContext));
                if (alreadyHookedContextSet.putIfNotExists(currentBeanContext, null)) {
                    var inactive = new ParamHolder<Boolean>();

                    currentBeanContext.registerDisposeHook(beanContext -> {
                        if (Boolean.TRUE.equals(inactive.getValue())) {
                            return;
                        }
                        foreignContextHook.accept(beanContext);
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
