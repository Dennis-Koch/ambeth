package de.osthus.ambeth.util;

import de.osthus.ambeth.ioc.IServiceContext;

public interface IMultithreadingHelper
{
	void invokeInParallel(IServiceContext serviceContext, Runnable runnable, int workerCount);

	void invokeInParallel(IServiceContext serviceContext, Runnable... runnables);
}