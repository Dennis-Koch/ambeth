package de.osthus.ambeth.security.threading;

import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public interface IBackgroundAuthenticatingExecutorService
{
	void startBackgroundWorkerWithAuthentication(IBackgroundWorkerDelegate runnable);

	<T> T startBackgroundWorkerWithAuthentication(IResultingBackgroundWorkerDelegate<T> runnable);
}