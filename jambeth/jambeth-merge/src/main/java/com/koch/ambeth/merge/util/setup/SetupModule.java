package com.koch.ambeth.merge.util.setup;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

@FrameworkModule
public class SetupModule implements IInitializingModule
{
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(DataSetup.class).autowireable(IDataSetup.class, IDatasetBuilderExtendable.class);
	}

	public static void registerDataSetBuilder(IBeanContextFactory beanContextFactory, Class<? extends IDatasetBuilder> type)
	{
		IBeanConfiguration builder = beanContextFactory.registerBean(type).autowireable(type);
		beanContextFactory.link(builder).to(IDatasetBuilderExtendable.class);
	}
}
