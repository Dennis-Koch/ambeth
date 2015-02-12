using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Threading;
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

        public IServiceContext BeanContext { protected get; set; }

        protected readonly DefaultExtendableContainer<IThreadLocalCleanupBean> listeners = new DefaultExtendableContainer<IThreadLocalCleanupBean>("threadLocalCleanupBean");

        protected ForkStateEntry[] cachedForkStateEntries;

        protected readonly IdentityHashMap<IThreadLocalCleanupBean, WeakReference> extensionToContextMap = new IdentityHashMap<IThreadLocalCleanupBean, WeakReference>();

        protected readonly IdentityWeakHashMap<IServiceContext, ParamHolder<bool?>> alreadyHookedContextSet = new IdentityWeakHashMap<IServiceContext, ParamHolder<bool?>>();

        protected readonly IBackgroundWorkerParamDelegate<IServiceContext> foreignContextHook;

        public ThreadLocalCleanupController()
        {
            foreignContextHook = new IBackgroundWorkerParamDelegate<IServiceContext>(delegate(IServiceContext state)
            {
                Lock writeLock = listeners.GetWriteLock();
                writeLock.Lock();
                try
                {
                    cachedForkStateEntries = null;
                }
                finally
                {
                    writeLock.Unlock();
                }
            });
        }

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
                        Type forkProcessorType = forkable.Processor;
                        IForkProcessor forkProcessor = null;
                        if (forkProcessorType != null && !typeof(IForkProcessor).Equals(forkProcessorType))
                        {
                            WeakReference beanContextOfExtensionR = extensionToContextMap.Get(extension);
                            IServiceContext beanContextOfExtension = beanContextOfExtensionR != null ? (IServiceContext)beanContextOfExtensionR.Target : null;
                            if (beanContextOfExtension == null)
                            {
                                beanContextOfExtension = BeanContext;
                            }
                            forkProcessor = beanContextOfExtension.RegisterBean<IForkProcessor>(forkProcessorType).Finish();
                        }
                        forkStateEntries.Add(new ForkStateEntry(extension, field.Name, valueTL, forkable.Value, forkProcessor));
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
                IForkProcessor forkProcessor = forkStateEntry.forkProcessor;
                if (forkProcessor != null)
                {
                    Object value = forkProcessor.ResolveOriginalValue(forkStateEntry.tlBean, forkStateEntry.fieldName, forkStateEntry.valueTL);
                    oldValues[a] = new ForkProcessorValueResolver(value, forkProcessor);
                    continue;
                }
                {
                    Object value = forkStateEntry.getValueMI.Invoke(forkStateEntry.valueTL, ForkStateEntry.EMPTY_ARGS);
                    if (value != null && ForkableType.SHALLOW_COPY.Equals(forkStateEntry.forkableType))
                    {
                        throw new Exception("Could not clone " + value);
                    }
                    else
                    {
                        oldValues[a] = new ReferenceValueResolver(value, value);
                    }
                }
            }
            return new ForkState(forkStateEntries, oldValues);
        }

        public void RegisterThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean)
        {
            Lock writeLock = listeners.GetWriteLock();
            writeLock.Lock();
            try
            {
                listeners.Register(threadLocalCleanupBean);
                cachedForkStateEntries = null;
                IServiceContext currentBeanContext = BeanContextInitializer.GetCurrentBeanContext();
                if (currentBeanContext != null)
                {
                    extensionToContextMap.Put(threadLocalCleanupBean, new WeakReference(currentBeanContext));
                    if (alreadyHookedContextSet.PutIfNotExists(currentBeanContext, null))
                    {
                        ParamHolder<bool?> inactive = new ParamHolder<bool?>();

                        currentBeanContext.RegisterDisposeHook(new IBackgroundWorkerParamDelegate<IServiceContext>(delegate(IServiceContext state)
                            {
                                if (inactive.Value.HasValue && inactive.Value.Value)
                                {
                                    return;
                                }
                                foreignContextHook(state);
                            }));
                        alreadyHookedContextSet.Put(currentBeanContext, inactive);
                    }
                }
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        public void UnregisterThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean)
        {
            Lock writeLock = listeners.GetWriteLock();
            writeLock.Lock();
            try
            {
                listeners.Unregister(threadLocalCleanupBean);
                cachedForkStateEntries = null;
                extensionToContextMap.Remove(threadLocalCleanupBean);
            }
            finally
            {
                writeLock.Unlock();
            }
            // clear this threadlocal a last time before letting the bean alone...
            threadLocalCleanupBean.CleanupThreadLocal();
        }
    }
}
