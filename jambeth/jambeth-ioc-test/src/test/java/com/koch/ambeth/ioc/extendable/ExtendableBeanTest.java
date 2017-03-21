package com.koch.ambeth.ioc.extendable;

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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.extendable.ExtendableBean;
import com.koch.ambeth.ioc.extendable.ExtendableBeanTest.ExtendableBeanTestModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.util.collections.ArrayList;

@TestModule(ExtendableBeanTestModule.class)
public class ExtendableBeanTest extends AbstractIocTest
{
	public static class ExtendableBeanTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean("testExtensionPoint", ExtendableBean.class)
					.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, ITestListenerExtendable.class)
					.propertyValue(ExtendableBean.P_PROVIDER_TYPE, ITestListenerRegistry.class);

			beanContextFactory.registerBean("testExtensionPoint2", ExtendableBean.class)
					.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, ITestListenerExtendable2.class)
					.propertyValue(ExtendableBean.P_PROVIDER_TYPE, ITestListenerRegistry2.class);
		}
	}

	@Test
	public void testToString()
	{
		ITestListenerExtendable testListenerExtendable = beanContext.getService("testExtensionPoint", ITestListenerExtendable.class);
		ITestListenerRegistry testListenerRegistry = beanContext.getService("testExtensionPoint", ITestListenerRegistry.class);

		Assert.assertNotNull(testListenerExtendable.toString());
		Assert.assertNotNull(testListenerRegistry.toString());
	}

	@Test
	public void testExtensionPoint()
	{
		ITestListenerExtendable testListenerExtendable = beanContext.getService("testExtensionPoint", ITestListenerExtendable.class);
		ITestListenerRegistry testListenerRegistry = beanContext.getService("testExtensionPoint", ITestListenerRegistry.class);

		testListenerExtendable.addTestListener(new ITestListener()
		{
		});

		ITestListener[] testListeners = testListenerRegistry.getTestListeners();
		Assert.assertNotNull(testListeners);
		Assert.assertEquals(1, testListeners.length);
	}

	@Test
	public void testExtensionPointByType()
	{
		ITestListenerExtendable2 testListenerExtendable = beanContext.getService("testExtensionPoint2", ITestListenerExtendable2.class);
		ITestListenerRegistry2 testListenerRegistry = beanContext.getService("testExtensionPoint2", ITestListenerRegistry2.class);

		testListenerExtendable.registerTestListener(new ITestListener()
		{
		}, List.class);

		ITestListener testListener = testListenerRegistry.getTestListener(ArrayList.class);
		Assert.assertNotNull(testListener);
	}
}
