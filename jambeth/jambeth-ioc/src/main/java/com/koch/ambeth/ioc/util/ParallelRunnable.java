package com.koch.ambeth.ioc.util;

import com.koch.ambeth.ioc.threadlocal.IForkState;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public class ParallelRunnable<V> extends AbstractParallelRunnable<V>
{
	private final IForkState forkState;

	private final IBackgroundWorkerParamDelegate<V> run;

	public ParallelRunnable(RunnableHandle<V> runnableHandle, boolean buildThreadLocals)
	{
		super(runnableHandle, buildThreadLocals);
		forkState = runnableHandle.forkState;
		run = runnableHandle.run;
	}

	@Override
	protected void runIntern(V item) throws Throwable
	{
		if (buildThreadLocals)
		{
			forkState.use(run, item);
		}
		else
		{
			run.invoke(item);
		}
	}
}