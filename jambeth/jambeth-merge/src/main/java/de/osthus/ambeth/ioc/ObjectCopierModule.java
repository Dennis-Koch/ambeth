package de.osthus.ambeth.ioc;

import de.osthus.ambeth.copy.IObjectCopier;
import de.osthus.ambeth.copy.IObjectCopierExtendable;
import de.osthus.ambeth.copy.ObjectCopier;
import de.osthus.ambeth.copy.StringBuilderOCE;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

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
		IBeanConfiguration objectCopier = beanContextFactory.registerAnonymousBean(ObjectCopier.class).autowireable(IObjectCopier.class,
				IObjectCopierExtendable.class);

		// Default ObjectCopier extensions
		IBeanConfiguration stringBuilderOCE = beanContextFactory.registerAnonymousBean(StringBuilderOCE.class);
		beanContextFactory.link(stringBuilderOCE).to(IObjectCopierExtendable.class).with(StringBuilder.class);
	}
}
