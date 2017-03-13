package com.koch.ambeth.testutil;

import org.junit.runners.model.InitializationError;

import com.koch.ambeth.testutil.AmbethIocRunner;
import com.koch.ambeth.testutil.ICleanupAfter;

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
