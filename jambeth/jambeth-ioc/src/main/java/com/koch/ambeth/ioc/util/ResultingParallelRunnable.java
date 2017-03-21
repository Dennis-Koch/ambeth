package com.koch.ambeth.ioc.util;

/*-
 * #%L
 * jambeth-ioc
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.concurrent.locks.Lock;

import com.koch.ambeth.ioc.threadlocal.IForkState;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;

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
