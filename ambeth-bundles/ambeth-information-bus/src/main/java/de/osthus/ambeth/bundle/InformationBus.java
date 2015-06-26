package de.osthus.ambeth.bundle;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.BytecodeModule;
import de.osthus.ambeth.ioc.CacheBytecodeModule;
import de.osthus.ambeth.ioc.CacheDataChangeModule;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.CacheStreamModule;
import de.osthus.ambeth.ioc.DotModule;
import de.osthus.ambeth.ioc.EventDataChangeModule;
import de.osthus.ambeth.ioc.EventModule;
import de.osthus.ambeth.ioc.ExprModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.MappingModule;
import de.osthus.ambeth.ioc.MergeBytecodeModule;
import de.osthus.ambeth.ioc.MergeModule;
import de.osthus.ambeth.ioc.ObjectCopierModule;
import de.osthus.ambeth.ioc.PrivilegeModule;
import de.osthus.ambeth.ioc.SecurityBytecodeModule;
import de.osthus.ambeth.ioc.SecurityModule;
import de.osthus.ambeth.ioc.SensorModule;
import de.osthus.ambeth.ioc.ServiceModule;
import de.osthus.ambeth.ioc.StreamModule;
import de.osthus.ambeth.util.setup.SetupModule;

@SuppressWarnings("unchecked")
public class InformationBus implements IBundleModule
{
	private static final Class<?>[] bundleModules = { BytecodeModule.class, CacheBytecodeModule.class, CacheDataChangeModule.class, CacheModule.class,
			CacheStreamModule.class, DotModule.class, EventDataChangeModule.class, EventModule.class, ExprModule.class, MappingModule.class,
			MergeBytecodeModule.class, MergeModule.class, ObjectCopierModule.class, PrivilegeModule.class, SecurityBytecodeModule.class, SecurityModule.class,
			SensorModule.class, ServiceModule.class, SetupModule.class, StreamModule.class };

	private static final Class<?>[] parentBundles = { Core.class };

	private static final Class<?>[] resultingBundleModules;

	static
	{
		try
		{
			ArrayList<Class<? extends IInitializingModule>> allModules = new ArrayList<Class<? extends IInitializingModule>>();
			allModules.addAll((Class<? extends IInitializingModule>[]) bundleModules);

			for (Class<?> parentBundleClass : parentBundles)
			{
				IBundleModule parentBundle = (IBundleModule) parentBundleClass.newInstance();
				Class<? extends IInitializingModule>[] parentBundleModules = parentBundle.getBundleModules();
				allModules.addAll(parentBundleModules);
			}

			resultingBundleModules = allModules.toArray(Class.class);
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Class<? extends IInitializingModule>[] getBundleModules()
	{
		return (Class<? extends IInitializingModule>[]) resultingBundleModules;
	}
}
