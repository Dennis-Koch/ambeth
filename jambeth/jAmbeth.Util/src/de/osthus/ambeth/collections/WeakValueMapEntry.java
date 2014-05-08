package de.osthus.ambeth.collections;

import java.lang.ref.WeakReference;
import java.util.Map.Entry;

import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.StringBuilderUtil;

public class WeakValueMapEntry<K, V> extends WeakReference<V> implements IMapEntry<K, V>, IPrintable
{
	protected final int hash;

	protected IMapEntry<K, V> nextEntry;

	protected final K key;

	public WeakValueMapEntry(int hash, IMapEntry<K, V> nextEntry, K key, V value)
	{
		super(value);
		this.hash = hash;
		this.nextEntry = nextEntry;
		this.key = key;
	}

	@Override
	public K getKey()
	{
		return key;
	}

	@Override
	public V getValue()
	{
		return get();
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
		throw new UnsupportedOperationException();
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
