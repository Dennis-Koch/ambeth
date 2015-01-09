package de.osthus.ambeth.util;

import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.ioc.threadlocal.IForkState;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;

public class ResultingParallelRunnable<R, V> extends AbstractParallelRunnable<V>
{
	public static class Invocation<R, V> implements IBackgroundWorkerParamDelegate<V>
	{
		protected final IResultingBackgroundWorkerParamDelegate<R, V> run;

		public Invocation(ResultingRunnableHandle<R, V> runnableHandle)
		{
			run = runnableHandle.run;
		}

		@Override
		public void invoke(V item) throws Throwable
		{
			run.invoke(item);
		}
	}

	public static class InvocationWithAggregate<R, V> extends Invocation<R, V>
	{
		private final IAggregrateResultHandler<R, V> aggregrateResultHandler;
		private final Lock parallelLock;

		public InvocationWithAggregate(ResultingRunnableHandle<R, V> runnableHandle)
		{
			super(runnableHandle);
			aggregrateResultHandler = runnableHandle.aggregrateResultHandler;
			parallelLock = runnableHandle.parallelLock;
		}

		@Override
		public void invoke(V item) throws Throwable
		{
			R result = run.invoke(item);
			Lock parallelLock = this.parallelLock;
			parallelLock.lock();
			try
			{
				aggregrateResultHandler.aggregateResult(result, item);
			}
			finally
			{
				parallelLock.unlock();
			}
		}
	}

	private final Invocation<R, V> run;

	private final IForkState forkState;

	public ResultingParallelRunnable(ResultingRunnableHandle<R, V> runnableHandle, boolean buildThreadLocals)
	{
		super(runnableHandle, buildThreadLocals);
		forkState = runnableHandle.forkState;
		if (runnableHandle.aggregrateResultHandler != null)
		{
			run = new InvocationWithAggregate<R, V>(runnableHandle);
		}
		else
		{
			run = new Invocation<R, V>(runnableHandle);
		}
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