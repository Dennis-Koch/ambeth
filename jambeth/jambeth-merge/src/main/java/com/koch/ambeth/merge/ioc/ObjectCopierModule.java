package com.koch.ambeth.merge.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.copy.IObjectCopier;
import com.koch.ambeth.merge.copy.IObjectCopierExtendable;
import com.koch.ambeth.merge.copy.ObjectCopier;
import com.koch.ambeth.merge.copy.StringBuilderOCE;

/**
 * Registers an ObjectCopier as well as default extensions to copy objects Include this module in an IOC container to gain access to <code>IObjectCopier</code>
 * & <code>IObjectCopierExtendable</code> functionality
 */
@FrameworkModule
public class ObjectCopierModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		// Default ObjectCopier implementation
		beanContextFactory.registerBean(ObjectCopier.class).autowireable(IObjectCopier.class, IObjectCopierExtendable.class);

		// Default ObjectCopier extensions
		IBeanConfiguration stringBuilderOCE = beanContextFactory.registerBean(StringBuilderOCE.class);
		beanContextFactory.link(stringBuilderOCE).to(IObjectCopierExtendable.class).with(StringBuilder.class);
	}
}
