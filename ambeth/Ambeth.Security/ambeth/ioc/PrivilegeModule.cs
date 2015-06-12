using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Datachange;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Privilege;
using De.Osthus.Ambeth.Privilege.Config;
using De.Osthus.Ambeth.Privilege.Factory;
using De.Osthus.Ambeth.Remote;
using De.Osthus.Ambeth.Service;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class PrivilegeModule : IInitializingModule
    {
        [Property(ServiceConfigurationConstants.NetworkClientMode, DefaultValue = "false")]
        public virtual bool IsNetworkClientMode { get; set; }

        [Property(PrivilegeConfigurationConstants.PrivilegeServiceBeanActive, DefaultValue = "true")]
        public virtual bool IsPrivilegeServiceBeanActive { get; set; }

        public virtual void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            IBeanConfiguration privilegeProvider = beanContextFactory.RegisterBean<PrivilegeProvider>().Autowireable<IPrivilegeProvider>();
            IBeanConfiguration ppEventListener = beanContextFactory.RegisterBean<UnfilteredDataChangeListener>().PropertyRefs(privilegeProvider);
            beanContextFactory.Link(ppEventListener).To<IEventListenerExtendable>().With(typeof(IDataChange));
       		beanContextFactory.Link(privilegeProvider, PrivilegeProvider.m_HandleClearAllCaches).To<IEventListenerExtendable>().With(typeof(ClearAllCachesEvent));

            beanContextFactory.RegisterBean<EntityPrivilegeFactoryProvider>().Autowireable<IEntityPrivilegeFactoryProvider>();
		    beanContextFactory.RegisterBean<EntityTypePrivilegeFactoryProvider>().Autowireable<IEntityTypePrivilegeFactoryProvider>();

            if (IsNetworkClientMode && IsPrivilegeServiceBeanActive)
            {
                beanContextFactory.RegisterBean<ClientServiceBean>("privilegeServiceWCF")
                    .PropertyValue("Interface", typeof(IPrivilegeService))
                    .PropertyValue("SyncRemoteInterface", typeof(IPrivilegeServiceWCF))
                    .PropertyValue("AsyncRemoteInterface", typeof(IPrivilegeClient)).Autowireable<IPrivilegeService>();
            }
        }
    }
}