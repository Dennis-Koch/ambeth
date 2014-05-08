using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Ioc.Hierarchy;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Link;

namespace De.Osthus.Ambeth.Hierarchy
{
    public class NamedBeanContextHolder<V> : NamedBeanContextHolder, IBeanContextHolder<V>
    {
        public NamedBeanContextHolder(IServiceContext beanContext, String beanName) : base(beanContext, beanName)
        {
            // Intended blank
        }

        public V GetTypedValue()
        {
            if (beanContext == null)
            {
                throw new NotSupportedException("This bean context has already been disposed!");
            }
            return beanContext.GetService<V>(beanName, true);
        }
    }

    public class NamedBeanContextHolder : IBeanContextHolder
    {
        protected String beanName;

        protected IServiceContext beanContext;

        public NamedBeanContextHolder(IServiceContext beanContext, String beanName)
        {
            this.beanContext = beanContext;
            this.beanName = beanName;
        }

        public ILinkRuntimeExtendable LinkExtendable
        {
            get
            {
                if (beanContext == null)
                {
                    throw new NotSupportedException("This bean context has already been disposed!");
                }
                return beanContext;
            }
        }

        public Object GetValue()
        {
            if (beanContext == null)
            {
                throw new NotSupportedException("This bean context has already been disposed!");
            }
            return beanContext.GetService(beanName, true);
        }

        public void Dispose()
        {
            if (beanContext != null)
            {
                try
                {
                    beanContext.Dispose();
                }
                finally
                {
                    beanContext = null;
                    beanName = null;
                }
            }
        }
    }
}
