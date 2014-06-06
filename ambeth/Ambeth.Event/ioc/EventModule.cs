using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Event.Config;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Remote;
using De.Osthus.Ambeth.Service;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class EventModule : IInitializingModule
    {
        [LogInstance]
		public ILogger Log { private get; set; }
                
        [Property(ServiceConfigurationConstants.NetworkClientMode, DefaultValue="false")]
        public bool IsNetworkClientMode { get; set; }

        [Property(EventConfigurationConstants.PollingActive, DefaultValue = "false")]
        public bool IsPollingActive { get; set; }

        [Property(EventConfigurationConstants.EventServiceBeanActive, DefaultValue = "true")]
        public bool IsEventServiceBeanActive { get; set; }
        
        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterBean<EventListenerRegistry>("eventListenerRegistry").Autowireable(typeof(IEventListenerExtendable), typeof(IEventBatcherExtendable),
                typeof(IEventTargetExtractorExtendable), typeof(IEventBatcher), typeof(IEventDispatcher), typeof(IEventListener), typeof(IEventQueue));

            if (IsNetworkClientMode && IsEventServiceBeanActive)
            {
                beanContextFactory.RegisterBean<ClientServiceBean>("eventServiceWCF")
                    .PropertyValue("Interface", typeof(IEventService))
                    .PropertyValue("SyncRemoteInterface", typeof(IEventServiceWCF))
                    .PropertyValue("AsyncRemoteInterface", typeof(IEventClient))
                    .Autowireable<IEventService>();
                //beanContextFactory.registerBean<EventServiceDelegate>("eventService").autowireable<IEventService>();

                if (IsPollingActive)
                {
                    beanContextFactory.RegisterBean<EventPoller>("eventPoller").Autowireable<IEventPoller>();
                    beanContextFactory.Link("eventPoller").To<IOfflineListenerExtendable>();
                }
                else
                {
                    if (Log.InfoEnabled)
                    {
                        Log.Info("Event polling disabled. Reason: property '" + EventConfigurationConstants.PollingActive + "' set to '" + IsPollingActive + "'");
                    }
                }
            }
        }
    }
}
