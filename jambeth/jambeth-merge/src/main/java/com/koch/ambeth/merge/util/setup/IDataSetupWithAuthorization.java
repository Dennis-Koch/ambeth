package com.koch.ambeth.merge.util.setup;

import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public interface IDataSetupWithAuthorization
{
	<T> T executeWithAuthorization(IResultingBackgroundWorkerDelegate<T> runnable) throws Throwable;
}
