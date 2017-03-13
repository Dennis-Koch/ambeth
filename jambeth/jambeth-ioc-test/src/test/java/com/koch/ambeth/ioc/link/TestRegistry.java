package com.koch.ambeth.ioc.link;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.koch.ambeth.ioc.DefaultExtendableContainer;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.extendable.IExtendableContainer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class TestRegistry implements ITestListenerExtendable, IInitializingBean, ITestRegistry, IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final IExtendableContainer<PropertyChangeListener> propertyChangedList = new DefaultExtendableContainer<PropertyChangeListener>(
			PropertyChangeListener.class, "pceListener");

	protected final IExtendableContainer<ITestListener> testListeners = new DefaultExtendableContainer<ITestListener>(ITestListener.class, "testListener");

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public void afterStarted()
	{
		PropertyChangeEvent pceArgs = new PropertyChangeEvent(this, "Test", 1, 2);
		PropertyChangeListener[] pceListeners = propertyChangedList.getExtensions();
		for (PropertyChangeListener pceListener : pceListeners)
		{
			pceListener.propertyChange(pceArgs);
		}
		ITestListener[] testListeners = this.testListeners.getExtensions();
		for (ITestListener testListener : testListeners)
		{
			testListener.myMethod(pceArgs);
		}
	}

	@Override
	public PropertyChangeListener[] getPceListeners()
	{
		return propertyChangedList.getExtensions();
	}

	@Override
	public ITestListener[] getTestListeners()
	{
		return testListeners.getExtensions();
	}

	@Override
	public void registerTestListener(ITestListener testListener)
	{
		testListeners.register(testListener);
	}

	@Override
	public void unregisterTestListener(ITestListener testListener)
	{
		testListeners.unregister(testListener);
	}
}
