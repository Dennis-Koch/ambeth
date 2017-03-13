package com.koch.ambeth.server.persistence;

import com.koch.ambeth.core.bundle.IBundleModule;
import com.koch.ambeth.informationbus.persistence.InformationBusWithPersistence;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.server.bundle.Server;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

@SuppressWarnings("unchecked")
public class ServerWithPersistence implements IBundleModule {
	private static final Class<?>[] bundleModules = {};

	private static final Class<?>[] parentBundles =
			{InformationBusWithPersistence.class, Server.class};

	private static final Class<?>[] resultingBundleModules;

	static {
		try {
			ArrayList<Class<? extends IInitializingModule>> allModules =
					new ArrayList<Class<? extends IInitializingModule>>();
			allModules.addAll((Class<? extends IInitializingModule>[]) bundleModules);

			for (Class<?> parentBundleClass : parentBundles) {
				IBundleModule parentBundle = (IBundleModule) parentBundleClass.newInstance();
				Class<? extends IInitializingModule>[] parentBundleModules =
						parentBundle.getBundleModules();
				allModules.addAll(parentBundleModules);
			}

			resultingBundleModules = allModules.toArray(Class.class);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Class<? extends IInitializingModule>[] getBundleModules() {
		return (Class<? extends IInitializingModule>[]) resultingBundleModules;
	}
}
