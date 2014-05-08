package de.osthus.ambeth.ioc.extendable;

public interface ITestListenerExtendableType
{
	void addTestListener(ITestListener testListener, Class<?> type);

	void removeTestListener(ITestListener testListener, Class<?> type);

}
