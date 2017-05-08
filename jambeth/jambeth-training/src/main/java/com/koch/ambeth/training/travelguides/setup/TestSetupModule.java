package com.koch.ambeth.training.travelguides.setup;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.util.setup.SetupModule;

@FrameworkModule
public class TestSetupModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory contextFactory) throws Throwable
	{
		SetupModule.registerDataSetBuilder(contextFactory, CoreDatasetBuilder.class);
	}

}
