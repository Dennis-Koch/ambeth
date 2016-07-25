package de.osthus.ambeth.propertychange;

import java.beans.PropertyChangeEvent;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.annotation.PropertyChangeAspect;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.model.INotifyPropertyChanged;
import de.osthus.ambeth.propertychange.PropertyChangeTest.PropertyChangeBridgeTestModule;
import de.osthus.ambeth.testutil.AbstractInformationBusTest;
import de.osthus.ambeth.testutil.TestModule;

@TestModule(PropertyChangeBridgeTestModule.class)
public class PropertyChangeTest extends AbstractInformationBusTest
{
	public static class PropertyChangeBridgeTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			IBeanConfiguration bean1 = beanContextFactory.registerBean(Bean1.class).autowireable(Bean1.class);
			IBeanConfiguration bean2 = beanContextFactory.registerBean(Bean2.class).autowireable(Bean2.class);
			beanContextFactory.link(bean1, "myProp").to(bean2, INotifyPropertyChanged.class);
		}
	}

	public static class Bean1
	{
		public int callCount = 0;

		public void myProp(PropertyChangeEvent evt)
		{
			if (evt.getPropertyName().equals("MyProperty"))
			{
				callCount++;
			}
		}
	}

	@PropertyChangeAspect
	public static abstract class Bean2
	{
		public abstract String getMyProperty();

		public abstract void setMyProperty(String myProperty);
	}

	@Autowired
	protected Bean1 bean1;

	@Autowired
	protected Bean2 bean2;

	@Test
	public void test() throws Throwable
	{
		bean2.setMyProperty("value");
		Assert.assertEquals(1, bean1.callCount);
		bean2.setMyProperty("value");
		Assert.assertEquals(1, bean1.callCount);
	}
}
