package de.osthus.ambeth.ioc.extendable;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.extendable.ExtendableBeanTest.ExtendableBeanTestModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;

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

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

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
