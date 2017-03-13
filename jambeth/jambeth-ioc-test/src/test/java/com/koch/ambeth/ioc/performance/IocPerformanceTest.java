package com.koch.ambeth.ioc.performance;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.category.PerformanceTests;

@Category(PerformanceTests.class)
@TestProperties(name = IocConfigurationConstants.TrackDeclarationTrace, value = "false")
public class IocPerformanceTest extends AbstractIocTest {
	public static final String count_prop = "count_prop";

	private static final int count = 100000;

	@Test
	@TestProperties(name = IocPerformanceTest.count_prop, value = "" + count)
	public void performance() {
		IServiceContext childContext = beanContext.createService(IocPerformanceTestModule.class);
		Assert.assertEquals(count, childContext.getObjects(TestBean.class).size());
		childContext.dispose();
	}
}
