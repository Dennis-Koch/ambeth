package de.osthus.ambeth.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Interface for Ambeth map implementation. Overrides methods from java.util.Map to be comparable with C# interface.
 * 
 * @param <K>
 *            Key type
 * @param <V>
 *            Value type
 */
public interface IMap<K, V> extends Map<K, V>, Iterable<Entry<K, V>>
{
	@Override
	void clear();

	@Override
	boolean containsKey(Object key);

	@Override
	ISet<Entry<K, V>> entrySet();

	void entrySet(ISet<Entry<K, V>> targetEntrySet);

	@Override
	V get(Object key);

	@Override
	boolean isEmpty();

	@Override
	ISet<K> keySet();

	void keySet(Collection<K> targetKeySet);

	IList<K> keyList();

	@Override
	V put(K key, V value);

	@Override
	V remove(Object key);

	@Override
	IList<V> values();

	K getKey(K key);

	boolean putIfNotExists(K key, V value);

	boolean removeIfValue(K key, V value);

	V[] toArray(Class<V> arrayType);
}