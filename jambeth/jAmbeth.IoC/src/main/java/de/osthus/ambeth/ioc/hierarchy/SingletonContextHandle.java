package de.osthus.ambeth.ioc.hierarchy;

import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class SingletonContextHandle extends AbstractChildContextHandle
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IServiceContext childContext;

	@Override
	protected IServiceContext getChildContext()
	{
		return childContext;
	}

	@Override
	protected void setChildContext(IServiceContext childContext)
	{
		this.childContext = childContext;
	}
}
