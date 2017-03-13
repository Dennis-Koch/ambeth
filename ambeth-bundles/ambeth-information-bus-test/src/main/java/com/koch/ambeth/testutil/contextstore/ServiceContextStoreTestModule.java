package com.koch.ambeth.testutil.contextstore;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class ServiceContextStoreTestModule implements IInitializingModule
{
	public static final String BLA_DSERVICE = "blaDService";

	public static final String BLA_PROV_EMPTY = "blaDServiceProviderEmpty";

	public static final String BLA_PROV_1 = "blaDServiceProvider1";

	public static final String BLA_PROV_2 = "blaDServiceProvider2";

	public static final String BLA_PROV_3 = "blaDServiceProvider3";

	public static final String BLA_PROV_4 = "blaDServiceProvider4";

	public static final String BLA_PROV_5 = "blaDServiceProvider5";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(BLA_DSERVICE, BlaDServiceImpl.class).autowireable(BlaDService.class);

		beanContextFactory.registerBean(BLA_PROV_EMPTY, BlaDServiceProviderImpl.class);
		beanContextFactory.registerBean(BLA_PROV_1, BlaDServiceProviderImpl.class);
		beanContextFactory.registerBean(BLA_PROV_2, BlaDServiceProviderImpl.class).autowireable(BlaDServiceProvider.class);
		beanContextFactory.registerBean(BLA_PROV_3, BlaDServiceProviderImpl.class);
		beanContextFactory.registerBean(BLA_PROV_4, BlaDServiceProviderImpl.class).autowireable(BlaDServiceProviderImpl.class);
		beanContextFactory.registerBean(BLA_PROV_5, BlaDServiceProviderImpl.class);
	}
}
