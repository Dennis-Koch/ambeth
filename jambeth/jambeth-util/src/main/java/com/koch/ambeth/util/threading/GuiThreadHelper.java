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

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class GuiThreadHelper implements IGuiThreadHelper {
	private static IResultingBackgroundWorkerDelegate<Boolean> dispatchThreadResolver;

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
	private static IResultingBackgroundWorkerDelegate<Boolean> createDispatchThreadResolver() {
		try {
			final Field f_toolkit = ReflectUtil.getDeclaredField(Toolkit.class, "toolkit");
			final Method m_systemEventQueueImpl = ReflectUtil.getDeclaredMethod(false, Toolkit.class,
					EventQueue.class, "getSystemEventQueueImpl");
			final Field f_dispatchThread = ReflectUtil.getDeclaredField(EventQueue.class,
					"dispatchThread");

			return new IResultingBackgroundWorkerDelegate<Boolean>() {
				@Override
				public Boolean invoke() throws Exception {
					Object toolkit = f_toolkit != null ? f_toolkit.get(null) : null;
					if (toolkit == null) {
						return Boolean.FALSE;
					}
					Object eventQueue = m_systemEventQueueImpl.invoke(toolkit);
					if (eventQueue == null) {
						return Boolean.FALSE;
					}
					Object dispatchThread = f_dispatchThread != null ? f_dispatchThread.get(eventQueue)
							: null;
					return dispatchThread != null && ((Thread) dispatchThread).isAlive();
				}
			};
		}
		catch (Throwable e) {
			return null;
		}
	}

	/**
	 * Checks whether a valid AWT dispatch thread instance can be found
	 *
	 * @return true if a valid AWT dispatch thread instance can be bound
	 */
	public static boolean hasUiThread() {
		try {
			return dispatchThreadResolver != null && dispatchThreadResolver.invoke().booleanValue();
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected Executor executor;

	protected boolean isGuiInitialized, skipGuiInitializeCheck, javaUiActive;

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
			}
			catch (Throwable e) {
				skipGuiInitializeCheck = true;
			}
		}
		return isGuiInitialized;
	}

	@Override
	public boolean isInGuiThread() {
		return isGuiInitialized() ? EventQueue.isDispatchThread() : false;
	}

	@Override
	public void invokeInGuiAndWait(final IBackgroundWorkerDelegate runnable) {
		if (!isGuiInitialized() || EventQueue.isDispatchThread()) {
			try {
				runnable.invoke();
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		else {
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						try {
							runnable.invoke();
						}
						catch (Exception e) {
							throw RuntimeExceptionUtil.mask(e);
						}
					}
				});
			}
			catch (InvocationTargetException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
			catch (InterruptedException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public <R> R invokeInGuiAndWait(final IResultingBackgroundWorkerDelegate<R> runnable) {
		if (!isGuiInitialized() || EventQueue.isDispatchThread()) {
			try {
				return runnable.invoke();
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		else {
			final ParamHolder<R> ph = new ParamHolder<>();
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						try {
							ph.setValue(runnable.invoke());
						}
						catch (Exception e) {
							throw RuntimeExceptionUtil.mask(e);
						}
					}
				});
				return ph.getValue();
			}
			catch (InvocationTargetException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
			catch (InterruptedException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public <R, S> R invokeInGuiAndWait(final IResultingBackgroundWorkerParamDelegate<R, S> runnable,
			final S state) {
		if (!isGuiInitialized() || EventQueue.isDispatchThread()) {
			try {
				return runnable.invoke(state);
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		else {
			final ParamHolder<R> ph = new ParamHolder<>();
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						try {
							ph.setValue(runnable.invoke(state));
						}
						catch (Exception e) {
							throw RuntimeExceptionUtil.mask(e);
						}
					}
				});
				return ph.getValue();
			}
			catch (InvocationTargetException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
			catch (InterruptedException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public void invokeInGuiAndWait(final ISendOrPostCallback runnable, final Object state) {
		if (!isGuiInitialized() || EventQueue.isDispatchThread()) {
			try {
				runnable.invoke(state);
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		else {
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						try {
							runnable.invoke(state);
						}
						catch (Exception e) {
							throw RuntimeExceptionUtil.mask(e);
						}
					}
				});
			}
			catch (InvocationTargetException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
			catch (InterruptedException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public void invokeInGui(final IBackgroundWorkerDelegate runnable) {
		if (!isGuiInitialized() || EventQueue.isDispatchThread()) {
			try {
				runnable.invoke();
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						runnable.invoke();
					}
					catch (Exception e) {
						throw RuntimeExceptionUtil.mask(e);
					}
				}
			});
		}
	}

	@Override
	public void invokeInGui(final ISendOrPostCallback runnable, final Object state) {
		if (!isGuiInitialized() || EventQueue.isDispatchThread()) {
			try {
				runnable.invoke(state);
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						runnable.invoke(state);
					}
					catch (Exception e) {
						throw RuntimeExceptionUtil.mask(e);
					}
				}
			});
		}
	}

	@Override
	public void invokeInGuiLate(IBackgroundWorkerDelegate runnable) {
		invokeInGui(runnable);
	}

	@Override
	public void invokeInGuiLate(ISendOrPostCallback runnable, Object state) {
		invokeInGui(runnable, state);
	}

	@Override
	public void invokeOutOfGui(final IBackgroundWorkerDelegate runnable) {
		if (executor == null || !isGuiInitialized() || !EventQueue.isDispatchThread()) {
			try {
				runnable.invoke();
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		else {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						runnable.invoke();
					}
					catch (Exception e) {
						throw RuntimeExceptionUtil.mask(e);
					}
				}
			});
		}
	}
}
