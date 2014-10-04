package de.osthus.ambeth.ioc;

import de.osthus.ambeth.audit.AuditController;
import de.osthus.ambeth.audit.AuditMethodCallPostProcessor;
import de.osthus.ambeth.audit.IAuditEntryVerifier;
import de.osthus.ambeth.audit.IMethodCallLogger;
import de.osthus.ambeth.config.AuditConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.ITransactionListenerExtendable;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IMergeListenerExtendable;

@FrameworkModule
public class AuditModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = AuditConfigurationConstants.AuditActive, defaultValue = "false")
	protected boolean auditActive;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (!auditActive)
		{
			return;
		}
		beanContextFactory.registerAnonymousBean(AuditMethodCallPostProcessor.class);

		IBeanConfiguration auditEntryController = beanContextFactory.registerAnonymousBean(AuditController.class).autowireable(IMethodCallLogger.class,
				IAuditEntryVerifier.class);
		beanContextFactory.link(auditEntryController).to(ITransactionListenerExtendable.class);
		beanContextFactory.link(auditEntryController).to(IMergeListenerExtendable.class);
	}
}
