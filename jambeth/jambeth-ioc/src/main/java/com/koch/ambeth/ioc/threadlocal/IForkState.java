package com.koch.ambeth.ioc.threadlocal;

import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;

public interface IForkState
{
	void use(Runnable runnable);

	void use(IBackgroundWorkerDelegate runnable);

	<V> void use(IBackgroundWorkerParamDelegate<V> runnable, V arg);

	<R> R use(IResultingBackgroundWorkerDelegate<R> runnable);

	<R, V> R use(IResultingBackgroundWorkerParamDelegate<R, V> runnable, V arg);

	void reintegrateForkedValues();
}
