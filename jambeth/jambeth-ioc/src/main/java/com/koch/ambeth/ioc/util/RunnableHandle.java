package com.koch.ambeth.ioc.util;

import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public class RunnableHandle<V> extends AbstractRunnableHandle<V>
{
	public final IBackgroundWorkerParamDelegate<V> run;

	public RunnableHandle(IBackgroundWorkerParamDelegate<V> run, ArrayList<V> items, IThreadLocalCleanupController threadLocalCleanupController)
	{
		super(items, threadLocalCleanupController);
		this.run = run;
	}
}