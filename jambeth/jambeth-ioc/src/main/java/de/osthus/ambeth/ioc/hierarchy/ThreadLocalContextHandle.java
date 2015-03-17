package de.osthus.ambeth.ioc.hierarchy;

import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBeanExtendable;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class ThreadLocalContextHandle extends AbstractChildContextHandle implements IThreadLocalCleanupBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Forkable
	protected final ThreadLocal<IServiceContext> childContextTL = new SensitiveThreadLocal<IServiceContext>();

	@Autowired
	protected IThreadLocalCleanupBeanExtendable threadLocalCleanupBeanExtendable;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		threadLocalCleanupBeanExtendable.registerThreadLocalCleanupBean(this);
	}

	@Override
	public void destroy() throws Throwable
	{
		threadLocalCleanupBeanExtendable.unregisterThreadLocalCleanupBean(this);
		super.destroy();
	}

	@Override
	protected IServiceContext getChildContext()
	{
		return childContextTL.get();
	}

	@Override
	protected void setChildContext(IServiceContext childContext)
	{
		childContextTL.set(childContext);
	}

	@Override
	public void cleanupThreadLocal()
	{
		IServiceContext context = childContextTL.get();
		if (context == null)
		{
			return;
		}
		childContextTL.remove();
		context.dispose();
	}
}
