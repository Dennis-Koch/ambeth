package de.osthus.ambeth.concept;

import de.osthus.ambeth.bundle.IBundleModule;
import de.osthus.ambeth.ioc.IInitializingModule;

public class ServerWithPersistence implements IBundleModule
{
	@Override
	public Class<? extends IInitializingModule>[] getBundleModules()
	{
		throw new RuntimeException();
	}
}
