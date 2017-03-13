package com.koch.ambeth.ioc.injection;

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
