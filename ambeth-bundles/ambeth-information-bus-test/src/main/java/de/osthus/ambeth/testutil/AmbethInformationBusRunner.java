package de.osthus.ambeth.testutil;

import org.junit.runners.model.InitializationError;

public class AmbethInformationBusRunner extends AmbethIocRunner
{
	public AmbethInformationBusRunner(Class<?> testClass) throws InitializationError
	{
		super(testClass);
	}

	@Override
	protected Class<? extends ICleanupAfter> getCleanupAfterType()
	{
		return CleanupAfterInformationBus.class;
	}
}
