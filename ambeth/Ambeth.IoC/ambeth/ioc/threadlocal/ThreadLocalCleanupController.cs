using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Reflection;
using System.Threading;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public class ThreadLocalCleanupController : IInitializingBean, IThreadLocalCleanupBeanExtendable, IThreadLocalCleanupController
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly DefaultExtendableContainer<IThreadLocalCleanupBean> listeners = new DefaultExtendableContainer<IThreadLocalCleanupBean>("threadLocalCleanupBean");

        protected ForkStateEntry[] cachedForkStateEntries;

        public virtual void AfterPropertiesSet()
        {
            // Intended blank
        }

        public virtual void CleanupThreadLocal()
        {
            IThreadLocalCleanupBean[] extensions = listeners.GetExtensions();
            for (int a = 0, size = extensions.Length; a < size; a++)
            {
                extensions[a].CleanupThreadLocal();
            }
        }

        protected ForkStateEntry[] GetForkStateEntries()
	    {
		    ForkStateEntry[] cachedForkStateEntries = this.cachedForkStateEntries;
		    if (cachedForkStateEntries != null)
		    {
			    return cachedForkStateEntries;
		    }
		    Lock writeLock = listeners.GetWriteLock();
		    writeLock.Lock();
		    try
		    {
			    // check again: concurrent thread might have been faster
			    cachedForkStateEntries = this.cachedForkStateEntries;
			    if (cachedForkStateEntries != null)
			    {
				    return cachedForkStateEntries;
			    }
			    IThreadLocalCleanupBean[] extensions = listeners.GetExtensions();
			    List<ForkStateEntry> forkStateEntries = new List<ForkStateEntry>(extensions.Length);
			    for (int a = 0, size = extensions.Length; a < size; a++)
			    {
				    IThreadLocalCleanupBean extension = extensions[a];
				    FieldInfo[] fields = ReflectUtil.GetDeclaredFieldsInHierarchy(extension.GetType());
				    foreach (FieldInfo field in fields)
				    {
					    Forkable forkable = AnnotationUtil.GetAnnotation<Forkable>(field, false);
					    if (forkable == null)
					    {
						    continue;
					    }
					    Object valueTL = field.GetValue(extension);
					    if (valueTL == null)
					    {
						    continue;
					    }
					    forkStateEntries.Add(new ForkStateEntry(extension, valueTL, forkable.Value));
				    }
			    }
			    cachedForkStateEntries = forkStateEntries.ToArray();
			    this.cachedForkStateEntries = cachedForkStateEntries;
			    return cachedForkStateEntries;
		    }
		    finally
		    {
			    writeLock.Unlock();
		    }
	    }

	    public IForkState CreateForkState()
	    {
		    ForkStateEntry[] forkStateEntries = GetForkStateEntries();

		    IForkedValueResolver[] oldValues = new IForkedValueResolver[forkStateEntries.Length];
		    for (int a = 0, size = forkStateEntries.Length; a < size; a++)
		    {
			    ForkStateEntry forkStateEntry = forkStateEntries[a];
                Object value = forkStateEntry.getValueMI.Invoke(forkStateEntry.valueTL, ForkStateEntry.EMPTY_ARGS);
			    if (value != null && ForkableType.SHALLOW_COPY.Equals(forkStateEntry.forkableType))
			    {
				    throw new Exception("Could not clone " + value);
			    }
			    else
			    {
				    oldValues[a] = new ReferenceValueResolver(value);
			    }
		    }
		    return new ForkState(forkStateEntries, oldValues);
	    }

        public void RegisterThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean)
        {
            listeners.Register(threadLocalCleanupBean);
        }

        public void UnregisterThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean)
        {
            listeners.Unregister(threadLocalCleanupBean);
        }
    }
}
