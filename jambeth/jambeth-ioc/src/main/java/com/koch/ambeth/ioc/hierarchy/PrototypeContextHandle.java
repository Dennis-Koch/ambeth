package com.koch.ambeth.ioc.hierarchy;

import java.util.Map.Entry;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public class PrototypeContextHandle implements IContextHandle
{
	@LogInstance
	private ILogger log;

	protected final ISet<IServiceContext> childContexts = new HashSet<IServiceContext>();

	@Autowired
	protected IContextFactory childContextFactory;

	@Override
	public IServiceContext start()
	{
		IServiceContext childContext = childContextFactory.createChildContext(null);
		childContexts.add(childContext);
		return childContext;
	}

	@Override
	public IServiceContext start(final IMap<String, Object> namedBeans)
	{
		IServiceContext childContext = childContextFactory.createChildContext(new IBackgroundWorkerParamDelegate<IBeanContextFactory>()
		{

			@Override
			public void invoke(IBeanContextFactory childContextFactory)
			{
				for (Entry<String, Object> entry : namedBeans)
				{
					String beanName = entry.getKey();
					Object bean = entry.getValue();
					childContextFactory.registerExternalBean(beanName, bean);
				}
			}
		});
		childContexts.add(childContext);
		return childContext;
	}

	@Override
	public IServiceContext start(IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate)
	{
		IServiceContext childContext = childContextFactory.createChildContext(registerPhaseDelegate);
		childContexts.add(childContext);
		return childContext;
	}

	@Override
	public void stop()
	{
		for (IServiceContext childContext : childContexts)
		{
			try
			{
				childContext.dispose();
			}
			catch (Throwable e)
			{
				log.error(e);
			}
		}
		childContexts.clear();
	}
}
