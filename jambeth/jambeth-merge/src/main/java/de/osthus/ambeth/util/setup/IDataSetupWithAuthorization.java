package de.osthus.ambeth.util.setup;

import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public interface IDataSetupWithAuthorization
{
	<T> T executeWithAuthorization(IResultingBackgroundWorkerDelegate<T> runnable) throws Throwable;
}
