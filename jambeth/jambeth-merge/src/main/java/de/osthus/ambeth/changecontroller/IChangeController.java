package de.osthus.ambeth.changecontroller;

import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public interface IChangeController
{

	<T> T runWithoutEDBL(IResultingBackgroundWorkerDelegate<T> runnable) throws Throwable;

}
