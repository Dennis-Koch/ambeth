package com.koch.ambeth.security.threading;

import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public interface IBackgroundAuthenticatingExecutorService
{
	void startBackgroundWorkerWithAuthentication(IBackgroundWorkerDelegate runnable);

	<T> T startBackgroundWorkerWithAuthentication(IResultingBackgroundWorkerDelegate<T> runnable);
}