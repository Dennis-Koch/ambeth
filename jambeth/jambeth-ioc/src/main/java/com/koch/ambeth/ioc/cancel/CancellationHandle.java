package com.koch.ambeth.ioc.cancel;

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

import com.koch.ambeth.util.collections.WeakHashSet;
import com.koch.ambeth.util.function.CheckedConsumer;
import com.koch.ambeth.util.function.CheckedFunction;
import com.koch.ambeth.util.function.CheckedRunnable;
import com.koch.ambeth.util.function.CheckedSupplier;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.StateRollback;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CancellationHandle implements ICancellationHandle {
    protected final WeakHashSet<Thread> owningThreads = new WeakHashSet<>();
    protected final Lock writeLock = new ReentrantLock();
    protected volatile boolean cancelled;
    protected Cancellation cancellation;

    public CancellationHandle(Cancellation cancellation) {
        this.cancellation = cancellation;
    }

    public IStateRollback addOwningThread() {
        writeLock.lock();
        try {
            if (owningThreads.add(Thread.currentThread())) {
                return () -> removeOwningThread();
            }
            return StateRollback.empty();
        } finally {
            writeLock.unlock();
        }
    }

    private void removeOwningThread() {
        writeLock.lock();
        try {
            owningThreads.remove(Thread.currentThread());
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void cancel() {
        cancelled = true;
        writeLock.lock();
        try {
            for (Thread owningThread : owningThreads) {
                if (owningThread != null) {
                    owningThread.interrupt();
                }
            }
            owningThreads.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void close() throws Exception {
        Cancellation cancellation = this.cancellation;
        if (cancellation == null) {
            return;
        }
        this.cancellation = null;
        ICancellationHandle cancellationHandle = cancellation.cancelledTL.get();
        if (cancellationHandle != this) {
            throw new IllegalStateException("Only the thread owning this cancellationHandle is allowed to close it");
        }
        cancellation.cancelledTL.set(null);
    }

    @Override
    public <V> void withCancellationAwareness(CheckedConsumer<V> consumer, V state) {
        var rollback = addOwningThread();
        try {
            CheckedConsumer.invoke(consumer, state);
        } finally {
            rollback.rollback();
        }
    }

    @Override
    public void withCancellationAwareness(CheckedRunnable runnable) {
        var rollback = addOwningThread();
        try {
            CheckedRunnable.invoke(runnable);
        } finally {
            rollback.rollback();
        }
    }

    @Override
    public <R> R withCancellationAwareness(CheckedSupplier<R> supplier) {
        var rollback = addOwningThread();
        try {
            return CheckedSupplier.invoke(supplier);
        } finally {
            rollback.rollback();
        }
    }

    @Override
    public <R, V> R withCancellationAwareness(CheckedFunction<V, R> function, V state) {
        var rollback = addOwningThread();
        try {
            return CheckedFunction.invoke(function, state);
        } finally {
            rollback.rollback();
        }
    }
}
