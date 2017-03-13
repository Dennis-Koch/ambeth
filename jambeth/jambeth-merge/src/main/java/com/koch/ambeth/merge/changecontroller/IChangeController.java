package com.koch.ambeth.merge.changecontroller;

import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public interface IChangeController
{

	<T> T runWithoutEDBL(IResultingBackgroundWorkerDelegate<T> runnable) throws Throwable;

}
