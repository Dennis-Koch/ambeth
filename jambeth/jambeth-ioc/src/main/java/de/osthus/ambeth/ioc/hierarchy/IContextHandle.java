package de.osthus.ambeth.ioc.hierarchy;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;

public interface IContextHandle
{
	IServiceContext start();

	IServiceContext start(IMap<String, Object> namedBeans);

	IServiceContext start(IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate);

	void stop();
}
