package com.koch.ambeth.ioc.util;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.cancel.ICancellation;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.IForkState;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedConsumer;
import com.koch.ambeth.util.function.CheckedFunction;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class MultithreadingHelper implements IMultithreadingHelper {
    /**
     * Defines the maximal amount of time threads is given to run a parallel task.
     */
    public static final String TIMEOUT = "ambeth.mth.timeout";
    @Autowired
    protected ICancellation cancellation;
    @Autowired
    protected IClassLoaderProvider classLoaderProvider;
    @Autowired(optional = true)
    protected ExecutorService executor;
    @Autowired
    protected IThreadLocalCleanupController threadLocalCleanupController;
    @Property(name = TIMEOUT, defaultValue = "30000")
    protected long timeout;
    @Property(name = IocConfigurationConstants.TransparentParallelizationActive, defaultValue = "true")
    protected boolean transparentParallelizationActive;

    @Override
    public void invokeInParallel(IServiceContext serviceContext, Runnable runnable, int workerCount) {
        invokeInParallel(serviceContext, false, timeout, runnable, workerCount);
    }

    @Override
    public void invokeInParallel(IServiceContext serviceContext, boolean inheritThreadLocals, long timeout, Runnable runnable, int workerCount) {
        Runnable[] runnables = new Runnable[workerCount];
        for (int a = workerCount; a-- > 0; ) {
            if (runnable instanceof INamedRunnable) {
                String name = ((INamedRunnable) runnable).getName() + "-" + a;
                runnables[a] = new WrappingNamedRunnable(runnable, name);
            } else {
                runnables[a] = runnable;
            }
        }
        invokeInParallel(serviceContext, inheritThreadLocals, timeout, runnables);
    }

    @Override
    public void invokeInParallel(final IServiceContext serviceContext, Runnable... runnables) {
        invokeInParallel(serviceContext, false, timeout, runnables);
    }

    @Override
    public void invokeInParallel(IServiceContext serviceContext, boolean inheritThreadLocals, long timeout, Runnable... runnables) {
        CountDownLatch latch = new CountDownLatch(runnables.length);
        ParamHolder<Throwable> throwableHolder = new ParamHolder<>();
        IForkState forkState = inheritThreadLocals ? threadLocalCleanupController.createForkState() : null;

        Thread[] threads = new Thread[runnables.length];
        for (int a = runnables.length; a-- > 0; ) {
            Runnable catchingRunnable = new CatchingRunnable(forkState, runnables[a], latch, throwableHolder, cancellation, threadLocalCleanupController);

            Thread thread = new Thread(catchingRunnable);
            thread.setContextClassLoader(classLoaderProvider.getClassLoader());
            thread.setDaemon(true);
            threads[a] = thread;
        }
        for (Thread thread : threads) {
            thread.start();
        }
        try {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw RuntimeExceptionUtil.mask(e);
        }
        if (throwableHolder.getValue() != null) {
            throw RuntimeExceptionUtil.mask(throwableHolder.getValue(), "Error occured while invoking runnables");
        }
    }

    protected boolean isMultiThreadingAllowed() {
        return transparentParallelizationActive && executor != null;
    }

    @Override
    public <R, V> void invokeAndWait(List<V> items, CheckedFunction<V, R> itemHandler, IAggregrateResultHandler<R, V> aggregateResultHandler) {
        if (items.isEmpty()) {
            return;
        }
        if (!isMultiThreadingAllowed() || items.size() == 1) {
            for (int a = items.size(); a-- > 0; ) {
                V item = items.get(a);
                R result = CheckedFunction.invoke(itemHandler, item);
                if (aggregateResultHandler != null) {
                    aggregateResultHandler.aggregateResult(result, item);
                }
            }
            return;
        }
        ResultingRunnableHandle<R, V> runnableHandle =
                new ResultingRunnableHandle<>(new CancellationCheckResultingBackgroundWorker<>(itemHandler), aggregateResultHandler, new ArrayList<>(items), threadLocalCleanupController);

        Runnable parallelRunnable = new ResultingParallelRunnable<>(runnableHandle, true);
        Runnable mainRunnable = new ResultingParallelRunnable<>(runnableHandle, false);

        queueAndWait(items.size() - 1, parallelRunnable, mainRunnable, runnableHandle);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R, K, V> void invokeAndWait(Map<K, V> items, CheckedFunction<Entry<K, V>, R> itemHandler, IAggregrateResultHandler<R, Entry<K, V>> aggregateResultHandler) {
        if (items.isEmpty()) {
            return;
        }
        if (!isMultiThreadingAllowed() || items.size() == 1) {
            if (items instanceof Iterable) {
                for (Entry<K, V> item : (Iterable<Entry<K, V>>) items) {
                    R result = CheckedFunction.invoke(itemHandler, item);
                    if (aggregateResultHandler != null) {
                        aggregateResultHandler.aggregateResult(result, item);
                    }
                }
            } else {
                for (Entry<K, V> item : items.entrySet()) {
                    R result = CheckedFunction.invoke(itemHandler, item);
                    if (aggregateResultHandler != null) {
                        aggregateResultHandler.aggregateResult(result, item);
                    }
                }
            }
            return;
        }
        var itemsList = new ArrayList<Entry<K, V>>(items.size());
        if (items instanceof Iterable) {
            for (var item : (Iterable<Entry<K, V>>) items) {
                itemsList.add(item);
            }
        } else {
            for (var item : items.entrySet()) {
                itemsList.add(item);
            }
        }
        var runnableHandle =
                new ResultingRunnableHandle<>(new CancellationCheckResultingBackgroundWorker<Entry<K, V>, R>(itemHandler), aggregateResultHandler, itemsList, threadLocalCleanupController);

        var parallelRunnable = new ResultingParallelRunnable<>(runnableHandle, true);
        var mainRunnable = new ResultingParallelRunnable<>(runnableHandle, false);

        queueAndWait(items.size() - 1, parallelRunnable, mainRunnable, runnableHandle);
    }

    @Override
    public <V> void invokeAndWait(List<V> items, CheckedConsumer<V> itemHandler) {
        if (items.isEmpty()) {
            return;
        }
        if (!isMultiThreadingAllowed() || items.size() == 1) {
            for (int a = items.size(); a-- > 0; ) {
                V item = items.get(a);
                CheckedConsumer.invoke(itemHandler, item);
            }
            return;
        }
        var runnableHandle = new RunnableHandle<V>(new CancellationCheckBackgroundWorker<>(itemHandler), new ArrayList<>(items), threadLocalCleanupController);

        var parallelRunnable = new ParallelRunnable<>(runnableHandle, true);
        var mainRunnable = new ParallelRunnable<>(runnableHandle, false);

        queueAndWait(items.size() - 1, parallelRunnable, mainRunnable, runnableHandle);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> void invokeAndWait(Map<K, V> items, CheckedConsumer<Entry<K, V>> itemHandler) {
        if (items.isEmpty()) {
            return;
        }
        if (!isMultiThreadingAllowed() || items.size() == 1) {
            if (items instanceof Iterable) {
                for (var item : (Iterable<Entry<K, V>>) items) {
                    CheckedConsumer.invoke(itemHandler, item);
                }
            } else {
                for (var item : items.entrySet()) {
                    CheckedConsumer.invoke(itemHandler, item);
                }
            }
            return;
        }
        var itemsList = new ArrayList<Entry<K, V>>(items.size());
        if (items instanceof Iterable) {
            for (var item : (Iterable<Entry<K, V>>) items) {
                itemsList.add(item);
            }
        } else {
            for (var item : items.entrySet()) {
                itemsList.add(item);
            }
        }
        var runnableHandle = new RunnableHandle<Entry<K, V>>(new CancellationCheckBackgroundWorker<>(itemHandler), itemsList, threadLocalCleanupController);

        var parallelRunnable = new ParallelRunnable<>(runnableHandle, true);
        var mainRunnable = new ParallelRunnable<>(runnableHandle, false);

        queueAndWait(items.size() - 1, parallelRunnable, mainRunnable, runnableHandle);
    }

    protected <V> void queueAndWait(int forkCount, Runnable parallelRunnable, Runnable mainRunnable, AbstractRunnableHandle<V> runnableHandle) {
        // for n items fork at most n - 1 threads because our main thread behaves like a worker by
        // itself
        for (int a = forkCount; a-- > 0; ) {
            executor.execute(parallelRunnable);
        }
        // consume items with the "main thread" as long as there is one in the queue
        mainRunnable.run();

        // wait till the forked threads have finished, too
        waitForLatch(runnableHandle.latch, runnableHandle);

        runnableHandle.forkState.reintegrateForkedValues();
    }

    protected void waitForLatch(CountDownLatch latch, InterruptingParamHolder exHolder) {
        while (true) {
            Throwable ex = exHolder.getValue();
            if (ex != null) {
                // A parallel exception will be thrown here
                throw RuntimeExceptionUtil.mask(ex);
            }
            try {
                latch.await();
                if (latch.getCount() == 0) {
                    return;
                }
            } catch (InterruptedException e) {
                // intended blank
                // will be thrown if a foreign thread throws an exception an is stored in the paramHolder
            }
        }
    }

    @Value
    private class CancellationCheckBackgroundWorker<V> implements CheckedConsumer<V> {
        CheckedConsumer<V> itemHandler;

        @Override
        public void accept(final V state) throws Exception {
            cancellation.withCancellationAwareness(itemHandler, state);
        }
    }

    @Value
    private class CancellationCheckResultingBackgroundWorker<V, R> implements CheckedFunction<V, R> {
        CheckedFunction<V, R> itemHandler;

        @Override
        public R apply(V state) throws Exception {
            return cancellation.withCancellationAwareness(itemHandler, state);
        }
    }
}
