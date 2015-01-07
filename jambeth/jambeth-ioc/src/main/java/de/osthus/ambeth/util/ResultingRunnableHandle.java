package de.osthus.ambeth.util;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;

public class ResultingRunnableHandle<R, V> extends AbstractRunnableHandle<V>
{
	public final IResultingBackgroundWorkerParamDelegate<R, V> run;

	public final IAggregrateResultHandler<R, V> aggregrateResultHandler;

	public ResultingRunnableHandle(IResultingBackgroundWorkerParamDelegate<R, V> run, IAggregrateResultHandler<R, V> aggregrateResultHandler, IList<V> items,
			IThreadLocalCleanupController threadLocalCleanupController)
	{
		super(items, threadLocalCleanupController);
		this.run = run;
		this.aggregrateResultHandler = aggregrateResultHandler;
	}
}