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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.concurrent.locks.Lock;

import com.koch.ambeth.ioc.DefaultExtendableContainer;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.factory.BeanContextInitializer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityWeakHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.ThreadLocalObjectCollector;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public class ThreadLocalCleanupController implements IInitializingBean, IDisposableBean, IThreadLocalCleanupBeanExtendable, IThreadLocalCleanupController
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final DefaultExtendableContainer<IThreadLocalCleanupBean> listeners = new DefaultExtendableContainer<IThreadLocalCleanupBean>(
			IThreadLocalCleanupBean.class, "threadLocalCleanupBean");

	protected ForkStateEntry[] cachedForkStateEntries;

	protected final IdentityHashMap<IThreadLocalCleanupBean, Reference<IServiceContext>> extensionToContextMap = new IdentityHashMap<IThreadLocalCleanupBean, Reference<IServiceContext>>();

	protected final IdentityWeakHashMap<IServiceContext, ParamHolder<Boolean>> alreadyHookedContextSet = new IdentityWeakHashMap<IServiceContext, ParamHolder<Boolean>>();

	protected IServiceContext beanContext;

	protected ThreadLocalObjectCollector objectCollector;

	protected final IBackgroundWorkerParamDelegate<IServiceContext> foreignContextHook = new IBackgroundWorkerParamDelegate<IServiceContext>()
	{
		@Override
		public void invoke(IServiceContext state) throws Throwable
		{
			Lock writeLock = listeners.getWriteLock();
			writeLock.lock();
			try
			{
				cachedForkStateEntries = null;
			}
			finally
			{
				writeLock.unlock();
			}
		}
	};

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public void destroy() throws Throwable
	{
		cleanupThreadLocal();
	}

	public void setObjectCollector(ThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setBeanContext(IServiceContext beanContext)
	{
		this.beanContext = beanContext;
	}

	@Override
	public void cleanupThreadLocal()
	{
		IThreadLocalCleanupBean[] extensions = listeners.getExtensions();
		for (int a = 0, size = extensions.length; a < size; a++)
		{
			extensions[a].cleanupThreadLocal();
		}
		if (objectCollector != null)
		{
			objectCollector.clearThreadLocal();
		}
	}

	@SuppressWarnings("rawtypes")
	protected ForkStateEntry[] getForkStateEntries()
	{
		ForkStateEntry[] cachedForkStateEntries = this.cachedForkStateEntries;
		if (cachedForkStateEntries != null)
		{
			return cachedForkStateEntries;
		}
		Lock writeLock = listeners.getWriteLock();
		writeLock.lock();
		try
		{
			// check again: concurrent thread might have been faster
			cachedForkStateEntries = this.cachedForkStateEntries;
			if (cachedForkStateEntries != null)
			{
				return cachedForkStateEntries;
			}
			IThreadLocalCleanupBean[] extensions = listeners.getExtensions();
			ArrayList<ForkStateEntry> forkStateEntries = new ArrayList<ForkStateEntry>(extensions.length);
			for (int a = 0, size = extensions.length; a < size; a++)
			{
				IThreadLocalCleanupBean extension = extensions[a];
				Field[] fields = ReflectUtil.getDeclaredFieldsInHierarchy(extension.getClass());
				for (Field field : fields)
				{
					Forkable forkable = field.getAnnotation(Forkable.class);
					if (forkable == null)
					{
						continue;
					}
					ThreadLocal<?> valueTL = (ThreadLocal<?>) field.get(extension);
					if (valueTL == null)
					{
						continue;
					}
					Class<? extends IForkProcessor> forkProcessorType = forkable.processor();
					IForkProcessor forkProcessor = null;
					if (forkProcessorType != null && !IForkProcessor.class.equals(forkProcessorType))
					{
						Reference<IServiceContext> beanContextOfExtensionR = extensionToContextMap.get(extension);
						IServiceContext beanContextOfExtension = beanContextOfExtensionR != null ? beanContextOfExtensionR.get() : null;
						if (beanContextOfExtension == null)
						{
							beanContextOfExtension = beanContext;
						}
						forkProcessor = beanContextOfExtension.registerBean(forkProcessorType).finish();
					}
					forkStateEntries.add(new ForkStateEntry(extension, field.getName(), valueTL, forkable.value(), forkProcessor));
				}
			}
			cachedForkStateEntries = forkStateEntries.toArray(ForkStateEntry.class);
			this.cachedForkStateEntries = cachedForkStateEntries;
			return cachedForkStateEntries;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public IForkState createForkState()
	{
		ForkStateEntry[] forkStateEntries = getForkStateEntries();

		IForkedValueResolver[] oldValues = new IForkedValueResolver[forkStateEntries.length];
		for (int a = 0, size = forkStateEntries.length; a < size; a++)
		{
			ForkStateEntry forkStateEntry = forkStateEntries[a];
			IForkProcessor forkProcessor = forkStateEntry.forkProcessor;
			if (forkProcessor != null)
			{
				Object value = forkProcessor.resolveOriginalValue(forkStateEntry.tlBean, forkStateEntry.fieldName, forkStateEntry.valueTL);
				oldValues[a] = new ForkProcessorValueResolver(value, forkProcessor);
				continue;
			}
			Object value = forkStateEntry.valueTL.get();
			if (value != null && ForkableType.SHALLOW_COPY.equals(forkStateEntry.forkableType))
			{
				if (value instanceof Cloneable)
				{
					oldValues[a] = new ShallowCopyValueResolver(value);
				}
				else
				{
					throw new IllegalStateException("Could not clone " + value);
				}
			}
			else
			{
				oldValues[a] = new ReferenceValueResolver(value, value);
			}
		}
		return new ForkState(forkStateEntries, oldValues);
	}

	@Override
	public void registerThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean)
	{
		Lock writeLock = listeners.getWriteLock();
		writeLock.lock();
		try
		{
			listeners.register(threadLocalCleanupBean);
			cachedForkStateEntries = null;
			IServiceContext currentBeanContext = BeanContextInitializer.getCurrentBeanContext();
			if (currentBeanContext != null)
			{
				extensionToContextMap.put(threadLocalCleanupBean, new WeakReference<IServiceContext>(currentBeanContext));
				if (alreadyHookedContextSet.putIfNotExists(currentBeanContext, null))
				{
					final ParamHolder<Boolean> inactive = new ParamHolder<Boolean>();

					currentBeanContext.registerDisposeHook(new IBackgroundWorkerParamDelegate<IServiceContext>()
					{
						@Override
						public void invoke(IServiceContext state) throws Throwable
						{
							if (Boolean.TRUE.equals(inactive.getValue()))
							{
								return;
							}
							foreignContextHook.invoke(state);
						}
					});
					alreadyHookedContextSet.put(currentBeanContext, inactive);
				}
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void unregisterThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean)
	{
		Lock writeLock = listeners.getWriteLock();
		writeLock.lock();
		try
		{
			listeners.unregister(threadLocalCleanupBean);
			cachedForkStateEntries = null;
			extensionToContextMap.remove(threadLocalCleanupBean);
		}
		finally
		{
			writeLock.unlock();
		}
		// clear this threadlocal a last time before letting the bean alone...
		threadLocalCleanupBean.cleanupThreadLocal();
	}
}
