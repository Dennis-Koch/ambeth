package de.osthus.ambeth.cache;

public interface ICacheContext
{
	<R> R executeWithCache(ISingleCacheRunnable<R> runnable) throws Throwable;

	<R> R executeWithCache(ICacheProvider cacheProvider, ISingleCacheRunnable<R> runnable) throws Throwable;

	<R> R executeWithCache(ICache cache, ISingleCacheRunnable<R> runnable) throws Throwable;

	<R, T> R executeWithCache(ISingleCacheParamRunnable<R, T> runnable, T state) throws Throwable;

	<R, T> R executeWithCache(ICacheProvider cacheProvider, ISingleCacheParamRunnable<R, T> runnable, T state) throws Throwable;

	<R, T> R executeWithCache(ICache cache, ISingleCacheParamRunnable<R, T> runnable, T state) throws Throwable;

}
