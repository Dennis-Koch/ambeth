package com.koch.ambeth.ioc.hierarchy;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public interface IContextFactory
{
	IServiceContext createChildContext(IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate);
}
