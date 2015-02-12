using System;
using De.Osthus.Ambeth.Ioc.Link;
using System.Collections.Generic;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Ioc.Factory;

namespace De.Osthus.Ambeth.Ioc.Hierarchy
{
    public class DefaultChildContextFactory : IInitializingBean, IContextFactory
    {
        public static readonly Type[] EMPTY_TYPE_ARRAY = new Type[0];

        [LogInstance]
        public ILogger log { private get; set; }

        public Type[] Modules { protected get; set; }

        public IServiceContext BeanContext { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
            if (Modules == null)
            {
                Modules = EMPTY_TYPE_ARRAY;
            }
        }

        public virtual IServiceContext CreateChildContext(IBackgroundWorkerParamDelegate<IBeanContextFactory> content)
        {
            return BeanContext.CreateService(content, Modules);
        }
    }
}
