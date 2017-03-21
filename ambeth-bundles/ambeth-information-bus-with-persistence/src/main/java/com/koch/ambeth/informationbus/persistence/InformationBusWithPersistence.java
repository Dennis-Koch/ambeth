package com.koch.ambeth.informationbus.persistence;

/*-
 * #%L
 * jambeth-information-bus-with-persistence
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.audit.server.ioc.AuditModule;
import com.koch.ambeth.cache.server.ioc.CacheServerModule;
import com.koch.ambeth.core.bundle.IBundleModule;
import com.koch.ambeth.event.server.ioc.EventServerModule;
import com.koch.ambeth.informationbus.InformationBus;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.job.cron4j.ioc.JobCron4jModule;
import com.koch.ambeth.merge.server.ioc.MergeServerModule;
import com.koch.ambeth.persistence.filter.ioc.FilterPersistenceModule;
import com.koch.ambeth.persistence.ioc.PersistenceModule;
import com.koch.ambeth.persistence.jdbc.connector.DialectSelectorModule;
import com.koch.ambeth.persistence.jdbc.ioc.PersistenceJdbcModule;
import com.koch.ambeth.query.jdbc.ioc.SQLQueryModule;
import com.koch.ambeth.security.persistence.ioc.SecurityQueryModule;
import com.koch.ambeth.security.server.ioc.PrivilegeServerModule;
import com.koch.ambeth.security.server.ioc.SecurityServerModule;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

@SuppressWarnings("unchecked")
public class InformationBusWithPersistence implements IBundleModule
{
	private static final Class<?>[] bundleModules = { AuditModule.class, CacheServerModule.class, DialectSelectorModule.class, EventServerModule.class,
			FilterPersistenceModule.class, JobCron4jModule.class, MergeServerModule.class, PersistenceJdbcModule.class, PersistenceModule.class,
			PrivilegeServerModule.class, SQLQueryModule.class, SecurityQueryModule.class, SecurityServerModule.class };

	private static final Class<?>[] parentBundles = { InformationBus.class };

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
