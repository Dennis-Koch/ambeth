using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Cache
{
    public interface ICacheContext
    {
        R ExecuteWithCache<R>(IResultingBackgroundWorkerDelegate<R> runnable);

        R ExecuteWithCache<R>(ICacheProvider cacheProvider, IResultingBackgroundWorkerDelegate<R> runnable);

        R ExecuteWithCache<R>(ICache cache, IResultingBackgroundWorkerDelegate<R> runnable);

	    R ExecuteWithCache<R, T>(IResultingBackgroundWorkerParamDelegate<R, T> runnable, T state);

        R ExecuteWithCache<R, T>(ICacheProvider cacheProvider, IResultingBackgroundWorkerParamDelegate<R, T> runnable, T state);

        R ExecuteWithCache<R, T>(ICache cache, IResultingBackgroundWorkerParamDelegate<R, T> runnable, T state);
    }
}