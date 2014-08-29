package de.osthus.ambeth.ioc;

import de.osthus.ambeth.audit.AuditMethodCallPostProcessor;
import de.osthus.ambeth.audit.IMethodCallLogger;
import de.osthus.ambeth.audit.MethodCallLogger;
import de.osthus.ambeth.config.AuditConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.event.DatabasePreCommitEvent;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

@FrameworkModule
public class AuditModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = AuditConfigurationConstants.AuditMethodActive, defaultValue = "false")
	protected boolean auditMethodActive;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (auditMethodActive)
		{
			beanContextFactory.registerAnonymousBean(AuditMethodCallPostProcessor.class);

			IBeanConfiguration methodCallLogger = beanContextFactory.registerAnonymousBean(MethodCallLogger.class).autowireable(IMethodCallLogger.class);
			beanContextFactory.link(methodCallLogger, "handlePreCommit").to(IEventListenerExtendable.class).with(DatabasePreCommitEvent.class);
		}
	}
}
