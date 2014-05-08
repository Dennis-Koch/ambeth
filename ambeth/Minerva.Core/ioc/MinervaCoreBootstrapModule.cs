using System;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Minerva.Bind;

#if SILVERLIGHT
using De.Osthus.Minerva.Dialogs;
using De.Osthus.Minerva.Command;
using De.Osthus.Minerva.Converter;
#endif
using De.Osthus.Minerva.Core;
using De.Osthus.Minerva.Security;
using De.Osthus.Ambeth.Log;
using System.Threading;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Cache.Config;
using De.Osthus.Ambeth.Merge.Config;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Minerva.Busy;
using De.Osthus.Ambeth.Merge;

namespace De.Osthus.Minerva.Ioc
{
    [FrameworkModule]
    public class MinervaCoreBootstrapModule : IInitializingBootstrapModule
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        [Property(CacheConfigurationConstants.CacheServiceBeanActive, DefaultValue = "true")]
        public virtual bool IsCacheServiceBeanActive { get; set; }
		
        [Property(MergeConfigurationConstants.MergeServiceBeanActive, DefaultValue = "true")]
        public virtual bool IsMergeServiceBeanActive { get; set; }

        [Property(ServiceConfigurationConstants.NetworkClientMode, DefaultValue = "false")]
        public bool IsNetworkClientMode { get; set; }

        [Property(MergeConfigurationConstants.MergeServiceMockType, Mandatory=false)]
        public virtual Type MergeServiceMockType { get; set; }        

        public virtual void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterBean<SecurityScopeProvider>("securityScopeProvider").Autowireable(typeof(ISecurityScopeProvider), typeof(ISecurityScopeChangeExtendable));

#if SILVERLIGHT
            beanContextFactory.RegisterBean<WindowFactory>("windowFactory").Autowireable<IWindowFactory>();
#endif
            beanContextFactory.RegisterBean<BusyController>("busyController").Autowireable<IBusyController>();

            //beanContextFactory.RegisterBean<ClientEntityFactory>("clientEntityFactory").Autowireable<IEntityFactory>();

            beanContextFactory.RegisterBean<UserControlPostProcessor>("UserControlPostProcessor");

            beanContextFactory.RegisterBean<DataChangeControllerPostProcessor>("dataChangeControllerPostProcessor");

#if SILVERLIGHT
            beanContextFactory.RegisterBean<CommandBindingHelper>("commandBindingHelper").Autowireable<ICommandBindingHelper>();
#endif
            SynchronizationContext current = SynchronizationContext.Current;
            if (current != null)
            {
                beanContextFactory.RegisterExternalBean("synchronizationContext", current).Autowireable<SynchronizationContext>();
            }

            beanContextFactory.RegisterBean<SharedData>("sharedData").Autowireable(typeof(ISharedData), typeof(ISharedDataHandOnExtendable));

#if SILVERLIGHT
            beanContextFactory.RegisterBean<FilterDescriptorConverter>("filterDescriptorConverter").Autowireable<IFilterDescriptorConverter>();
#endif

            //TODO Replace by CacheServiceExtendable Mocks
            //if (!IsCacheServiceBeanActive)
            //{
            //    beanContextFactory.registerBean<CacheServiceMock>("cacheServiceMock").autowireable(/*typeof(ICacheService),*/ typeof(IPersistenceMock));
            //}
            //if (!IsMergeServiceBeanActive)
            //{
            //    ParamChecker.AssertNotNull(MergeServiceMockType, "MergeServiceMockType");

            //    //beanContextFactory.registerBean("mergeServiceMock", MergeServiceMockType).autowireable<IMergeService>();
            //}
            
        }
    }
}
