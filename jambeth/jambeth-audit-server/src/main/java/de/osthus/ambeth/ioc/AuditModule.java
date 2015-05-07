package de.osthus.ambeth.ioc;

import de.osthus.ambeth.IAuditEntryVerifier;
import de.osthus.ambeth.audit.AuditConfigurationProvider;
import de.osthus.ambeth.audit.AuditController;
import de.osthus.ambeth.audit.AuditEntryReader;
import de.osthus.ambeth.audit.AuditEntryToSignature;
import de.osthus.ambeth.audit.AuditEntryVerifier;
import de.osthus.ambeth.audit.AuditEntryWriterV1;
import de.osthus.ambeth.audit.AuditMethodCallPostProcessor;
import de.osthus.ambeth.audit.AuditVerifierJob;
import de.osthus.ambeth.audit.IAuditConfigurationExtendable;
import de.osthus.ambeth.audit.IAuditConfigurationProvider;
import de.osthus.ambeth.audit.IAuditEntryReader;
import de.osthus.ambeth.audit.IAuditEntryToSignature;
import de.osthus.ambeth.audit.IAuditEntryWriterExtendable;
import de.osthus.ambeth.audit.IAuditInfoController;
import de.osthus.ambeth.audit.IMethodCallLogger;
import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.config.AuditConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.ITransactionListenerExtendable;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.job.IJobExtendable;
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

	@Property(name = AuditConfigurationConstants.VerifierCrontab, mandatory = false)
	protected String auditVerifierCrontab;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(AuditConfigurationProvider.class).autowireable(IAuditConfigurationProvider.class, IAuditConfigurationExtendable.class);

		IBeanConfiguration auditEntryController = beanContextFactory.registerBean(AuditController.class).autowireable(IMethodCallLogger.class,
				IAuditInfoController.class);

		if (auditActive)
		{
			beanContextFactory.registerBean(AuditEntryReader.class).autowireable(IAuditEntryReader.class);

			if (auditVerifierCrontab != null)
			{
				IBeanConfiguration auditVerifierJob = beanContextFactory.registerBean(AuditVerifierJob.class);
				beanContextFactory.link(auditVerifierJob).to(IJobExtendable.class).with(AuditVerifierJob.class.getSimpleName(), auditVerifierCrontab)
						.optional();
			}

			IBeanConfiguration auditEntryVerifier = beanContextFactory.registerBean(AuditEntryVerifier.class).autowireable(IAuditEntryVerifier.class);
			beanContextFactory.link(auditEntryVerifier, AuditEntryVerifier.HANDLE_CLEAR_ALL_CACHES_EVENT).to(IEventListenerExtendable.class)
					.with(ClearAllCachesEvent.class);

			beanContextFactory.registerBean(AuditEntryToSignature.class).autowireable(IAuditEntryToSignature.class, IAuditEntryWriterExtendable.class);

			IBeanConfiguration auditEntryWriterV1 = beanContextFactory.registerBean(AuditEntryWriterV1.class);
			beanContextFactory.link(auditEntryWriterV1).to(IAuditEntryWriterExtendable.class).with(Integer.valueOf(1));

			beanContextFactory.registerBean(AuditMethodCallPostProcessor.class);
			beanContextFactory.link(auditEntryController).to(ITransactionListenerExtendable.class);
			beanContextFactory.link(auditEntryController).to(IMergeListenerExtendable.class);
		}
	}
}
