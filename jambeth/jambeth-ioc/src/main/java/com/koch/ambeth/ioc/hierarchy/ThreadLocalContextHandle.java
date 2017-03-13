package com.koch.ambeth.ioc.hierarchy;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBeanExtendable;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

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
