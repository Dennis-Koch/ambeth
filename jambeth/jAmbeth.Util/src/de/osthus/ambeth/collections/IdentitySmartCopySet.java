package de.osthus.ambeth.collections;

public class IdentitySmartCopySet<K> extends SmartCopySet<K>
{
	public IdentitySmartCopySet()
	{
		super();
	}

	public IdentitySmartCopySet(float loadFactor)
	{
		super(loadFactor);
	}

	public IdentitySmartCopySet(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	public IdentitySmartCopySet(int initialCapacity)
	{
		super(initialCapacity);
	}

	@Override
	protected int extractHash(Object key)
	{
		return System.identityHashCode(key);
	}

	@Override
	protected boolean equalKeys(K key, ISetEntry<K> entry)
	{
		return key == entry.getKey();
	}
}
