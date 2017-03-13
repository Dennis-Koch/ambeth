package com.koch.ambeth.core.bundle;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IocModule;

public class Core implements IBundleModule
{
	private static final Class<?>[] bundleModules = { IocModule.class };

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends IInitializingModule>[] getBundleModules()
	{
		return (Class<? extends IInitializingModule>[]) bundleModules;
	}
}
