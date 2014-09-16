package de.osthus.ambeth.testutil;

import org.junit.runner.RunWith;

import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocBootstrapModule;
import de.osthus.ambeth.ioc.annotation.Autowired;

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
@TestFrameworkModule({ IocBootstrapModule.class })
@TestPropertiesList({ @TestProperties(name = IocConfigurationConstants.TrackDeclarationTrace, value = "true"),
		@TestProperties(name = IocConfigurationConstants.DebugModeActive, value = "true"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.accessor.AccessorTypeProvider", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.bytecode.core.BytecodeEnhancer", value = "WARN"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.bytecode.visitor.ClassWriter", value = "INFO"),
		// @TestProperties(name = "ambeth.log.level.de.osthus.ambeth.bytecode.visitor.LogImplementationsClassVisitor", value = "INFO"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.template.PropertyChangeTemplate", value = "INFO") })
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
