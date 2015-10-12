package de.osthus.ambeth.testutil.datagenerator;

public interface ITestSetterExtendable
{

	void registerTestSetter(ITestSetter testSetter);

	void unregisterTestSetter(ITestSetter entityFilter);
}
