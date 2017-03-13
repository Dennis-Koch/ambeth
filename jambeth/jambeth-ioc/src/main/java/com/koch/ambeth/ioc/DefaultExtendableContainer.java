package com.koch.ambeth.ioc;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.concurrent.locks.Lock;

import com.koch.ambeth.ioc.extendable.IExtendableContainer;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.IdentitySmartCopySet;

public class DefaultExtendableContainer<V> extends IdentitySmartCopySet<V> implements IExtendableContainer<V>
{
	protected final String message;

	protected final Class<V> type;

	protected final V[] emptyArray;

	@SuppressWarnings("unchecked")
	public DefaultExtendableContainer(Class<V> type, String message)
	{
		this.type = type;
		this.message = message;
		emptyArray = (V[]) Array.newInstance(type, 0);
	}

	@Override
	public void register(V listener)
	{
		ParamChecker.assertParamNotNull(listener, message);
		Lock lock = getWriteLock();
		lock.lock();
		try
		{
			boolean add = add(listener);
			ParamChecker.assertTrue(add, message);
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
		Lock lock = getWriteLock();
		lock.lock();
		try
		{
			ParamChecker.assertTrue(remove(listener), message);
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
		int size = size();
		if (size == 0)
		{
			return emptyArray;
		}
		V[] array = (V[]) Array.newInstance(type, size);
		toArray(array);
		return array;
	}

	@Override
	public void getExtensions(Collection<V> targetExtensionList)
	{
		toList(targetExtensionList);
	}
}