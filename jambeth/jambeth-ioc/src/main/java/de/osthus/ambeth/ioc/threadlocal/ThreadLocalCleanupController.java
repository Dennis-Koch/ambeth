package de.osthus.ambeth.ioc.threadlocal;

import java.lang.reflect.Field;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.ThreadLocalObjectCollector;
import de.osthus.ambeth.util.ReflectUtil;

public class ThreadLocalCleanupController implements IInitializingBean, IDisposableBean, IThreadLocalCleanupBeanExtendable, IThreadLocalCleanupController
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final DefaultExtendableContainer<IThreadLocalCleanupBean> listeners = new DefaultExtendableContainer<IThreadLocalCleanupBean>(
			IThreadLocalCleanupBean.class, "threadLocalCleanupBean");

	protected ForkStateEntry[] cachedForkStateEntries;

	protected IServiceContext beanContext;

	protected ThreadLocalObjectCollector objectCollector;

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
						forkProcessor = beanContext.registerBean(forkProcessorType).finish();
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
		}
		finally
		{
			writeLock.unlock();
		}
		// clear this threadlocal a last time before letting the bean alone...
		threadLocalCleanupBean.cleanupThreadLocal();
	}
}
