package de.osthus.ambeth.ioc;

import de.osthus.ambeth.audit.AuditConfigurationProvider;
import de.osthus.ambeth.audit.AuditController;
import de.osthus.ambeth.audit.AuditEntryWriterV1;
import de.osthus.ambeth.audit.AuditMethodCallPostProcessor;
import de.osthus.ambeth.audit.IAuditConfigurationExtendable;
import de.osthus.ambeth.audit.IAuditConfigurationProvider;
import de.osthus.ambeth.audit.IAuditEntryVerifier;
import de.osthus.ambeth.audit.IAuditEntryWriterExtendable;
import de.osthus.ambeth.audit.IAuditInfoController;
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
		beanContextFactory.registerBean(AuditMethodCallPostProcessor.class);

		IBeanConfiguration auditEntryWriterV1 = beanContextFactory.registerBean(AuditEntryWriterV1.class);
		beanContextFactory.link(auditEntryWriterV1).to(IAuditEntryWriterExtendable.class).with(Integer.valueOf(1));

		beanContextFactory.registerBean(AuditConfigurationProvider.class).autowireable(IAuditConfigurationProvider.class, IAuditConfigurationExtendable.class);

		IBeanConfiguration auditEntryController = beanContextFactory.registerBean(AuditController.class).autowireable(IMethodCallLogger.class,
				IAuditEntryVerifier.class, IAuditEntryWriterExtendable.class, IAuditInfoController.class);
		beanContextFactory.link(auditEntryController).to(ITransactionListenerExtendable.class);
		beanContextFactory.link(auditEntryController).to(IMergeListenerExtendable.class);
	}
}
