package de.osthus.ambeth.metrics;

import com.yammer.metrics.Metrics;

import de.osthus.ambeth.ioc.IDisposableBean;

/**
 * Register this bean to a context to bind the shutdown procedure of the metrics-core threadPool to the IOC lifecycle. This can save against a memory leak
 */
public class ShutdownMetrics implements IDisposableBean
{
	@Override
	public void destroy() throws Throwable
	{
		Metrics.shutdown();
	}
}
