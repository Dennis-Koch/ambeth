package de.osthus.ambeth.collections;

import java.util.Collection;
import java.util.Set;

/**
 * Spezialisierte Form der StHashSet mit dem Unterschied, dass hierbei die Eindeutigkeit von Entries nicht auf der Equal-Identit�t der jeweiligen Keys
 * basiert, sondern auf deren Objekt-Identit�t.
 * 
 * Man k�nnte somit als Beispiel, wenn auch praxisfern, 2 Keys vom Typ Integer mit dem Wert '1' in diese Set hinzuf�gen, solange die beiden Integer
 * verschiedene Integer-Instanzen darstellen.
 * 
 * @author kochd
 * 
 * @param <K>
 *            Der Typ der in der Set enthaltenen Keys
 */
public class IdentityHashSet<K> extends HashSet<K>
{
	public static <K> IdentityHashSet<K> create(int size)
	{
		return create(size, DEFAULT_LOAD_FACTOR);
	}

	public static <K> IdentityHashSet<K> create(int size, float loadFactor)
	{
		return new IdentityHashSet<K>((int) (size / loadFactor) + 1, loadFactor);
	}

	public IdentityHashSet()
	{
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	public IdentityHashSet(Collection<? extends K> sourceCollection)
	{
		this((int) (sourceCollection.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR);
		addAll(sourceCollection);
	}

	public IdentityHashSet(K[] sourceArray)
	{
		this((int) (sourceArray.length / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR);
		addAll(sourceArray);
	}

	public IdentityHashSet(float loadFactor)
	{
		this(DEFAULT_INITIAL_CAPACITY, loadFactor);
	}

	public IdentityHashSet(int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	public IdentityHashSet(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor, SetEntry.class);
	}

	public IdentityHashSet(Set<? extends K> map)
	{
		super((int) (map.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR);
		addAll(map);
	}

	@Override
	protected int extractHash(K key)
	{
		return System.identityHashCode(key);
	}

	@Override
	protected boolean equalKeys(K key, ISetEntry<K> entry)
	{
		return key == entry.getKey();
	}
}
