using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Threading;
using System;
using System.Collections.Generic;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
#if !SILVERLIGHT
    public class RunnableHandle<V> : AbstractRunnableHandle<V>
    {
        public readonly IBackgroundWorkerParamDelegate<V> run;

        public RunnableHandle(IBackgroundWorkerParamDelegate<V> run, IList<V> items, IThreadLocalCleanupController threadLocalCleanupController) : base(items, threadLocalCleanupController)
        {
            this.run = run;
        }
    }
#endif
}