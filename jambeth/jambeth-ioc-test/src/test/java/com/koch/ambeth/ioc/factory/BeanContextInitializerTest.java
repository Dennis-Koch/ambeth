package com.koch.ambeth.ioc.factory;

import java.util.Set;

import org.junit.Test;

import com.koch.ambeth.ioc.IBeanPostProcessor;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.exception.BeanContextInitException;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.testutil.AbstractIocTest;

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
