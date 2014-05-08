package de.osthus.ambeth.ioc.hierarchy;

import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.RegisterPhaseDelegate;

public interface IContextFactory
{
	IServiceContext createChildContext(RegisterPhaseDelegate registerPhaseDelegate);
}
