package com.koch.ambeth.ioc.hierarchy;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

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
