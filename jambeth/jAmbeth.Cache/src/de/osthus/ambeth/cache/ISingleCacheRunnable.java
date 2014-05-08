package de.osthus.ambeth.cache;

public interface ISingleCacheRunnable<R>
{
	R run() throws Throwable;
}
