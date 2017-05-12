using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Ioc.Hierarchy;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Link;

namespace De.Osthus.Ambeth.Hierarchy
{
    public class BeanContextHolder<V> : BeanContextHolder, IBeanContextHolder<V>
    {        
        public BeanContextHolder(IServiceContext serviceContext) : base(serviceContext, typeof(V))
        {
            // Intended blank
        }

        public V GetTypedValue()
        {
            if (beanContext == null)
            {
                throw new NotSupportedException("This bean context has already been disposed!");
            }
            return beanContext.GetService<V>(true);
        }
    }

    public class BeanContextHolder : IBeanContextHolder
    {        
        protected Type autowiredBeanType;

        protected IServiceContext beanContext;

        public BeanContextHolder(IServiceContext beanContext, Type autowiredBeanType)
        {
            this.beanContext = beanContext;
            this.autowiredBeanType = autowiredBeanType;
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
            return beanContext.GetService(autowiredBeanType, true);
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
                    autowiredBeanType = null;
                }
            }
        }
    }
}
