package de.osthus.ambeth.ioc.extendable;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.IdentityLinkedSet;
import de.osthus.ambeth.util.ParamChecker;

public class ExtendableContainer<V> implements IExtendableContainer<V>
{
	protected final String message;

	protected final IdentityLinkedSet<V> set;

	protected final Class<V> type;

	protected final V[] emptyArray;

	protected final Lock lock = new ReentrantLock();

	@SuppressWarnings("unchecked")
	public ExtendableContainer(Class<V> type, String message)
	{
		ParamChecker.assertParamNotNull(type, "type");
		ParamChecker.assertParamNotNull(message, "message");
		this.type = type;
		this.message = message;

		set = new IdentityLinkedSet<V>();
		emptyArray = (V[]) Array.newInstance(type, 0);
	}

	@Override
	public void register(V listener)
	{
		ParamChecker.assertParamNotNull(listener, message);
		Lock lock = this.lock;
		lock.lock();
		try
		{
			ParamChecker.assertTrue(set.add(listener), message);
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	public void unregister(V listener)
	{
		ParamChecker.assertParamNotNull(listener, message);
		Lock lock = this.lock;
		lock.lock();
		try
		{
			ParamChecker.assertTrue(set.remove(listener), message);
		}
		finally
		{
			lock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public V[] getExtensions()
	{
		Lock lock = this.lock;
		lock.lock();
		try
		{
			int size = set.size();
			if (size == 0)
			{
				return emptyArray;
			}
			V[] array = (V[]) Array.newInstance(type, size);
			set.toArray(array);
			return array;
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	public void getExtensions(Collection<V> targetListenerList)
	{
		Lock lock = this.lock;
		lock.lock();
		try
		{
			for (V listener : set)
			{
				targetListenerList.add(listener);
			}
		}
		finally
		{
			lock.unlock();
		}
	}
}