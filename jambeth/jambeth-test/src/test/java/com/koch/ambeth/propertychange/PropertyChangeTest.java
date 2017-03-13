package com.koch.ambeth.propertychange;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.propertychange.PropertyChangeTest.PropertyChangeBridgeTestModule;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestRebuildContext;
import com.koch.ambeth.util.annotation.FireTargetOnPropertyChange;
import com.koch.ambeth.util.annotation.FireThisOnPropertyChange;
import com.koch.ambeth.util.annotation.PropertyChangeAspect;
import com.koch.ambeth.util.model.INotifyPropertyChanged;
import com.koch.ambeth.util.model.INotifyPropertyChangedSource;

/**
 * Shows the feature how a specific method of a bean can be bound to a PCE (=PropertyChangeEvent) of another (PCE-capable) bean. In this setup
 * <code>Bean1</code> is the "observer" and <code>Bean2</code> is the "observable" providing the PCEs
 * 
 */
@TestModule(PropertyChangeBridgeTestModule.class)
@TestRebuildContext
public class PropertyChangeTest extends AbstractInformationBusTest
{
	public static class PropertyChangeBridgeTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			IBeanConfiguration simplePCEListenerBean = beanContextFactory.registerBean(SimplePCEListenerBean.class).autowireable(SimplePCEListenerBean.class);
			IBeanConfiguration bean1 = beanContextFactory.registerBean(Bean1.class).autowireable(Bean1.class);
			IBeanConfiguration bean2 = beanContextFactory.registerBean(Bean2.class).autowireable(Bean2.class);
			IBeanConfiguration bean3 = beanContextFactory.registerBean(Bean3.class).autowireable(Bean3.class);
			beanContextFactory.link(bean1, Bean1.MY_PROP_DELEGATE).to(bean2, INotifyPropertyChanged.class);
			beanContextFactory.link(bean1, Bean1.MY_PROP_DELEGATE).to(bean3, INotifyPropertyChanged.class);

			beanContextFactory.link(simplePCEListenerBean).to(bean2, INotifyPropertyChanged.class);
			beanContextFactory.link(simplePCEListenerBean).to(bean3, INotifyPropertyChanged.class);

			beanContextFactory.registerBean(Bean4.class).autowireable(Bean4.class);
		}
	}

	public static class SimplePCEListenerBean implements PropertyChangeListener
	{
		public int myPropertyCallCount = 0, valueCallCount = 0;

		@Override
		public void propertyChange(PropertyChangeEvent evt)
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
	public static abstract class Bean1
	{
		public static final String MY_PROP_DELEGATE = "myProp";

		public int myPropertyCallCount = 0, valueCallCount = 0;

		public abstract void setValue(int value);

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

	@PropertyChangeAspect(includeOldValue = false)
	public static abstract class Bean2
	{
		public static final String MY_PROPERTY_PROP_NAME = "MyProperty";

		@SuppressWarnings("unused")
		private String ignorePrivateField;

		protected String ignoreProtectedField;

		public String ignorePublicField;

		String ignorePackageField;

		public abstract String getMyProperty();

		public abstract void setMyProperty(String myProperty);
	}

	@PropertyChangeAspect(includeNewValue = false)
	public static abstract class Bean3 extends Bean2 implements INotifyPropertyChangedSource
	{
		public static final String VALUE_PROP_NAME = "Value";

		public abstract String getValue();

		public int fireValue()
		{
			int newValue = 2;
			onPropertyChanged(VALUE_PROP_NAME, 1, newValue);
			return newValue;
		}
	}

	@PropertyChangeAspect
	public static abstract class Bean4
	{
		@FireThisOnPropertyChange("Value2")
		@FireTargetOnPropertyChange("ValueTarget")
		public String getValue()
		{
			return getValue2() + "X";
		}

		public String getValueTarget()
		{
			return getValue2() + "Y";
		}

		public abstract String getValue2();

		public abstract void setValue2(String value2);
	}

	@Autowired
	protected SimplePCEListenerBean simplePCEListenerBean;

	@Autowired
	protected Bean1 bean1;

	@Autowired
	protected Bean2 bean2;

	@Autowired
	protected Bean3 bean3;

	@Autowired
	protected Bean4 bean4;

	@Test
	public void testIgnoredFields() throws Throwable
	{
		((INotifyPropertyChanged) bean2).addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				Assert.fail("Should never be called");
			}
		});
		bean2.ignorePackageField = "1";
		bean2.ignorePrivateField = "2";
		bean2.ignoreProtectedField = "3";
		bean2.ignorePublicField = "4";
	}

	@Test
	public void testFireAutomatedNoListener() throws Throwable
	{
		bean1.setValue(5);
	}

	@Test
	public void testFireExplicitNoListener() throws Throwable
	{
		((INotifyPropertyChangedSource) bean1).onPropertyChanged("Value", 0, 5);
	}

	@Test
	public void testFireExplicitWithNewValue() throws Throwable
	{
		((INotifyPropertyChanged) bean1).addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				Assert.assertEquals(1, evt.getOldValue());
				Assert.assertEquals(5, evt.getNewValue());
			}
		});
		((INotifyPropertyChangedSource) bean1).onPropertyChanged("Value", 1, 5);
	}

	@Test
	public void testFireExplicitWithoutOldValue() throws Throwable
	{
		((INotifyPropertyChanged) bean2).addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				Assert.assertNull(evt.getOldValue());
				Assert.assertEquals("b", evt.getNewValue());
			}
		});
		((INotifyPropertyChangedSource) bean2).onPropertyChanged("MyProperty", "a", "b");
	}

	@Test
	public void testFireExplicitWithoutNewValue() throws Throwable
	{
		((INotifyPropertyChanged) bean3).addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				Assert.assertEquals("a", evt.getOldValue());
				Assert.assertNull(evt.getNewValue());
			}
		});
		((INotifyPropertyChangedSource) bean3).onPropertyChanged("MyProperty", "a", "b");
	}

	@Test
	public void testFireThisOnPropertyChange() throws Throwable
	{
		final CountDownLatch valueLatch = new CountDownLatch(1);
		final CountDownLatch valueTargetLatch = new CountDownLatch(1);
		((INotifyPropertyChanged) bean4).addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (evt.getPropertyName().equals("Value"))
				{
					valueLatch.countDown();
				}
				if (evt.getPropertyName().equals("ValueTarget"))
				{
					valueTargetLatch.countDown();
				}
			}
		});
		bean4.setValue2("value2");
		Assert.assertEquals(0, valueLatch.getCount());
		Assert.assertEquals(0, valueTargetLatch.getCount());
	}

	@Test
	public void test() throws Throwable
	{
		bean2.setMyProperty("value");
		Assert.assertEquals(1, bean1.myPropertyCallCount);
		Assert.assertEquals(1, simplePCEListenerBean.myPropertyCallCount);
		bean2.setMyProperty("value");
		Assert.assertEquals(1, bean1.myPropertyCallCount);
		Assert.assertEquals(1, simplePCEListenerBean.myPropertyCallCount);

		bean3.setMyProperty("value");
		Assert.assertEquals(2, bean1.myPropertyCallCount);
		Assert.assertEquals(2, simplePCEListenerBean.myPropertyCallCount);

		bean3.fireValue();
		Assert.assertEquals(1, bean1.valueCallCount);
		Assert.assertEquals(1, simplePCEListenerBean.valueCallCount);
	}
}
