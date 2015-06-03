package de.osthus.ambeth.bundle;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IocModule;

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
