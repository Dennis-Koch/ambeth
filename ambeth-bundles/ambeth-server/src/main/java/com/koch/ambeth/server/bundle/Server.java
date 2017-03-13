package com.koch.ambeth.server.bundle;

import com.koch.ambeth.core.bundle.IBundleModule;
import com.koch.ambeth.informationbus.InformationBus;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.security.server.ioc.PrivilegeServerModule;
import com.koch.ambeth.security.server.ioc.SecurityServerModule;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.xml.ioc.XmlModule;

@SuppressWarnings("unchecked")
public class Server implements IBundleModule
{
	private static final Class<?>[] bundleModules = { PrivilegeServerModule.class, SecurityServerModule.class, XmlModule.class };

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
