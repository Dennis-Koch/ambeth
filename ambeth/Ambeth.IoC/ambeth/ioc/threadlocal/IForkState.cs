using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;
using System.Threading;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public interface IForkState
    {
        void Use(Runnable runnable);

        void Use(IBackgroundWorkerDelegate runnable);

	    void Use<V>(IBackgroundWorkerParamDelegate<V> runnable, V arg);

	    R Use<R>(IResultingBackgroundWorkerDelegate<R> runnable);

        R Use<R, V>(IResultingBackgroundWorkerParamDelegate<R, V> runnable, V arg);

        void ReintegrateForkedValues();
    }
}