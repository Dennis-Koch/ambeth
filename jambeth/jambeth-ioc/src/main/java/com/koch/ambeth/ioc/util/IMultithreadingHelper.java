package com.koch.ambeth.ioc.util;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.util.function.CheckedConsumer;
import com.koch.ambeth.util.function.CheckedFunction;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public interface IMultithreadingHelper {
    /**
     * Tries to execute the code given via the "itemHandler" in forked threads in addition to the
     * current thread. The current thread will behave the same as its forked threads:<br>
     * <br>
     * All acquired threads consume the "items" list till it is empty. The current thread will
     * terminate from this method only if the "items" list is empty and all forked threads have
     * finished their work on processing their last item.
     * <p>
     * Note that the implementation does not necessarily fork any thread at all - e.g. considering the
     * number of assigned cpus to the VM in some cases forks do not make sense.
     *
     * @param items                  The overall amount of items which can be processed concurrent from each other.
     *                               Must be non-null, can have a size of zero.
     * @param itemHandler            The code which gets called either by any forked thread or the current thread
     *                               processing a single item from "items". Must be non-null.
     * @param aggregateResultHandler Aggregating code which will be executed safely via an internal
     *                               exclusive lock in the scope of the current workers (potentially forked threads and the
     *                               current threads).
     */
    <R, V> void invokeAndWait(List<V> items, CheckedFunction<V, R> itemHandler, IAggregrateResultHandler<R, V> aggregateResultHandler);

    <R, K, V> void invokeAndWait(Map<K, V> items, CheckedFunction<Entry<K, V>, R> itemHandler, IAggregrateResultHandler<R, Entry<K, V>> aggregateResultHandler);

    <V> void invokeAndWait(List<V> items, CheckedConsumer<V> itemHandler);

    <K, V> void invokeAndWait(Map<K, V> items, CheckedConsumer<Entry<K, V>> itemHandler);

    void invokeInParallel(IServiceContext serviceContext, Runnable runnable, int workerCount);

    void invokeInParallel(IServiceContext serviceContext, Runnable... runnables);

    void invokeInParallel(IServiceContext serviceContext, boolean inheritThreadLocals, long timeout, Runnable runnable, int workerCount);

    void invokeInParallel(IServiceContext serviceContext, boolean inheritThreadLocals, long timeout, Runnable... runnables);
}
