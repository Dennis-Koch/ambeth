package de.osthus.ambeth.platform;

import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.util.IDisposable;

public interface IAmbethPlatformContext extends IDisposable
{
	IServiceContext getBeanContext();

	void clearThreadLocal();

	void afterBegin();

	void afterCommit();

	void afterRollback();
}
