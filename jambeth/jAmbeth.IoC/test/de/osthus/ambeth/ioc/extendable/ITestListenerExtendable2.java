package de.osthus.ambeth.ioc.extendable;

public interface ITestListenerExtendable2
{
	void registerTestListener(ITestListener testListener, Class<?> type);

	void unregisterTestListener(ITestListener testListener, Class<?> type);
}
