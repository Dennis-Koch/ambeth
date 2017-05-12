using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Ioc.Extendable;

namespace De.Osthus.Ambeth.Service
{
    public class ServiceByNameProvider : IServiceByNameProvider, IServiceExtendable, IInitializingBean
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        protected MapExtendableContainer<String, Object> serviceNameToObjectMap;

        public virtual void AfterPropertiesSet()
        {
            serviceNameToObjectMap = new MapExtendableContainer<String, Object>("serviceName", "service");
        }

        public void registerService(Object service, String serviceName)
        {
            serviceNameToObjectMap.Register(service, serviceName);
        }
        public void unregisterService(Object service, String serviceName)
        {
            serviceNameToObjectMap.Unregister(service, serviceName);
        }
        public Object GetService(String serviceName)
        {
            return serviceNameToObjectMap.GetExtension(serviceName);
        }
    }
}
