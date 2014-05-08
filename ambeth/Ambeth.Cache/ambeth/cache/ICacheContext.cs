namespace De.Osthus.Ambeth.Cache
{
    public interface ICacheContext
    {
        R ExecuteWithCache<R>(ISingleCacheRunnable<R> runnable);

        R ExecuteWithCache<R>(ICacheProvider cacheProvider, ISingleCacheRunnable<R> runnable);

        R ExecuteWithCache<R>(ICache cache, ISingleCacheRunnable<R> runnable);

	    R ExecuteWithCache<R, T>(ISingleCacheParamRunnable<R, T> runnable, T state);

	    R ExecuteWithCache<R, T>(ICacheProvider cacheProvider, ISingleCacheParamRunnable<R, T> runnable, T state);

	    R ExecuteWithCache<R, T>(ICache cache, ISingleCacheParamRunnable<R, T> runnable, T state);
    }
}