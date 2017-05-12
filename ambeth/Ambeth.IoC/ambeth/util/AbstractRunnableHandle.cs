using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Threading;
using System;
using System.Collections.Generic;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
#if !SILVERLIGHT
    public abstract class AbstractRunnableHandle<V>
    {
        public readonly Object parallelLock = new Object();

        public readonly CountDownLatch latch;

        public readonly IForkState forkState;

        public readonly ParamHolder<Exception> exHolder;

        public readonly IList<V> items;

        public readonly IThreadLocalCleanupController threadLocalCleanupController;

        public readonly Thread createdThread = Thread.CurrentThread;

        public AbstractRunnableHandle(IList<V> items, IThreadLocalCleanupController threadLocalCleanupController)
        {
            this.latch = new CountDownLatch(items.Count);
            this.exHolder = new InterruptingParamHolder(Thread.CurrentThread);
            this.items = items;
            this.threadLocalCleanupController = threadLocalCleanupController;
            this.forkState = threadLocalCleanupController.CreateForkState();
        }
    }
#endif
}