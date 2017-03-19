package com.koch.ambeth.platform;

import com.koch.ambeth.event.IEventQueue;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IModuleProvider;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.ioc.util.ImmutableTypeSet;
import com.koch.ambeth.ioc.util.ModuleUtil;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LoggerFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.objectcollector.ThreadLocalObjectCollector;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public class AmbethPlatformContext implements IAmbethPlatformContext {
	private class AmbethPlatformContextModule implements IInitializingModule {
		protected final AmbethPlatformContext apc;

		public AmbethPlatformContextModule(AmbethPlatformContext apc) {
			this.apc = apc;
		}

		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerExternalBean(apc).autowireable(IAmbethPlatformContext.class);
		}
	}

	private static DisposeDatabaseExtension disposeDatabaseExtension;

	static {
		try {
			disposeDatabaseExtension = new DisposeDatabaseExtension();
		}
		catch (NoClassDefFoundError e) {
			// intended blank
		}
	}

	public static IAmbethPlatformContext create(Properties props, Class<?>[] providerModules,
			Class<?>[] frameworkModules, Class<?>[] bootstrapModules,
			IInitializingModule[] providerModuleInstances,
			final IInitializingModule[] frameworkModuleInstances,
			final IInitializingModule[] bootstrapModuleInstances) {
		ParamChecker.assertParamNotNull(props, "props");

		IServiceContext bootstrapContext = null;
		final AmbethPlatformContext apc = new AmbethPlatformContext();
		try {
			IInitializingModule[] providerModuleInstancesCopy =
					new IInitializingModule[providerModuleInstances.length + 1];
			System.arraycopy(providerModuleInstances, 0, providerModuleInstancesCopy, 0,
					providerModuleInstances.length);
			providerModuleInstancesCopy[providerModuleInstancesCopy.length - 1] =
					apc.new AmbethPlatformContextModule(apc);
			providerModuleInstances = providerModuleInstancesCopy;

			bootstrapContext =
					BeanContextFactory.createBootstrap(props, providerModules, providerModuleInstances);

			IList<IModuleProvider> moduleProviders =
					bootstrapContext.getImplementingObjects(IModuleProvider.class);
			for (int a = moduleProviders.size(); a-- > 0;) {
				IModuleProvider moduleProvider = moduleProviders.get(a);
				Class<?>[] mpFrameworkModules = moduleProvider.getFrameworkModules();
				Class<?>[] mpBootstrapModules = moduleProvider.getBootstrapModules();
				frameworkModules = ModuleUtil.mergeModules(mpFrameworkModules, frameworkModules);
				bootstrapModules = ModuleUtil.mergeModules(mpBootstrapModules, bootstrapModules);
			}
			IServiceContext frameworkBeanContext = bootstrapContext;

			if (frameworkModules.length > 0 || frameworkModuleInstances.length > 0) {
				frameworkBeanContext = bootstrapContext.createService("framework",
						new IBackgroundWorkerParamDelegate<IBeanContextFactory>() {
							@Override
							public void invoke(IBeanContextFactory childContextFactory) {
								for (int a = frameworkModuleInstances.length; a-- > 0;) {
									childContextFactory.registerExternalBean(frameworkModuleInstances[a]);
								}
							}
						}, frameworkModules);
			}

			ILightweightTransaction transaction =
					frameworkBeanContext.getService(ILightweightTransaction.class, false);
			if (transaction != null) {
				ILogger log = LoggerFactory.getLogger(AmbethPlatformContext.class, props);
				if (log.isInfoEnabled()) {
					log.info("Starting initial database transaction to receive metadata for OR-Mappings...");
				}
				transaction.runInTransaction(new IBackgroundWorkerDelegate() {
					@Override
					public void invoke() throws Throwable {
						// Intended blank
					}
				});
				if (log.isInfoEnabled()) {
					log.info("Initial database transaction processed successfully");
				}
			}
			IServiceContext applicationBeanContext = frameworkBeanContext;

			if (bootstrapModules.length > 0 || bootstrapModuleInstances.length > 0) {
				applicationBeanContext = frameworkBeanContext.createService("application",
						new IBackgroundWorkerParamDelegate<IBeanContextFactory>() {
							@Override
							public void invoke(IBeanContextFactory childContextFactory) {
								for (int a = bootstrapModuleInstances.length; a-- > 0;) {
									childContextFactory.registerExternalBean(bootstrapModuleInstances[a]);
								}
							}
						}, bootstrapModules);
			}
			apc.beanContext = applicationBeanContext;
			return apc;
		}
		catch (Throwable e) {
			if (bootstrapContext != null) {
				IThreadLocalCleanupController tlCleanupController =
						bootstrapContext.getService(IThreadLocalCleanupController.class);
				bootstrapContext.dispose();
				tlCleanupController.cleanupThreadLocal();
			}
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected IServiceContext beanContext;

	protected IEventQueue eventQueue;

	@Override
	public void dispose() {
		if (beanContext == null) {
			return;
		}
		IServiceContext rootContext = beanContext.getRoot();
		IThreadLocalObjectCollector tlObjectCollector =
				rootContext.getService(IThreadLocalObjectCollector.class);
		beanContext.getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
		rootContext.dispose();
		beanContext = null;
		if (tlObjectCollector instanceof ThreadLocalObjectCollector) {
			((ThreadLocalObjectCollector) tlObjectCollector).clearThreadLocal();
			((ThreadLocalObjectCollector) tlObjectCollector).clearThreadLocals();
		}
		ImmutableTypeSet.flushState();
	}

	@Override
	public IServiceContext getBeanContext() {
		return beanContext;
	}

	@Override
	public void clearThreadLocal() {
		IThreadLocalCleanupController threadLocalCleanupController =
				beanContext.getService(IThreadLocalCleanupController.class);
		threadLocalCleanupController.cleanupThreadLocal();
	}

	@Override
	public void afterBegin() {
		if (eventQueue != null) {
			eventQueue.enableEventQueue();
		}
	}

	@Override
	public void afterCommit() {
		afterEnd();
	}

	@Override
	public void afterRollback() {
		afterEnd();
	}

	protected void afterEnd() {
		try {
			if (eventQueue != null) {
				eventQueue.flushEventQueue();
			}
		}
		finally {
			try {
				if (disposeDatabaseExtension != null) {
					disposeDatabaseExtension.disposeDatabase(beanContext);
				}
			}
			finally {
				clearThreadLocal();
			}
		}
	}
}
