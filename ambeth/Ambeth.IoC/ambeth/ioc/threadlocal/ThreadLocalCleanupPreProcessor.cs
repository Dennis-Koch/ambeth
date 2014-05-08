using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public class ThreadLocalCleanupPreProcessor : IInitializingBean, IBeanPreProcessor
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public IThreadLocalCleanupBeanExtendable ThreadLocalCleanupBeanExtendable { protected get; set; }

        public void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ThreadLocalCleanupBeanExtendable, "ThreadLocalCleanupBeanExtendable");
        }

        public void PreProcessProperties(IBeanContextFactory beanContextFactory, IProperties props, String beanName, Object service, Type beanType,
                IList<IPropertyConfiguration> propertyConfigs, IPropertyInfo[] properties)
        {
            if (service is IThreadLocalCleanupBean)
            {
                if (Log.DebugEnabled)
                {
                    Log.Debug("Registered bean '" + beanName + "' to " + typeof(IThreadLocalCleanupBeanExtendable).Name + " because it implements "
                            + typeof(IThreadLocalCleanupBean).Name);
                }
                beanContextFactory.Link(service).To<IThreadLocalCleanupBeanExtendable>();
            }
        }
    }
}