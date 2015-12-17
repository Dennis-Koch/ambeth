package de.osthus.ambeth.security.threading;

import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

/**
 * This execution is used in order to activate all proxies (especially the SecurityProxy)
 */
public interface IBackgroundAuthenticatingExecution
{
	void execute(IBackgroundWorkerDelegate runnable) throws Throwable;

	<T> T execute(IResultingBackgroundWorkerDelegate<T> runnable) throws Throwable;
}