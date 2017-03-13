package com.koch.ambeth.security.threading;

import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

/**
 * This execution is used in order to activate all proxies (especially the SecurityProxy)
 */
public interface IBackgroundAuthenticatingExecution
{
	void execute(IBackgroundWorkerDelegate runnable) throws Throwable;

	<T> T execute(IResultingBackgroundWorkerDelegate<T> runnable) throws Throwable;
}