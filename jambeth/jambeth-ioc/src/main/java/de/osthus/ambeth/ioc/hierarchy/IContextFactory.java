package de.osthus.ambeth.ioc.hierarchy;

import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;

public interface IContextFactory
{
	IServiceContext createChildContext(IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate);
}
