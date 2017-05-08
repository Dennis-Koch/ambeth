package com.koch.ambeth.ioc.performance;

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
