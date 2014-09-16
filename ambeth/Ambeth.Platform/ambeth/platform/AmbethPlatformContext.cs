using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Database;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Persistence;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
namespace De.Osthus.Ambeth.Platform
{
    public class AmbethPlatformContext : IAmbethPlatformContext
    {
        private class AmbethPlatformContextModule : IInitializingModule
        {
            protected readonly AmbethPlatformContext apc;

            public AmbethPlatformContextModule(AmbethPlatformContext apc)
            {
                this.apc = apc;
            }

            public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
            {
                beanContextFactory.RegisterExternalBean(apc).Autowireable<IAmbethPlatformContext>();
            }
        }

        public static IAmbethPlatformContext Create(Properties props, Type[] providerModules, Type[] frameworkModules, Type[] bootstrapModules,
                IInitializingModule[] providerModuleInstances, IInitializingModule[] frameworkModuleInstances,
                IInitializingModule[] bootstrapModuleInstances)
        {
            ParamChecker.AssertParamNotNull(props, "props");

            IServiceContext bootstrapContext = null;
            AmbethPlatformContext apc = new AmbethPlatformContext();
            try
            {
                IInitializingModule[] providerModuleInstancesCopy = new IInitializingModule[providerModuleInstances.Length + 1];
                Array.Copy(providerModuleInstances, 0, providerModuleInstancesCopy, 0, providerModuleInstances.Length);
                providerModuleInstancesCopy[providerModuleInstancesCopy.Length - 1] = new AmbethPlatformContextModule(apc);
                providerModuleInstances = providerModuleInstancesCopy;

                bootstrapContext = BeanContextFactory.CreateBootstrap(props, providerModules, providerModuleInstances);

                IList<IModuleProvider> moduleProviders = bootstrapContext.GetImplementingObjects<IModuleProvider>();
                for (int a = moduleProviders.Count; a-- > 0; )
                {
                    IModuleProvider moduleProvider = moduleProviders[a];
                    Type[] mpFrameworkModules = moduleProvider.GetFrameworkModules();
                    Type[] mpBootstrapModules = moduleProvider.GetBootstrapModules();
                    frameworkModules = ModuleUtil.MergeModules(mpFrameworkModules, frameworkModules);
                    bootstrapModules = ModuleUtil.MergeModules(mpBootstrapModules, bootstrapModules);
                }
                IServiceContext frameworkBeanContext = bootstrapContext;

                if (frameworkModules.Length > 0 || frameworkModuleInstances.Length > 0)
                {
                    frameworkBeanContext = bootstrapContext.CreateService("framework", delegate(IBeanContextFactory childContextFactory)
                        {
                            for (int a = frameworkModuleInstances.Length; a-- > 0; )
                            {
                                childContextFactory.RegisterExternalBean(frameworkModuleInstances[a]);
                            }
                        }, frameworkModules);
                }

                ITransaction transaction = frameworkBeanContext.GetService<ITransaction>(false);
                if (transaction != null)
                {
                    ILogger log = LoggerFactory.GetLogger(typeof(AmbethPlatformContext), props);
                    if (log.InfoEnabled)
                    {
                        log.Info("Starting initial database transaction to receive metadata for OR-Mappings...");
                    }
                    transaction.ProcessAndCommit(delegate(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
                        {
                            // Intended blank
                        });
                    if (log.InfoEnabled)
                    {
                        log.Info("Initial database transaction processed successfully");
                    }
                }
                IServiceContext applicationBeanContext = frameworkBeanContext;

                if (bootstrapModules.Length > 0 || bootstrapModuleInstances.Length > 0)
                {
                    applicationBeanContext = frameworkBeanContext.CreateService("application", delegate(IBeanContextFactory childContextFactory)
                        {
                            for (int a = bootstrapModuleInstances.Length; a-- > 0; )
                            {
                                childContextFactory.RegisterExternalBean(bootstrapModuleInstances[a]);
                            }
                        }, bootstrapModules);
                }
                apc.beanContext = applicationBeanContext;
                return apc;
            }
            catch (Exception)
            {
                if (bootstrapContext != null)
                {
                    IThreadLocalCleanupController tlCleanupController = bootstrapContext.GetService<IThreadLocalCleanupController>();
                    bootstrapContext.Dispose();
                    tlCleanupController.CleanupThreadLocal();
                }
                throw;
            }
        }

        protected IServiceContext beanContext;

        protected IEventQueue eventQueue;

        public void Dispose()
        {
            if (beanContext == null)
            {
                return;
            }
            IServiceContext rootContext = beanContext.GetRoot();
            beanContext.GetService<IThreadLocalCleanupController>().CleanupThreadLocal();
            rootContext.Dispose();
            beanContext = null;
        }

        public IServiceContext GetBeanContext()
        {
            return beanContext;
        }

        public void ClearThreadLocal()
        {
            IThreadLocalCleanupController threadLocalCleanupController = beanContext.GetService<IThreadLocalCleanupController>();
            threadLocalCleanupController.CleanupThreadLocal();
        }

        public void AfterBegin()
        {
            if (eventQueue != null)
            {
                eventQueue.EnableEventQueue();
            }
        }

        public void AfterCommit()
        {
            AfterEnd();
        }

        public void AfterRollback()
        {
            AfterEnd();
        }

        protected void AfterEnd()
        {
            try
            {
                if (eventQueue != null)
                {
                    eventQueue.FlushEventQueue();
                }
            }
            finally
            {
                try
                {
                    IDatabaseProvider databaseProvider = beanContext.GetService<IDatabaseProvider>("databaseProvider", false);
                    IDatabase database = databaseProvider != null ? databaseProvider.TryGetInstance() : null;
                    if (database != null)
                    {
                        database.Dispose();
                    }
                }
                finally
                {
                    ClearThreadLocal();
                }
            }
        }
    }
}