package de.osthus.ambeth.testutil.datagenerator;


public interface ITestSetterExtendable {

	public void registerTestSetter(ITestSetter testSetter);

	public void unregisterTestSetter(ITestSetter entityFilter);
}
