package de.osthus.ambeth.util;

import de.osthus.ambeth.ioc.threadlocal.IForkState;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;

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