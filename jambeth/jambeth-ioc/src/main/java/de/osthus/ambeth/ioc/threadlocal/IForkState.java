package de.osthus.ambeth.ioc.threadlocal;

import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;

public interface IForkState
{
	void use(Runnable runnable);

	void use(IBackgroundWorkerDelegate runnable);

	<V> void use(IBackgroundWorkerParamDelegate<V> runnable, V arg);

	<R> R use(IResultingBackgroundWorkerDelegate<R> runnable);

	<R, V> R use(IResultingBackgroundWorkerParamDelegate<R, V> runnable, V arg);
}
