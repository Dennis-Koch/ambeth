package com.koch.ambeth.audit.server.ioc;

import com.koch.ambeth.audit.IAuditEntryVerifier;
import com.koch.ambeth.audit.server.AuditConfigurationProvider;
import com.koch.ambeth.audit.server.AuditController;
import com.koch.ambeth.audit.server.AuditEntryReader;
import com.koch.ambeth.audit.server.AuditEntryToSignature;
import com.koch.ambeth.audit.server.AuditEntryVerifier;
import com.koch.ambeth.audit.server.AuditEntryWriterV1;
import com.koch.ambeth.audit.server.AuditMethodCallPostProcessor;
import com.koch.ambeth.audit.server.AuditVerifierJob;
import com.koch.ambeth.audit.server.AuditVerifyOnLoadTask;
import com.koch.ambeth.audit.server.IAuditConfigurationExtendable;
import com.koch.ambeth.audit.server.IAuditConfigurationProvider;
import com.koch.ambeth.audit.server.IAuditEntryReader;
import com.koch.ambeth.audit.server.IAuditEntryToSignature;
import com.koch.ambeth.audit.server.IAuditEntryWriterExtendable;
import com.koch.ambeth.audit.server.IAuditInfoController;
import com.koch.ambeth.audit.server.IAuditVerifyOnLoadTask;
import com.koch.ambeth.audit.server.IMethodCallLogger;
import com.koch.ambeth.audit.server.config.AuditConfigurationConstants;
import com.koch.ambeth.cache.audit.IVerifyOnLoad;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.job.IJobExtendable;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IMergeListenerExtendable;
import com.koch.ambeth.persistence.api.database.ITransactionListenerExtendable;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;

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

			IBeanConfiguration auditEntryVerifier = beanContextFactory.registerBean(AuditEntryVerifier.class).autowireable(IAuditEntryVerifier.class,
					IVerifyOnLoad.class);
			beanContextFactory.link(auditEntryVerifier, AuditEntryVerifier.HANDLE_CLEAR_ALL_CACHES_EVENT).to(IEventListenerExtendable.class)
					.with(ClearAllCachesEvent.class);

			beanContextFactory.registerBean(AuditVerifyOnLoadTask.class).autowireable(IAuditVerifyOnLoadTask.class);

			beanContextFactory.registerBean(AuditEntryToSignature.class).autowireable(IAuditEntryToSignature.class, IAuditEntryWriterExtendable.class);

			IBeanConfiguration auditEntryWriterV1 = beanContextFactory.registerBean(AuditEntryWriterV1.class);
			beanContextFactory.link(auditEntryWriterV1).to(IAuditEntryWriterExtendable.class).with(Integer.valueOf(1));

			beanContextFactory.registerBean(AuditMethodCallPostProcessor.class);
			beanContextFactory.link(auditEntryController).to(ITransactionListenerExtendable.class);
			beanContextFactory.link(auditEntryController).to(IMergeListenerExtendable.class);
		}
	}
}
