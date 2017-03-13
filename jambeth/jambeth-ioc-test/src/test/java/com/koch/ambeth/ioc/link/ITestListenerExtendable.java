package com.koch.ambeth.ioc.link;

public interface ITestListenerExtendable
{
	void registerTestListener(ITestListener testListener);

	void unregisterTestListener(ITestListener testListener);
}
