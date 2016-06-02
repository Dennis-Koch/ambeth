package de.osthus.ambeth.ioc.factory;

import java.util.Set;

import org.junit.Test;

import de.osthus.ambeth.ioc.IBeanPostProcessor;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.exception.BeanContextInitException;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.AbstractIocTest;

public class BeanContextInitializerTest extends AbstractIocTest
{
	public static class FaultyBeanPostProcessor implements IBeanPostProcessor
	{
		@Override
		public Object postProcessBean(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration,
				Class<?> beanType, Object targetBean, Set<Class<?>> requestedTypes)
		{
			return null; // This should return the targetBean, but this is a faulty implementation.
		}
	}

	public static class FaultyBeanPostProcessorModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean(FaultyBeanPostProcessor.class);
		}
	}

	@LogInstance
	private ILogger log;

	@Test(expected = BeanContextInitException.class)
	public void testFaultyBeanPostProcessor()
	{
		beanContext.createService(FaultyBeanPostProcessorModule.class);
	}
}
