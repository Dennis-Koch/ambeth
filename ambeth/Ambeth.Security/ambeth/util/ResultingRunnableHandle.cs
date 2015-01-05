using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Threading;
using System;
using System.Collections.Generic;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
#if !SILVERLIGHT
    public class ResultingRunnableHandle<R, V> : AbstractRunnableHandle<V>
    {
        public readonly IResultingBackgroundWorkerParamDelegate<R, V> run;

        public readonly IAggregrateResultHandler<R, V> aggregrateResultHandler;

        public ResultingRunnableHandle(IResultingBackgroundWorkerParamDelegate<R, V> run, IAggregrateResultHandler<R, V> aggregrateResultHandler, IList<V> items, IThreadLocalCleanupController threadLocalCleanupController) : base(items, threadLocalCleanupController)
        {
            this.run = run;
            this.aggregrateResultHandler = aggregrateResultHandler;
        }
    }
#endif
}