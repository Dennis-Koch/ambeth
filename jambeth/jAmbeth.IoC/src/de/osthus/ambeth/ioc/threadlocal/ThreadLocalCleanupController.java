package de.osthus.ambeth.ioc.threadlocal;

import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.extendable.IExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.ThreadLocalObjectCollector;

public class ThreadLocalCleanupController implements IInitializingBean, IDisposableBean, IThreadLocalCleanupBeanExtendable, IThreadLocalCleanupController
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final IExtendableContainer<IThreadLocalCleanupBean> listeners = new DefaultExtendableContainer<IThreadLocalCleanupBean>(
			IThreadLocalCleanupBean.class, "threadLocalCleanupBean");

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

	@Override
	public void registerThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean)
	{
		listeners.register(threadLocalCleanupBean);
	}

	@Override
	public void unregisterThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean)
	{
		listeners.unregister(threadLocalCleanupBean);
		// clear this threadlocal a last time before letting the bean alone...
		threadLocalCleanupBean.cleanupThreadLocal();
	}
}
