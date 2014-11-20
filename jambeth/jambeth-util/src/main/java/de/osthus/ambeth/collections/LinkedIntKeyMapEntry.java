package de.osthus.ambeth.collections;

public class LinkedIntKeyMapEntry<V> extends AbstractListElem<LinkedIntKeyMapEntry<V>>
{
	protected int key, hash;

	protected LinkedIntKeyMapEntry<V> nextEntry;

	protected V value;

	public void initEntry(final int hash, final int key, final V value, LinkedIntKeyMapEntry<V> nextEntry)
	{
		this.value = value;
		this.nextEntry = nextEntry;
		this.key = key;
		this.hash = hash;
	}

	public V getValue()
	{
		return value;
	}

	public int getKey()
	{
		return key;
	}
}