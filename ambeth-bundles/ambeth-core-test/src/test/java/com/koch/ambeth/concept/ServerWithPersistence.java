package com.koch.ambeth.concept;

import com.koch.ambeth.core.bundle.IBundleModule;
import com.koch.ambeth.ioc.IInitializingModule;

public class ServerWithPersistence implements IBundleModule
{
	@Override
	public Class<? extends IInitializingModule>[] getBundleModules()
	{
		throw new RuntimeException();
	}
}
