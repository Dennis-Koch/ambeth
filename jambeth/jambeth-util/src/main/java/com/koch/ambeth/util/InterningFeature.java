package com.koch.ambeth.util;

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

import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.util.collections.WeakHashSet;

public class InterningFeature extends WeakHashSet<Object> implements IInterningFeature
{
	protected final ReentrantLock writeLock = new ReentrantLock();

	public InterningFeature()
	{
		super(0.5f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T intern(T value)
	{
		if (value == null)
		{
			return null;
		}
		ReentrantLock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			Object internedValue = get(value);
			if (internedValue == null)
			{
				internedValue = value;
				add(internedValue);
			}
			return (T) internedValue;
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
