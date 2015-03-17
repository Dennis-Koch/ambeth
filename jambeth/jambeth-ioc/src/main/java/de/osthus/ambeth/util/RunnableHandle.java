package de.osthus.ambeth.util;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;

public class RunnableHandle<V> extends AbstractRunnableHandle<V>
{
	public final IBackgroundWorkerParamDelegate<V> run;

	public RunnableHandle(IBackgroundWorkerParamDelegate<V> run, ArrayList<V> items, IThreadLocalCleanupController threadLocalCleanupController)
	{
		super(items, threadLocalCleanupController);
		this.run = run;
	}
}