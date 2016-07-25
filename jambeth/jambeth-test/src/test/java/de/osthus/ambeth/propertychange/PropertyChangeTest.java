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
import de.osthus.ambeth.model.INotifyPropertyChangedSource;
import de.osthus.ambeth.propertychange.PropertyChangeTest.PropertyChangeBridgeTestModule;
import de.osthus.ambeth.testutil.AbstractInformationBusTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;

/**
 * Shows the feature how a specific method of a bean can be bound to a PCE (=PropertyChangeEvent) of another (PCE-capable) bean. In this setup
 * <code>Bean1</code> is the "observer" and <code>Bean2</code> is the "observable" providing the PCEs
 * 
 */
@TestModule(PropertyChangeBridgeTestModule.class)
@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.bytecode.core.BytecodeEnhancer", value = "DEBUG")
public class PropertyChangeTest extends AbstractInformationBusTest
{
	public static class PropertyChangeBridgeTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			IBeanConfiguration bean1 = beanContextFactory.registerBean(Bean1.class).autowireable(Bean1.class);
			IBeanConfiguration bean2 = beanContextFactory.registerBean(Bean2.class).autowireable(Bean2.class);
			IBeanConfiguration bean3 = beanContextFactory.registerBean(Bean3.class).autowireable(Bean3.class);
			beanContextFactory.link(bean1, Bean1.MY_PROP_DELEGATE).to(bean2, INotifyPropertyChanged.class);
			beanContextFactory.link(bean1, Bean1.MY_PROP_DELEGATE).to(bean3, INotifyPropertyChanged.class);
		}
	}

	public static class Bean1
	{
		public static final String MY_PROP_DELEGATE = "myProp";

		public int myPropertyCallCount = 0, valueCallCount = 0;

		public void myProp(PropertyChangeEvent evt)
		{
			if (evt.getPropertyName().equals(Bean2.MY_PROPERTY_PROP_NAME))
			{
				myPropertyCallCount++;
			}
			else if (evt.getPropertyName().equals(Bean3.VALUE_PROP_NAME))
			{
				valueCallCount++;
			}
		}
	}

	@PropertyChangeAspect
	public static abstract class Bean2
	{
		public static final String MY_PROPERTY_PROP_NAME = "MyProperty";

		public abstract String getMyProperty();

		public abstract void setMyProperty(String myProperty);
	}

	@PropertyChangeAspect
	public static abstract class Bean3 extends Bean2 implements INotifyPropertyChangedSource
	{
		public static final String VALUE_PROP_NAME = "Value";

		public int fireValue()
		{
			int newValue = 2;
			getPropertyChangeSupport().firePropertyChange(this, VALUE_PROP_NAME, 1, newValue);
			return newValue;
		}
	}

	@Autowired
	protected Bean1 bean1;

	@Autowired
	protected Bean2 bean2;

	@Autowired
	protected Bean3 bean3;

	@Test
	public void test() throws Throwable
	{
		bean2.setMyProperty("value");
		Assert.assertEquals(1, bean1.myPropertyCallCount);
		bean2.setMyProperty("value");
		Assert.assertEquals(1, bean1.myPropertyCallCount);

		bean3.setMyProperty("value");
		Assert.assertEquals(2, bean1.myPropertyCallCount);

		bean3.fireValue();
		Assert.assertEquals(1, bean1.valueCallCount);
	}
}
