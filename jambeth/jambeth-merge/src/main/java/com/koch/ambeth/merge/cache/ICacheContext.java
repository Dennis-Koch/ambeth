package com.koch.ambeth.merge.cache;

import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;

public interface ICacheContext
{
	<R> R executeWithCache(IResultingBackgroundWorkerDelegate<R> runnable) throws Throwable;

	<R> R executeWithCache(ICacheProvider cacheProvider, IResultingBackgroundWorkerDelegate<R> runnable) throws Throwable;

	<R> R executeWithCache(ICache cache, IResultingBackgroundWorkerDelegate<R> runnable) throws Throwable;

	<R, T> R executeWithCache(IResultingBackgroundWorkerParamDelegate<R, T> runnable, T state) throws Throwable;

	<R, T> R executeWithCache(ICacheProvider cacheProvider, IResultingBackgroundWorkerParamDelegate<R, T> runnable, T state) throws Throwable;

	<R, T> R executeWithCache(ICache cache, IResultingBackgroundWorkerParamDelegate<R, T> runnable, T state) throws Throwable;

}
