package de.osthus.ambeth.training.travelguides.setup;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.util.setup.SetupModule;

@FrameworkModule
public class TestSetupModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory contextFactory) throws Throwable
	{
		SetupModule.registerDataSetBuilder(contextFactory, CoreDatasetBuilder.class);
	}

}
