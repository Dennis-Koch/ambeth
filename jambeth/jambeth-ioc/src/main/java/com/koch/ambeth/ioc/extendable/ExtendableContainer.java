package com.koch.ambeth.ioc.extendable;

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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.IdentityLinkedSet;

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
