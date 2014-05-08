package de.osthus.ambeth.ioc.performance;

import junit.framework.Assert;

import org.junit.Test;

import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestProperties;

@TestProperties(name = IocConfigurationConstants.TrackDeclarationTrace, value = "false")
public class IocPerformanceTest extends AbstractIocTest
{
	public static final String count_prop = "count_prop";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Test
	@TestProperties(name = IocPerformanceTest.count_prop, value = "50000")
	public void performance()
	{
		IServiceContext childContext = beanContext.createService(IocPerformanceTestModule.class);
		Assert.assertEquals(50000, childContext.getObjects(TestBean.class).size());
		childContext.dispose();
	}
}
