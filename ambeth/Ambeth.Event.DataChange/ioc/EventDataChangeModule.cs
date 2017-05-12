using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Event.Config;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Factory;
using System;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class EventDataChangeModule : IInitializingModule
    {
        [Property(EventConfigurationConstants.EventManagerName, Mandatory = false)]
        public String EventManagerName { protected get; set; }

        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            if (EventManagerName != null)
            {
                beanContextFactory.Link(EventManagerName).To<IEventListenerExtendable>().With(typeof(IDataChange));
            }
        }
    }
}