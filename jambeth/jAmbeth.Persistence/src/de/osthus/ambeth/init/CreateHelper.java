package de.osthus.ambeth.init;

import java.util.ArrayList;
import java.util.Collection;

import de.osthus.ambeth.threading.SensitiveThreadLocal;

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
