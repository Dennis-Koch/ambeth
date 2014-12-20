using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Threading;
using System;
using System.Collections.Generic;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
#if !SILVERLIGHT
    public class RunnableHandle<R, V>
    {
        public readonly IResultingBackgroundWorkerParamDelegate<R, V> run;

        public readonly IAggregrateResultHandler<R, V> aggregrateResultHandler;

        public readonly Object parallelLock = new Object();

        public readonly CountDownLatch latch;

        public readonly IForkState forkState;

        public readonly ParamHolder<Exception> exHolder;

        public readonly IList<V> items;

        public readonly IThreadLocalCleanupController threadLocalCleanupController;

        public RunnableHandle(IResultingBackgroundWorkerParamDelegate<R, V> run, IAggregrateResultHandler<R, V> aggregrateResultHandler, IList<V> items,
                IThreadLocalCleanupController threadLocalCleanupController)
        {
            this.run = run;
            this.aggregrateResultHandler = aggregrateResultHandler;
            this.latch = new CountDownLatch(items.Count);
            this.exHolder = new InterruptingParamHolder(Thread.CurrentThread);
            this.items = items;
            this.threadLocalCleanupController = threadLocalCleanupController;
            this.forkState = threadLocalCleanupController.CreateForkState();
        }
    }
#endif
}