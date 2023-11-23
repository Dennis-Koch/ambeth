package com.koch.ambeth.merge.cache;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedRunnable;

public class CacheModification implements ICacheModification, IThreadLocalCleanupBean {
    private static final Integer ACTIVE = Integer.valueOf(1), FLUSHING = Integer.valueOf(2);
    // Intentionally no SensitiveThreadLocal
    @Forkable
    protected final ThreadLocal<Integer> activeTL = new ThreadLocal<>();
    @Forkable
    protected final ThreadLocal<Boolean> internalUpdateTL = new ThreadLocal<>();
    protected final ThreadLocal<ArrayList<CheckedRunnable>> queuedEventsTL = new ThreadLocal<>();
    @LogInstance
    private ILogger log;

    @Override
    public void cleanupThreadLocal() {
        activeTL.remove();
        internalUpdateTL.remove();
        queuedEventsTL.remove();
    }

    @Override
    public boolean isActiveOrFlushing() {
        return activeTL.get() != null;
    }

    @Override
    public boolean isActive() {
        return ACTIVE.equals(activeTL.get());
    }

    @Override
    public void setActive(boolean active) {
        var existingIsActive = isActive();
        if (existingIsActive == active) {
            return;
        }
        if (existingIsActive) {
            activeTL.set(FLUSHING);
            try {
                fireQueuedPropertyChangeEvents();
            } finally {
                activeTL.remove();
            }
        } else {
            activeTL.set(ACTIVE);
        }
    }

    @Override
    public boolean isInternalUpdate() {
        var internalUpdate = internalUpdateTL.get();
        return internalUpdate != null ? internalUpdate.booleanValue() : false;
    }

    @Override
    public void setInternalUpdate(boolean internalUpdate) {
        if (internalUpdate) {
            internalUpdateTL.set(Boolean.TRUE);
        } else {
            internalUpdateTL.remove();
        }
    }

    @Override
    public boolean isActiveOrFlushingOrInternalUpdate() {
        return isActiveOrFlushing() || isInternalUpdate();
    }

    @Override
    public void queuePropertyChangeEvent(CheckedRunnable task) {
        if (!isActive()) {
            throw new IllegalStateException("Not supported if isActive() is 'false'");
        }
        var queuedEvents = queuedEventsTL.get();
        if (queuedEvents == null) {
            queuedEvents = new ArrayList<>();
            queuedEventsTL.set(queuedEvents);
        }
        queuedEvents.add(task);
    }

    protected void fireQueuedPropertyChangeEvents() {
        var queuedEvents = queuedEventsTL.get();
        if (queuedEvents == null) {
            return;
        }
        queuedEventsTL.remove();
        try {
            for (int a = 0, size = queuedEvents.size(); a < size; a++) {
                var queuedEvent = queuedEvents.get(a);
                queuedEvent.run();
            }
        } catch (Throwable e) {
            log.error(e);
            throw RuntimeExceptionUtil.mask(e);
        }
    }
}
