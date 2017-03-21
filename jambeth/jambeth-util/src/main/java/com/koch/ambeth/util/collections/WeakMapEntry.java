package com.koch.ambeth.util.collections;

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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map.Entry;

import com.koch.ambeth.util.EqualsUtil;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringBuilderUtil;

/**
 * Wird von verschiedenen Map-Implementierungen als Entry f�r die Key-Value Mappings ben�tigt
 * 
 * @author kochd
 * 
 * @param <K>
 *            Der Typ des Keys
 * @param <V>
 *            Der Typ des Values
 */
public class WeakMapEntry<K, V> extends WeakReference<K> implements IMapEntry<K, V>, IPrintable
{
	protected final int hash;

	protected IMapEntry<K, V> nextEntry;

	protected V value;

	public WeakMapEntry(K key, V value, int hash, IMapEntry<K, V> nextEntry, ReferenceQueue<Object> referenceQueue)
	{
		super(key, referenceQueue);
		this.hash = hash;
		this.nextEntry = nextEntry;
		this.value = value;
	}

	@Override
	public K getKey()
	{
		return get();
	}

	@Override
	public V getValue()
	{
		return value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof Entry))
		{
			return false;
		}
		Entry<Object, Object> other = (Entry<Object, Object>) obj;
		return EqualsUtil.equals(getKey(), other.getKey()) && EqualsUtil.equals(getValue(), other.getValue());
	}

	@Override
	public int hashCode()
	{
		// Key is enough for hashcoding
		K key = getKey();
		if (key == null)
		{
			// Any prime number
			return 97;
		}
		return key.hashCode();
	}

	@Override
	public int getHash()
	{
		return hash;
	}

	@Override
	public IMapEntry<K, V> getNextEntry()
	{
		return nextEntry;
	}

	public void setNextEntry(IMapEntry<K, V> nextEntry)
	{
		this.nextEntry = nextEntry;
	}

	@Override
	public V setValue(V value)
	{
		V oldValue = this.value;
		this.value = value;
		return oldValue;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append('(');
		StringBuilderUtil.appendPrintable(sb, getKey());
		sb.append(',');
		StringBuilderUtil.appendPrintable(sb, getValue());
		sb.append(')');
	}
}
