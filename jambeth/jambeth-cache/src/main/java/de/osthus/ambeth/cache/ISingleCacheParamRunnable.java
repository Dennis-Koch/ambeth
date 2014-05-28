package de.osthus.ambeth.cache;

public interface ISingleCacheParamRunnable<R, T>
{
	R run(T state) throws Throwable;
}
