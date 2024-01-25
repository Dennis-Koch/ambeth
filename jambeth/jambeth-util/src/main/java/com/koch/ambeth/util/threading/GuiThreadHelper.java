package com.koch.ambeth.util.threading;

/*-
 * #%L
 * jambeth-util
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

import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedFunction;
import com.koch.ambeth.util.function.CheckedRunnable;
import com.koch.ambeth.util.function.CheckedSupplier;
import lombok.Setter;
import lombok.SneakyThrows;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

public class GuiThreadHelper implements IGuiThreadHelper {
    private static CheckedSupplier<Boolean> dispatchThreadResolver;

    static {
        dispatchThreadResolver = createDispatchThreadResolver();
    }

    /**
     * Returns a delegate which evaluates to "true" if and only if a valid instance of an AWT event
     * dispatch thread can be found. There may be a valid <code>Toolkit</code> instance or even a
     * valid <code>EventQueue</code> handle. But only with a valid active dispatch thread the method
     * returns "true".
     *
     * @return null, if any kind of error occurs
     */
    private static CheckedSupplier<Boolean> createDispatchThreadResolver() {
        try {
            final Field f_toolkit = ReflectUtil.getDeclaredField(Toolkit.class, "toolkit");
            final Method m_systemEventQueueImpl = ReflectUtil.getDeclaredMethod(false, Toolkit.class, EventQueue.class, "getSystemEventQueueImpl");
            final Field f_dispatchThread = ReflectUtil.getDeclaredField(EventQueue.class, "dispatchThread");

            return () -> {
                Object toolkit = f_toolkit != null ? f_toolkit.get(null) : null;
                if (toolkit == null) {
                    return Boolean.FALSE;
                }
                Object eventQueue = m_systemEventQueueImpl.invoke(toolkit);
                if (eventQueue == null) {
                    return Boolean.FALSE;
                }
                Object dispatchThread = f_dispatchThread != null ? f_dispatchThread.get(eventQueue) : null;
                return dispatchThread != null && ((Thread) dispatchThread).isAlive();
            };
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * Checks whether a valid AWT dispatch thread instance can be found
     *
     * @return true if a valid AWT dispatch thread instance can be bound
     */
    @SneakyThrows
    public static boolean hasUiThread() {
        return dispatchThreadResolver != null && dispatchThreadResolver.get().booleanValue();
    }

    protected Executor executor;

    protected boolean isGuiInitialized, skipGuiInitializeCheck;

    @Setter
    protected boolean javaUiActive;

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public boolean isGuiInitialized() {
        if (!javaUiActive) {
            return false;
        }
        if (!isGuiInitialized && !skipGuiInitializeCheck) {
            try {
                isGuiInitialized = hasUiThread();
            } catch (Throwable e) {
                skipGuiInitializeCheck = true;
            }
        }
        return isGuiInitialized;
    }

    @Override
    public boolean isInGuiThread() {
        return isGuiInitialized() ? EventQueue.isDispatchThread() : false;
    }

    @SneakyThrows
    @Override
    public void invokeInGuiAndWait(final CheckedRunnable runnable) {
        if (!isGuiInitialized() || EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            try {
                EventQueue.invokeAndWait(() -> CheckedRunnable.invoke(runnable));
            } catch (InvocationTargetException | InterruptedException e) {
                throw RuntimeExceptionUtil.mask(e);
            }
        }
    }

    @SneakyThrows
    @Override
    public <R> R invokeInGuiAndWait(final CheckedSupplier<R> supplier) {
        if (!isGuiInitialized() || EventQueue.isDispatchThread()) {
            return supplier.get();
        } else {
            final ParamHolder<R> ph = new ParamHolder<>();
            try {
                EventQueue.invokeAndWait(() -> {
                    var result = CheckedSupplier.invoke(supplier);
                    ph.setValue(result);
                });
                return ph.getValue();
            } catch (InvocationTargetException | InterruptedException e) {
                throw RuntimeExceptionUtil.mask(e);
            }
        }
    }

    @SneakyThrows
    @Override
    public <R, S> R invokeInGuiAndWait(final CheckedFunction<S, R> runnable, final S state) {
        if (!isGuiInitialized() || EventQueue.isDispatchThread()) {
            return runnable.apply(state);
        } else {
            var ph = new ParamHolder<R>();
            try {
                EventQueue.invokeAndWait(() -> {
                    var result = CheckedFunction.invoke(runnable, state);
                    ph.setValue(result);
                });
                return ph.getValue();
            } catch (InvocationTargetException | InterruptedException e) {
                throw RuntimeExceptionUtil.mask(e);
            }
        }
    }

    @Override
    public void invokeInGuiAndWait(final ISendOrPostCallback runnable, final Object state) {
        if (!isGuiInitialized() || EventQueue.isDispatchThread()) {
            runnable.invoke(state);
        } else {
            try {
                EventQueue.invokeAndWait(() -> {
                    try {
                        runnable.invoke(state);
                    } catch (Exception e) {
                        throw RuntimeExceptionUtil.mask(e);
                    }
                });
            } catch (InvocationTargetException | InterruptedException e) {
                throw RuntimeExceptionUtil.mask(e);
            }
        }
    }

    @SneakyThrows
    @Override
    public void invokeInGui(final CheckedRunnable runnable) {
        if (!isGuiInitialized() || EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            EventQueue.invokeLater(() -> CheckedRunnable.invoke(runnable));
        }
    }

    @Override
    public void invokeInGui(final ISendOrPostCallback runnable, final Object state) {
        if (!isGuiInitialized() || EventQueue.isDispatchThread()) {
            runnable.invoke(state);
        } else {
            EventQueue.invokeLater(() -> {
                try {
                    runnable.invoke(state);
                } catch (Exception e) {
                    throw RuntimeExceptionUtil.mask(e);
                }
            });
        }
    }

    @Override
    public void invokeInGuiLate(CheckedRunnable runnable) {
        invokeInGui(runnable);
    }

    @Override
    public void invokeInGuiLate(ISendOrPostCallback runnable, Object state) {
        invokeInGui(runnable, state);
    }

    @SneakyThrows
    @Override
    public void invokeOutOfGui(CheckedRunnable runnable) {
        if (executor == null || !isGuiInitialized() || !EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            executor.execute(() -> CheckedRunnable.invoke(runnable));
        }
    }
}
