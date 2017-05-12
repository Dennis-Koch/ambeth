using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Ioc.Extendable;

namespace De.Osthus.Ambeth.Cache
{
    public class ServiceResultProcessorRegistry : IInitializingBean, IServiceResultProcessorExtendable, IServiceResultProcessorRegistry
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        protected IMapExtendableContainer<Type, IServiceResultProcessor> extensions = new ClassExtendableContainer<IServiceResultProcessor>("serviceResultProcessor", "returnType");

        public void AfterPropertiesSet()
        {
            // Intended blank
        }

        public IServiceResultProcessor GetServiceResultProcessor(Type returnType)
        {
            return extensions.GetExtension(returnType);
        }

        public void RegisterServiceResultProcessor(IServiceResultProcessor serviceResultProcessor, Type returnType)
        {
            extensions.Register(serviceResultProcessor, returnType);
        }

        public void UnregisterServiceResultProcessor(IServiceResultProcessor serviceResultProcessor, Type returnType)
        {
            extensions.Unregister(serviceResultProcessor, returnType);
        }
    }
}