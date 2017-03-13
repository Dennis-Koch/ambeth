package com.koch.ambeth.util.collections;

import java.util.Map.Entry;

import com.koch.ambeth.util.EqualsUtil;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringBuilderUtil;

public class MapLinkedEntry<K, V> extends AbstractListElem<MapLinkedEntry<K, V>> implements IMapEntry<K, V>, IPrintable
{
	protected final int hash;

	protected MapLinkedEntry<K, V> nextEntry;

	protected final K key;

	protected V value;

	public MapLinkedEntry()
	{
		// For GenericFastList
		hash = 0;
		key = null;
	}

	public MapLinkedEntry(int hash, K key, V value)
	{
		this.hash = hash;
		this.key = key;
		this.value = value;
	}

	@Override
	public K getKey()
	{
		return key;
	}

	@Override
	public V getValue()
	{
		return value;
	}

	@Override
	public int getHash()
	{
		return hash;
	}

	@Override
	public MapLinkedEntry<K, V> getNextEntry()
	{
		return nextEntry;
	}

	public void setNextEntry(final MapLinkedEntry<K, V> nextEntry)
	{
		this.nextEntry = nextEntry;
	}

	@Override
	public V setValue(final V value)
	{
		V oldValue = this.value;
		this.value = value;
		return oldValue;
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
