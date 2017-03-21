package com.koch.ambeth.ioc.factory;

/*-
 * #%L
 * jambeth-ioc-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.Set;

import org.junit.Test;

import com.koch.ambeth.ioc.IBeanPostProcessor;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.exception.BeanContextInitException;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.testutil.AbstractIocTest;

public class BeanContextInitializerTest extends AbstractIocTest {
	public static class FaultyBeanPostProcessor implements IBeanPostProcessor {
		@Override
		public Object postProcessBean(IBeanContextFactory beanContextFactory,
				IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> beanType,
				Object targetBean, Set<Class<?>> requestedTypes) {
			return null; // This should return the targetBean, but this is a faulty implementation.
		}
	}

	public static class FaultyBeanPostProcessorModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean(FaultyBeanPostProcessor.class);
		}
	}

	@LogInstance
	private ILogger log;

	@Test(expected = BeanContextInitException.class)
	public void testFaultyBeanPostProcessor() {
		beanContext.createService(FaultyBeanPostProcessorModule.class);
	}
}
