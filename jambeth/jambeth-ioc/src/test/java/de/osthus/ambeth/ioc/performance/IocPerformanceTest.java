package de.osthus.ambeth.ioc.performance;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.category.PerformanceTests;

@Category(PerformanceTests.class)
@TestProperties(name = IocConfigurationConstants.TrackDeclarationTrace, value = "false")
public class IocPerformanceTest extends AbstractIocTest
{
	public static final String count_prop = "count_prop";

	private static final int count = 100000;

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Test
	@TestProperties(name = IocPerformanceTest.count_prop, value = "" + count)
	public void performance()
	{
		IServiceContext childContext = beanContext.createService(IocPerformanceTestModule.class);
		Assert.assertEquals(count, childContext.getObjects(TestBean.class).size());
		childContext.dispose();
	}
}
