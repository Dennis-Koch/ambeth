package de.osthus.ambeth.util.setup;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

@FrameworkModule
public class SetupModule implements IInitializingModule
{
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAnonymousBean(DataSetup.class).autowireable(IDataSetup.class, IDatasetBuilderExtensionExtendable.class);
	}

	public static void registerDataSetBuilder(IBeanContextFactory beanContextFactory, Class<? extends IDatasetBuilder> type)
	{
		IBeanConfiguration builder = beanContextFactory.registerAnonymousBean(type).autowireable(type);
		beanContextFactory.link(builder).to(IDatasetBuilderExtensionExtendable.class);
	}
}
