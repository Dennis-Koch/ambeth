using System;
using De.Osthus.Ambeth.Ioc.Link;
using System.Collections.Generic;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Ioc.Hierarchy
{
    public class PrototypeContextHandle : AbstractSingletonContextHandle
    {
	    protected override IServiceContext GetChildContext()
	    {
            return null;
	    }

        protected override void SetChildContext(IServiceContext childContext)
	    {
            // Intended blank
	    }

        public override IServiceContext Start(IDictionary<String, Object> namedBeans)
        {
            return StartIntern(delegate(IBeanContextFactory childContextFactory)
            {
                if (Content != null)
                {
                    Content.Invoke(childContextFactory);
                }
                foreach (KeyValuePair<String, Object> entry in namedBeans)
                {
                    childContextFactory.RegisterExternalBean(entry.Key, entry.Value);
                }
            });
        }

        public override IServiceContext Start(IBackgroundWorkerParamDelegate<IBeanContextFactory> content)
        {
            return StartIntern(delegate(IBeanContextFactory childContextFactory)
            {
                if (Content != null)
                {
                    Content.Invoke(childContextFactory);
                }
                content.Invoke(childContextFactory);
            });
        }

        public override void Stop()
        {
            writeLock.Lock();
            try
            {
                IList<IServiceContext> children = new List<IServiceContext>(childContexts);
                foreach (IServiceContext childContext in children)
                {
                    childContext.Dispose();
                }
            }
            finally
            {
                writeLock.Unlock();
            }
        }
    }
}
