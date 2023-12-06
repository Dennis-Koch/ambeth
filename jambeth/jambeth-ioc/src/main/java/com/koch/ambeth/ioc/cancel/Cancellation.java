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

import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.function.CheckedConsumer;
import com.koch.ambeth.util.function.CheckedFunction;
import com.koch.ambeth.util.function.CheckedRunnable;
import com.koch.ambeth.util.function.CheckedSupplier;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.StateRollback;
import lombok.SneakyThrows;

public class Cancellation implements ICancellation, ICancellationWritable, IThreadLocalCleanupBean {
    @Forkable
    protected final ThreadLocal<ICancellationHandle> cancelledTL = new ThreadLocal<>();

    @Override
    public void cleanupThreadLocal() {
        cancelledTL.set(null);
    }

    @Override
    public boolean isCancelled() {
        var cancellationHandle = cancelledTL.get();
        if (cancellationHandle == null) {
            return false;
        }
        return cancellationHandle.isCancelled();
    }

    @SneakyThrows
    @Override
    public <V> void withCancellationAwareness(CheckedConsumer<V> consumer, V state) {
        ensureNotCancelled();
        var cancellationHandle = cancelledTL.get();
        if (cancellationHandle == null) {
            consumer.accept(state);
            return;
        }
        cancellationHandle.withCancellationAwareness(consumer, state);
    }

    @SneakyThrows
    @Override
    public void withCancellationAwareness(CheckedRunnable runnable) {
        ensureNotCancelled();
        var cancellationHandle = cancelledTL.get();
        if (cancellationHandle == null) {
            runnable.run();
        }
        cancellationHandle.withCancellationAwareness(runnable);
    }


    @SneakyThrows
    @Override
    public <R> R withCancellationAwareness(CheckedSupplier<R> supplier) {
        ensureNotCancelled();
        var cancellationHandle = cancelledTL.get();
        if (cancellationHandle == null) {
            return supplier.get();
        }
        return cancellationHandle.withCancellationAwareness(supplier);
    }

    @SneakyThrows
    @Override
    public <R, V> R withCancellationAwareness(CheckedFunction<V, R> function, V state) {
        ensureNotCancelled();
        var cancellationHandle = cancelledTL.get();
        if (cancellationHandle == null) {
            return function.apply(state);
        }
        return cancellationHandle.withCancellationAwareness(function, state);
    }

    @Override
    public void ensureNotCancelled() {
        if (isCancelled()) {
            throw new CancelledException();
        }
    }

    @Override
    public ICancellationHandle getEnsureCancellationHandle() {
        ensureNotCancelled();
        var cancellationHandle = cancelledTL.get();
        if (cancellationHandle == null) {
            cancellationHandle = createUnassignedCancellationHandle();
            cancelledTL.set(cancellationHandle);
        }
        return cancellationHandle;
    }

    @Override
    public ICancellationHandle createUnassignedCancellationHandle() {
        return new CancellationHandle(this);
    }

    @Override
    public IStateRollback pushCancellationHandle(final ICancellationHandle cancellationHandle) {
        ParamChecker.assertParamNotNull(cancellationHandle, "cancellationHandle");
        return StateRollback.chain(chain -> {
            chain.append(((CancellationHandle) cancellationHandle).addOwningThread());

            var oldCancellationHandle = cancelledTL.get();
            cancelledTL.set(cancellationHandle);
            chain.append(() -> cancelledTL.set(oldCancellationHandle));
        });
    }
}
