package de.osthus.ambeth.ioc.hierarchy;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.RegisterPhaseDelegate;

public interface IContextHandle
{
	IServiceContext start();

	IServiceContext start(IMap<String, Object> namedBeans);

	IServiceContext start(RegisterPhaseDelegate registerPhaseDelegate);

	void stop();
}
