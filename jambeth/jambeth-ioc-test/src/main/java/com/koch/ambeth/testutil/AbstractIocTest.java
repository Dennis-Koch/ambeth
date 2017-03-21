package com.koch.ambeth.testutil;

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

import org.junit.runner.RunWith;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;

/**
 * Abstract test class easing usage of IoC containers in test scenarios. Isolated modules can be registered with the <code>TestModule</code> annotation. The
 * test itself will be registered as a bean within the IoC container. Therefore it can consume any components for testing purpose and behave like a productively
 * bean.
 * 
 * In addition to registering custom modules the environment can be constructed for specific testing purpose with the <code>TestProperties</code> annotation.
 * Multiple properties can be wrapped using the <code>TestPropertiesList</code> annotation.
 * 
 * All annotations can be used on test class level as well as on test method level. In ambiguous scenarios the method annotations will gain precedence.
 */
@RunWith(AmbethIocRunner.class)
@TestFrameworkModule({ IocModule.class })
@TestPropertiesList({ @TestProperties(name = IocConfigurationConstants.TrackDeclarationTrace, value = "true"),
		@TestProperties(name = IocConfigurationConstants.DebugModeActive, value = "true"),
		@TestProperties(name = "ambeth.log.level.com.koch.ambeth.accessor.AccessorTypeProvider", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.com.koch.ambeth.bytecode.core.BytecodeEnhancer", value = "WARN"),
		@TestProperties(name = "ambeth.log.level.com.koch.ambeth.bytecode.visitor.ClassWriter", value = "DEBUG"),
		@TestProperties(name = "ambeth.log.level.com.koch.ambeth.bytecode.visitor.LogImplementationsClassVisitor", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.com.koch.ambeth.mixin.PropertyChangeMixin", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.com.koch.ambeth.template.PropertyChangeTemplate", value = "INFO") })
public abstract class AbstractIocTest implements IInitializingBean, IDisposableBean
{
	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ITestContext testContext;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public void destroy() throws Throwable
	{
		// Intended blank
	}
}
