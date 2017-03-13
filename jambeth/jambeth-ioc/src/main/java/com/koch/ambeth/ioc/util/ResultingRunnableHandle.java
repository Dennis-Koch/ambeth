package com.koch.ambeth.ioc.util;

import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;

public class ResultingRunnableHandle<R, V> extends AbstractRunnableHandle<V>
{
	public final IResultingBackgroundWorkerParamDelegate<R, V> run;

	public final IAggregrateResultHandler<R, V> aggregrateResultHandler;

	public ResultingRunnableHandle(IResultingBackgroundWorkerParamDelegate<R, V> run, IAggregrateResultHandler<R, V> aggregrateResultHandler,
			ArrayList<V> items, IThreadLocalCleanupController threadLocalCleanupController)
	{
		super(items, threadLocalCleanupController);
		this.run = run;
		this.aggregrateResultHandler = aggregrateResultHandler;
	}
}