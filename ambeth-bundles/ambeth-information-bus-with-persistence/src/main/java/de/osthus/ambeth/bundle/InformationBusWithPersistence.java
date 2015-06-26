package de.osthus.ambeth.bundle;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.filter.ioc.FilterPersistenceModule;
import de.osthus.ambeth.ioc.AuditModule;
import de.osthus.ambeth.ioc.CacheServerModule;
import de.osthus.ambeth.ioc.EventServerModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.JobCron4jModule;
import de.osthus.ambeth.ioc.MergeServerModule;
import de.osthus.ambeth.ioc.PersistenceJdbcModule;
import de.osthus.ambeth.ioc.PersistenceModule;
import de.osthus.ambeth.ioc.PrivilegeServerModule;
import de.osthus.ambeth.ioc.SQLQueryModule;
import de.osthus.ambeth.ioc.SecurityQueryModule;
import de.osthus.ambeth.ioc.SecurityServerModule;
import de.osthus.ambeth.persistence.jdbc.connector.DialectSelectorModule;

@SuppressWarnings("unchecked")
public class InformationBusWithPersistence implements IBundleModule
{
	private static final Class<?>[] bundleModules = { AuditModule.class, CacheServerModule.class, DialectSelectorModule.class, EventServerModule.class,
			FilterPersistenceModule.class, JobCron4jModule.class, MergeServerModule.class, PersistenceJdbcModule.class, PersistenceModule.class,
			PrivilegeServerModule.class, SQLQueryModule.class, SecurityQueryModule.class, SecurityServerModule.class };

	private static final Class<?>[] parentBundle = { InformationBus.class };

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
