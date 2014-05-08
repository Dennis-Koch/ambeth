package de.osthus.ambeth.ioc.beanruntime;

import org.junit.Test;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.AbstractIocTest;

public class BeanRuntimeTest extends AbstractIocTest
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
	}

	@Test
	public void testBeanRuntimeInjection()
	{
		// beanContext.
	}
}
