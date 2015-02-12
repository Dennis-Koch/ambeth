package de.osthus.ambeth.platform;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.database.IDatabaseProvider;
import de.osthus.ambeth.event.IEventQueue;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IModuleProvider;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LoggerFactory;
import de.osthus.ambeth.merge.ILightweightTransaction;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.objectcollector.ThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.ModuleUtil;
import de.osthus.ambeth.util.ParamChecker;

public class AmbethPlatformContext implements IAmbethPlatformContext
{
	private class AmbethPlatformContextModule implements IInitializingModule
	{
		protected final AmbethPlatformContext apc;

		public AmbethPlatformContextModule(AmbethPlatformContext apc)
		{
			this.apc = apc;
		}

		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerExternalBean(apc).autowireable(IAmbethPlatformContext.class);
		}
	}

	public static IAmbethPlatformContext create(Properties props, Class<?>[] providerModules, Class<?>[] frameworkModules, Class<?>[] bootstrapModules,
			IInitializingModule[] providerModuleInstances, final IInitializingModule[] frameworkModuleInstances,
			final IInitializingModule[] bootstrapModuleInstances)
	{
		ParamChecker.assertParamNotNull(props, "props");

		IServiceContext bootstrapContext = null;
		final AmbethPlatformContext apc = new AmbethPlatformContext();
		try
		{
			IInitializingModule[] providerModuleInstancesCopy = new IInitializingModule[providerModuleInstances.length + 1];
			System.arraycopy(providerModuleInstances, 0, providerModuleInstancesCopy, 0, providerModuleInstances.length);
			providerModuleInstancesCopy[providerModuleInstancesCopy.length - 1] = apc.new AmbethPlatformContextModule(apc);
			providerModuleInstances = providerModuleInstancesCopy;

			bootstrapContext = BeanContextFactory.createBootstrap(props, providerModules, providerModuleInstances);

			IList<IModuleProvider> moduleProviders = bootstrapContext.getImplementingObjects(IModuleProvider.class);
			for (int a = moduleProviders.size(); a-- > 0;)
			{
				IModuleProvider moduleProvider = moduleProviders.get(a);
				Class<?>[] mpFrameworkModules = moduleProvider.getFrameworkModules();
				Class<?>[] mpBootstrapModules = moduleProvider.getBootstrapModules();
				frameworkModules = ModuleUtil.mergeModules(mpFrameworkModules, frameworkModules);
				bootstrapModules = ModuleUtil.mergeModules(mpBootstrapModules, bootstrapModules);
			}
			IServiceContext frameworkBeanContext = bootstrapContext;

			if (frameworkModules.length > 0 || frameworkModuleInstances.length > 0)
			{
				frameworkBeanContext = bootstrapContext.createService("framework", new IBackgroundWorkerParamDelegate<IBeanContextFactory>()
				{
					@Override
					public void invoke(IBeanContextFactory childContextFactory)
					{
						for (int a = frameworkModuleInstances.length; a-- > 0;)
						{
							childContextFactory.registerExternalBean(frameworkModuleInstances[a]);
						}
					}
				}, frameworkModules);
			}

			ILightweightTransaction transaction = frameworkBeanContext.getService(ILightweightTransaction.class, false);
			if (transaction != null)
			{
				ILogger log = LoggerFactory.getLogger(AmbethPlatformContext.class, props);
				if (log.isInfoEnabled())
				{
					log.info("Starting initial database transaction to receive metadata for OR-Mappings...");
				}
				transaction.runInTransaction(new IBackgroundWorkerDelegate()
				{
					@Override
					public void invoke() throws Throwable
					{
						// Intended blank
					}
				});
				if (log.isInfoEnabled())
				{
					log.info("Initial database transaction processed successfully");
				}
			}
			IServiceContext applicationBeanContext = frameworkBeanContext;

			if (bootstrapModules.length > 0 || bootstrapModuleInstances.length > 0)
			{
				applicationBeanContext = frameworkBeanContext.createService("application", new IBackgroundWorkerParamDelegate<IBeanContextFactory>()
				{
					@Override
					public void invoke(IBeanContextFactory childContextFactory)
					{
						for (int a = bootstrapModuleInstances.length; a-- > 0;)
						{
							childContextFactory.registerExternalBean(bootstrapModuleInstances[a]);
						}
					}
				}, bootstrapModules);
			}
			apc.beanContext = applicationBeanContext;
			return apc;
		}
		catch (Throwable e)
		{
			if (bootstrapContext != null)
			{
				IThreadLocalCleanupController tlCleanupController = bootstrapContext.getService(IThreadLocalCleanupController.class);
				bootstrapContext.dispose();
				tlCleanupController.cleanupThreadLocal();
			}
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected IServiceContext beanContext;

	protected IEventQueue eventQueue;

	@Override
	public void dispose()
	{
		if (beanContext == null)
		{
			return;
		}
		IServiceContext rootContext = beanContext.getRoot();
		IThreadLocalObjectCollector tlObjectCollector = rootContext.getService(IThreadLocalObjectCollector.class);
		beanContext.getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
		rootContext.dispose();
		beanContext = null;
		if (tlObjectCollector instanceof ThreadLocalObjectCollector)
		{
			((ThreadLocalObjectCollector) tlObjectCollector).clearThreadLocal();
			((ThreadLocalObjectCollector) tlObjectCollector).clearThreadLocals();
		}
	}

	@Override
	public IServiceContext getBeanContext()
	{
		return beanContext;
	}

	@Override
	public void clearThreadLocal()
	{
		IThreadLocalCleanupController threadLocalCleanupController = beanContext.getService(IThreadLocalCleanupController.class);
		threadLocalCleanupController.cleanupThreadLocal();
	}

	@Override
	public void afterBegin()
	{
		if (eventQueue != null)
		{
			eventQueue.enableEventQueue();
		}
	}

	@Override
	public void afterCommit()
	{
		afterEnd();
	}

	@Override
	public void afterRollback()
	{
		afterEnd();
	}

	protected void afterEnd()
	{
		try
		{
			if (eventQueue != null)
			{
				eventQueue.flushEventQueue();
			}
		}
		finally
		{
			try
			{
				IDatabaseProvider databaseProvider = beanContext.getService("databaseProvider", IDatabaseProvider.class, false);
				IDatabase database = databaseProvider != null ? databaseProvider.tryGetInstance() : null;
				if (database != null)
				{
					database.dispose();
				}
			}
			finally
			{
				clearThreadLocal();
			}
		}
	}
}
