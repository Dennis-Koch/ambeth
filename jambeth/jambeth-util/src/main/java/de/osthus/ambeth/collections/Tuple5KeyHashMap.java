package de.osthus.ambeth.collections;

/**
 * 66 percent faster compared to a normal HashMap with a Tuple2 (Composite-)Key as the Map-Key. This is due to the fact that there is no need to instantiate
 * Tuple2 Keys for put() or get() operations. Of course the overall memory footprint is also the half compared to a normal map: There is only the entry object
 * with 2 key-fields compared to the entry object compared to 1 key-field which contains a Tuple2 Key instance
 * 
 * @param <Key1>
 * @param <Key2>
 * @param <V>
 */
public class Tuple5KeyHashMap<Key1, Key2, Key3, Key4, Key5, V> extends AbstractTuple5KeyHashMap<Key1, Key2, Key3, Key4, Key5, V>
{
	public static <Key1, Key2, Key3, Key4, Key5, V> Tuple5KeyHashMap<Key1, Key2, Key3, Key4, Key5, V> create(int size)
	{
		return create(size, DEFAULT_LOAD_FACTOR);
	}

	public static <Key1, Key2, Key3, Key4, Key5, V> Tuple5KeyHashMap<Key1, Key2, Key3, Key4, Key5, V> create(int size, float loadFactor)
	{
		return new Tuple5KeyHashMap<Key1, Key2, Key3, Key4, Key5, V>((int) (size / loadFactor) + 1, loadFactor);
	}

	protected int size;

	public Tuple5KeyHashMap()
	{
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	public Tuple5KeyHashMap(float loadFactor)
	{
		this(DEFAULT_INITIAL_CAPACITY, loadFactor);
	}

	public Tuple5KeyHashMap(int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	public Tuple5KeyHashMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	@Override
	protected Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V> createEntry(int hash, Key1 key1, Key2 key2, Key3 key3, Key4 key4, Key5 key5, V value,
			final Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V> nextEntry)
	{
		return new Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V>(key1, key2, key3, key4, key5, value, hash, nextEntry);
	}

	@Override
	protected void entryAdded(final Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V> entry)
	{
		size++;
	}

	@Override
	protected void entryRemoved(final Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V> entry)
	{
		size--;
	}

	@Override
	public int size()
	{
		return size;
	}
}
