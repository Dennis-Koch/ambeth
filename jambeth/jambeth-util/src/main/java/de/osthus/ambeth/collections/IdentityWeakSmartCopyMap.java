package de.osthus.ambeth.collections;

/**
 * This special kind of HashMap is intended to be used in high-performance concurrent scenarios with many reads and only some single occurences of write
 * accesses. To allow extremely high concurrency there is NO lock in read access scenarios. The design pattern to synchronize the reads with the indeed
 * synchronized write accesses the internal table-structure well be REPLACED on each write.
 * 
 * Because of this the existing internal object graph will NEVER be modified allowing unsynchronized read access of any amount without performance loss.
 * 
 * @param <K>
 * @param <V>
 */
public class IdentityWeakSmartCopyMap<K, V> extends WeakSmartCopyMap<K, V>
{
	public IdentityWeakSmartCopyMap()
	{
		super();
	}

	public IdentityWeakSmartCopyMap(float loadFactor)
	{
		super(loadFactor);
	}

	public IdentityWeakSmartCopyMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	public IdentityWeakSmartCopyMap(int initialCapacity)
	{
		super(initialCapacity, 0.5f);
	}

	@Override
	protected boolean equalKeys(K key, IMapEntry<K, V> entry)
	{
		return key == entry.getKey();
	}

	@Override
	protected int extractHash(K key)
	{
		return System.identityHashCode(key);
	}
}
