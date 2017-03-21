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
