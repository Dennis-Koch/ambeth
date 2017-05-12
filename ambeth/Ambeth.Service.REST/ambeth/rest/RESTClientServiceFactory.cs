using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Ioc.Factory;

namespace De.Osthus.Ambeth.Rest
{
    public class RESTClientServiceFactory : IClientServiceFactory, IInitializingBean
    {
        public virtual void AfterPropertiesSet()
        {
            // Intended blank
        }

        public virtual Type GetSyncInterceptorType(Type clientInterface)
        {
            return null;
        }

        public virtual Type GetTargetProviderType(Type clientInterface)
        {
            return typeof(RESTClientInterceptor);
        }

        public virtual String GetServiceName(Type clientInterface)
        {
            String name = clientInterface.Name;
            if (name.EndsWith("Client"))
            {
                name = name.Substring(0, name.Length - 6) + "Service";
            }
            else if (name.EndsWith("WCF"))
            {
                name = name.Substring(0, name.Length - 3);
            }
            if (name.StartsWith("I"))
            {
                return name.Substring(1);
            }
            return name;
        }


        public virtual void PostProcessTargetProviderBean(String targetProviderBeanName, IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.Link(targetProviderBeanName).To<IOfflineListenerExtendable>();
        }
    }
}
