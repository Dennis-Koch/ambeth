package de.osthus.ambeth.ioc.extendable;

public interface ITestListenerExtendable
{
	void addTestListener(ITestListener testListener);

	void removeTestListener(ITestListener testListener);
}
