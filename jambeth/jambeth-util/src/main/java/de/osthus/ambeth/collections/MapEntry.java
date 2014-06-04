package de.osthus.ambeth.collections;

import java.util.Map.Entry;

import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.StringBuilderUtil;

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
public class MapEntry<K, V> implements IMapEntry<K, V>, IPrintable
{
	protected final int hash;

	protected MapEntry<K, V> nextEntry;

	protected final K key;

	protected V value;

	public MapEntry(int hash, MapEntry<K, V> nextEntry, K key, V value)
	{
		super();
		this.hash = hash;
		this.nextEntry = nextEntry;
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
	public MapEntry<K, V> getNextEntry()
	{
		return nextEntry;
	}

	public void setNextEntry(MapEntry<K, V> nextEntry)
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