package de.osthus.ambeth.ioc.link;

import java.beans.PropertyChangeEvent;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class TestListener implements ITestListener
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public void handlePropertyChangedTest(PropertyChangeEvent e)
	{
		LinkContainerTest.propertyChangedReceivedCount++;
	}

	@Override
	public void myMethod(PropertyChangeEvent e)
	{
		LinkContainerTest.listenerReceivedCount++;
	}
}
