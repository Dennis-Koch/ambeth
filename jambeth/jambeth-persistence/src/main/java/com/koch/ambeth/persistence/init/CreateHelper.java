package com.koch.ambeth.persistence.init;

/*-
 * #%L
 * jambeth-persistence
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

import java.util.ArrayList;
import java.util.Collection;

import com.koch.ambeth.util.threading.SensitiveThreadLocal;

public final class CreateHelper
{
	private static final ThreadLocal<Collection<Object>> entityQueueLocal = new SensitiveThreadLocal<Collection<Object>>();

	public static void queueEntity(Object entity)
	{
		Collection<Object> entityQueue = CreateHelper.entityQueueLocal.get();
		if (entityQueue == null)
		{
			entityQueue = new ArrayList<Object>();
			CreateHelper.entityQueueLocal.set(entityQueue);
		}
		entityQueue.add(entity);
	}

	public static Collection<Object> getAndClearEntityQueue()
	{
		Collection<Object> entityQueue = CreateHelper.entityQueueLocal.get();
		if (entityQueue == null)
		{
			entityQueue = new ArrayList<Object>();
		}
		else
		{
			CreateHelper.entityQueueLocal.remove();
		}
		return entityQueue;
	}

	private CreateHelper()
	{
		// intended blank
	}
}
