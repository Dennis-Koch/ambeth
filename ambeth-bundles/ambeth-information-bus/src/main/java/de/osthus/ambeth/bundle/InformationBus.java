package de.osthus.ambeth.bundle;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.BytecodeModule;
import de.osthus.ambeth.ioc.CacheBytecodeModule;
import de.osthus.ambeth.ioc.CacheDataChangeModule;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.CacheStreamModule;
import de.osthus.ambeth.ioc.DataChangeModule;
import de.osthus.ambeth.ioc.EventDataChangeModule;
import de.osthus.ambeth.ioc.EventModule;
import de.osthus.ambeth.ioc.ExprModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.MappingModule;
import de.osthus.ambeth.ioc.MergeBytecodeModule;
import de.osthus.ambeth.ioc.MergeModule;
import de.osthus.ambeth.ioc.SecurityBytecodeModule;
import de.osthus.ambeth.ioc.SecurityModule;
import de.osthus.ambeth.ioc.SensorModule;
import de.osthus.ambeth.ioc.ServiceModule;
import de.osthus.ambeth.ioc.StreamModule;

@SuppressWarnings("unchecked")
public class InformationBus implements IBundleModule
{
	private static final Class<?>[] bundleModules = { BytecodeModule.class, CacheModule.class, CacheBytecodeModule.class, CacheDataChangeModule.class,
			CacheStreamModule.class, DataChangeModule.class, EventModule.class, EventDataChangeModule.class, ExprModule.class, MappingModule.class,
			MergeModule.class, MergeBytecodeModule.class, SecurityModule.class, SecurityBytecodeModule.class, SensorModule.class, ServiceModule.class,
			StreamModule.class };

	private static final Class<? extends IBundleModule> parentBundle = Core.class;

	private static final Class<?>[] resultingBundleModules;

	static
	{
		try
		{
			ArrayList<Class<? extends IInitializingModule>> allModules = new ArrayList<Class<? extends IInitializingModule>>();
			allModules.addAll((Class<? extends IInitializingModule>[]) bundleModules);

			Class<? extends IInitializingModule>[] parentBundleModules = parentBundle.newInstance().getBundleModules();
			allModules.addAll(parentBundleModules);

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
