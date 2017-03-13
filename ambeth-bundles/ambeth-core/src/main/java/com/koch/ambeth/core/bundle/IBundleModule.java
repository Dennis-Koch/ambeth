package com.koch.ambeth.core.bundle;

import com.koch.ambeth.ioc.IInitializingModule;

/**
 * Interface for bundle modules that defines the module list for a specific bundle.
 */
public interface IBundleModule
{
	Class<? extends IInitializingModule>[] getBundleModules();
}
