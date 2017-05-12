using System;
using De.Osthus.Ambeth.Ioc.Link;
using System.Collections.Generic;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Ioc.Hierarchy
{
    public abstract class AbstractSingletonContextHandle : IInitializingBean, IContextHandle
    {
        public class ChildContextFoot : IInitializingBean, IDisposableBean
        {
            public IServiceContext BeanContext { protected get; set; }

            public AbstractSingletonContextHandle ContextHandle { protected get; set; }

            public void AfterPropertiesSet()
            {
                ParamChecker.AssertNotNull(BeanContext, "BeanContext");
                ParamChecker.AssertNotNull(ContextHandle, "ContextHandle");
            }

            public void Destroy()
            {
                ContextHandle.ChildContextDestroyed(BeanContext);
            }
        }

        [LogInstance]
        public ILogger Log { private get; set; }

        public IServiceContext BeanContext { get; set; }

	    public IContextFactory ContextFactory { get; set; }

        public IBackgroundWorkerParamDelegate<IBeanContextFactory> Content { get; set; }

        protected readonly ISet<IServiceContext> childContexts = new IdentityHashSet<IServiceContext>();

	    protected readonly Lock writeLock = new ReadWriteLock().WriteLock;

	    public virtual void AfterPropertiesSet()
	    {
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
	    }

	    protected abstract IServiceContext GetChildContext();

	    protected abstract void SetChildContext(IServiceContext childContext);

        public virtual IServiceContext Start()
	    {
            return StartIntern(Content);
        }

        protected virtual IServiceContext StartIntern(IBackgroundWorkerParamDelegate<IBeanContextFactory> content)
        {
		    writeLock.Lock();
		    try
		    {
			    if (Log.DebugEnabled)
			    {
				    Log.Debug("Looking for existing child context...");
			    }
			    IServiceContext childContext = GetChildContext();
			    if (childContext == null || childContext.IsDisposed)
			    {
                    if (Log.DebugEnabled)
                    {
				        Log.Debug("No valid child context found. Creating new child context");
                    }
                    IBackgroundWorkerParamDelegate<IBeanContextFactory> rpd = new IBackgroundWorkerParamDelegate<IBeanContextFactory>(delegate(IBeanContextFactory beanContextFactory)
                        {
                            if (content != null)
                            {
                                content.Invoke(beanContextFactory);
                            }
                            beanContextFactory.RegisterBean<ChildContextFoot>().PropertyValue("ContextHandle", this);
                        });
                    if (ContextFactory != null)
                    {
                        childContext = ContextFactory.CreateChildContext(rpd);
                    }
                    else
                    {
                        childContext = BeanContext.CreateService(rpd);
                    }
				    SetChildContext(childContext);
                    childContexts.Add(childContext);
                }
			    else if (Log.DebugEnabled)
			    {
				    Log.Debug("Existing child context found and valid");
			    }
			    IList<IUpwakingBean> upwakingBeans = childContext.GetImplementingObjects<IUpwakingBean>();
			    for (int a = 0, size = upwakingBeans.Count; a < size; a++)
			    {
				    upwakingBeans[a].WakeUp();
			    }
                return childContext;
		    }
		    finally
		    {
			    writeLock.Unlock();
		    }
	    }

	    public virtual IServiceContext Start(IDictionary<String, Object> namedBeans)
	    {
		    throw new NotSupportedException();
	    }

        public virtual IServiceContext Start(IBackgroundWorkerParamDelegate<IBeanContextFactory> content)
	    {
		    throw new NotSupportedException();
	    }

        public virtual void Stop()
	    {
		    writeLock.Lock();
		    try
		    {
			    IServiceContext childContext = GetChildContext();
                if (childContext != null)
			    {
                    childContext.Dispose();
                    if (childContexts.Contains(childContext))
                    {
                        throw new System.Exception("This must never happen: The context must have unregistered itself from the set");
                    }
                    SetChildContext(null);
                }
		    }
		    finally
		    {
			    writeLock.Unlock();
		    }
	    }

        protected void ChildContextDestroyed(IServiceContext childContext)
        {
            writeLock.Lock();
            try
            {
                if (!childContexts.Remove(childContext))
                {
                    Log.Warn("ChildContext did not exist in set at this point. This should not occur and may be a bug");
                }
            }
            finally
            {
                writeLock.Unlock();
            }
        }
    }
}
