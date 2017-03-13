package com.koch.ambeth.ioc.hierarchy;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public interface IContextHandle
{
	IServiceContext start();

	IServiceContext start(IMap<String, Object> namedBeans);

	IServiceContext start(IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate);

	void stop();
}
