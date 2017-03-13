package com.koch.ambeth.ioc.jaxb;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.WeakHashMap;

public class JAXBContextProvider implements IJAXBContextProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final WeakHashMap<Class<?>[], Reference<JAXBContext>> queuedContextsMap = new WeakHashMap<Class<?>[], Reference<JAXBContext>>()
	{
		@Override
		protected boolean equalKeys(java.lang.Class<?>[] key, com.koch.ambeth.util.collections.IMapEntry<java.lang.Class<?>[], Reference<JAXBContext>> entry)
		{
			return Arrays.equals(key, entry.getKey());
		}

		@Override
		protected int extractHash(java.lang.Class<?>[] key)
		{
			return Arrays.hashCode(key);
		}
	};

	protected final Lock writeLock = new ReentrantLock();

	protected JAXBContext getExistingContext(Class<?>[] classesToBeBound)
	{
		Reference<JAXBContext> queuedContextsR = queuedContextsMap.get(classesToBeBound);
		if (queuedContextsR == null)
		{
			return null;
		}
		return queuedContextsR.get();
	}

	@Override
	public JAXBContext acquireSharedContext(Class<?>... classesToBeBound) throws JAXBException
	{
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			JAXBContext context = getExistingContext(classesToBeBound);
			if (context != null)
			{
				return context;
			}
		}
		finally
		{
			writeLock.unlock();
		}
		JAXBContext context = JAXBContext.newInstance(classesToBeBound);

		writeLock.lock();
		try
		{
			JAXBContext existingContext = getExistingContext(classesToBeBound);
			if (existingContext != null)
			{
				return existingContext;
			}
			queuedContextsMap.put(classesToBeBound, new WeakReference<JAXBContext>(context));
			return context;
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
