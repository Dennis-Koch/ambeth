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

import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.function.CheckedConsumer;
import com.koch.ambeth.util.function.CheckedFunction;
import com.koch.ambeth.util.function.CheckedRunnable;
import com.koch.ambeth.util.function.CheckedSupplier;
import com.koch.ambeth.util.state.IStateRollback;
import lombok.SneakyThrows;

import java.util.concurrent.locks.ReentrantLock;

public class ForkState extends ReentrantLock implements IForkState {
    private static final long serialVersionUID = 3277389225453647471L;

    protected final ForkStateEntry[] forkStateEntries;

    protected final IForkedValueResolver[] forkedValueResolvers;

    protected final ArrayList<Object>[] forkedValues;

    @SuppressWarnings("unchecked")
    public ForkState(ForkStateEntry[] forkStateEntries, IForkedValueResolver[] forkedValueResolvers) {
        this.forkStateEntries = forkStateEntries;
        this.forkedValueResolvers = forkedValueResolvers;
        forkedValues = new ArrayList[forkStateEntries.length];
    }

    @SuppressWarnings("unchecked")
    protected IStateRollback setThreadLocals() {
        var forkStateEntries = this.forkStateEntries;
        var forkedValueResolvers = this.forkedValueResolvers;
        var oldValues = new Object[forkedValueResolvers.length];
        for (int a = 0, size = forkStateEntries.length; a < size; a++) {
            var tlHandle = (ThreadLocal<Object>) forkStateEntries[a].valueTL;
            oldValues[a] = tlHandle.get();
            var forkedValue = forkedValueResolvers[a].createForkedValue();
            tlHandle.set(forkedValue);
        }
        return () -> restoreThreadLocals(oldValues);
    }

    @SuppressWarnings("unchecked")
    protected void restoreThreadLocals(Object[] oldValues) {
        var forkStateEntries = this.forkStateEntries;
        var forkedValueResolvers = this.forkedValueResolvers;
        var forkedValues = this.forkedValues;
        lock();
        try {
            for (int a = 0, size = forkStateEntries.length; a < size; a++) {
                var forkStateEntry = forkStateEntries[a];
                var tlHandle = (ThreadLocal<Object>) forkStateEntry.valueTL;
                var forkedValue = tlHandle.get();
                tlHandle.set(oldValues[a]);
                var forkedValueResolver = forkedValueResolvers[a];
                if (!(forkedValueResolver instanceof ForkProcessorValueResolver)) {
                    continue;
                }
                var forkedValuesItem = forkedValues[a];
                if (forkedValuesItem == null) {
                    forkedValuesItem = new ArrayList<>();
                    forkedValues[a] = forkedValuesItem;
                }
                forkedValuesItem.add(forkedValue);
            }
        } finally {
            unlock();
        }
    }

    @Override
    public void use(Runnable runnable) {
        var rollback = setThreadLocals();
        try {
            runnable.run();
        } finally {
            rollback.rollback();
        }
    }

    @SneakyThrows
    @Override
    public void use(CheckedRunnable runnable) {
        var rollback = setThreadLocals();
        try {
            runnable.run();
        } finally {
            rollback.rollback();
        }
    }

    @SneakyThrows
    @Override
    public <V> void use(CheckedConsumer<V> runnable, V arg) {
        var rollback = setThreadLocals();
        try {
            runnable.accept(arg);
        } finally {
            rollback.rollback();
        }
    }

    @SneakyThrows
    @Override
    public <R> R use(CheckedSupplier<R> runnable) {
        var rollback = setThreadLocals();
        try {
            return runnable.get();
        } finally {
            rollback.rollback();
        }
    }

    @SneakyThrows
    @Override
    public <R, V> R use(CheckedFunction<V, R> runnable, V arg) {
        var rollback = setThreadLocals();
        try {
            return runnable.apply(arg);
        } finally {
            rollback.rollback();
        }
    }

    @Override
    public void reintegrateForkedValues() {
        var forkStateEntries = this.forkStateEntries;
        var forkedValueResolvers = this.forkedValueResolvers;
        var forkedValues = this.forkedValues;
        for (int a = 0, size = forkStateEntries.length; a < size; a++) {
            var forkStateEntry = forkStateEntries[a];
            var forkedValuesItem = forkedValues[a];

            if (forkedValuesItem == null) {
                // nothing to do
                continue;
            }
            var originalValue = forkedValueResolvers[a].getOriginalValue();
            for (int b = 0, sizeB = forkedValuesItem.size(); b < sizeB; b++) {
                var forkedValue = forkedValuesItem.get(b);
                forkStateEntry.forkProcessor.returnForkedValue(originalValue, forkedValue);
            }
        }
    }
}
