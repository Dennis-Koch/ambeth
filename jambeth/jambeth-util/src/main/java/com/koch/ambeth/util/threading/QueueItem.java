package com.koch.ambeth.util.threading;

/*-
 * #%L
 * jambeth-util
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

import java.util.concurrent.CountDownLatch;

import com.koch.ambeth.util.collections.ListElem;

public class QueueItem extends ListElem<QueueItem>
{
	private final Object context;

	private final Object object;

	private final HandlerRunnable<?, ?> handler;

	private final CountDownLatch latch;

	private final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

	public QueueItem(Object context, Object object, HandlerRunnable<?, ?> handler, CountDownLatch latch)
	{
		super.value = this;
		this.context = context;
		this.object = object;
		this.handler = handler;
		this.latch = latch;
	}

	public Object getContext()
	{
		return context;
	}

	public Object getObject()
	{
		return object;
	}

	public ListElem<QueueItem> getThreadingLE()
	{
		return this;
	}

	public HandlerRunnable<?, ?> getHandler()
	{
		return handler;
	}

	public CountDownLatch getLatch()
	{
		return latch;
	}

	public ClassLoader getContextClassLoader()
	{
		return contextClassLoader;
	}
}
