package de.osthus.ambeth.bundle;

import de.osthus.ambeth.ioc.IInitializingModule;

/**
 * Interface for bundle modules that defines the module list for a specific bundle.
 */
public interface IBundleModule
{
	Class<? extends IInitializingModule>[] getBundleModules();
}
