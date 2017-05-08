package com.koch.ambeth.ioc.injection;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.category.PerformanceTests;

@Category(PerformanceTests.class)
@TestModule(InjectionTestModule.class)
public class InjectionTest extends AbstractIocTest {
	@Test
	public void test() {
		int beanCount = InjectionTestModule.BEAN_COUNT;
		String name = InjectionTestModule.NAME;

		for (int i = 0; i < beanCount; i++) {
			int previousNumber = (beanCount + i) % beanCount;
			int counterpartNumber = ((int) (beanCount * 1.5) + i) % beanCount;
			String serviceName = name + i;
			String previousName = name + previousNumber;
			String counterpartName = name + counterpartNumber;

			InjectionTestBean testBean = beanContext.getService(serviceName, InjectionTestBean.class);
			assertNotNull(testBean);
			assertEquals(serviceName, testBean.getName());

			InjectionTestBean previousTestBean = testBean.getPrevious();
			assertNotNull(previousTestBean);
			assertEquals(previousName, testBean.getName());

			InjectionTestBean counterpartTestBean = testBean.getCounterpart();
			assertNotNull(counterpartTestBean);
			assertEquals(counterpartName, counterpartTestBean.getName());
		}
	}
}
