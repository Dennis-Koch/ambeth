package de.osthus.ambeth.collections;

public class Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>
{
	private final Key1 key1;

	private final Key2 key2;

	private final Key3 key3;

	private final Key4 key4;

	private Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> nextEntry;

	private final int hash;

	private V value;

	public Tuple4KeyEntry(Key1 key1, Key2 key2, Key3 key3, Key4 key4, V value, int hash, Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> nextEntry)
	{
		this.key1 = key1;
		this.key2 = key2;
		this.key3 = key3;
		this.key4 = key4;
		this.value = value;
		this.hash = hash;
		this.nextEntry = nextEntry;
	}

	public int getHash()
	{
		return hash;
	}

	public Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> getNextEntry()
	{
		return nextEntry;
	}

	public void setNextEntry(Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> nextEntry)
	{
		this.nextEntry = nextEntry;
	}

	public Key1 getKey1()
	{
		return key1;
	}

	public Key2 getKey2()
	{
		return key2;
	}

	public Key3 getKey3()
	{
		return key3;
	}

	public Key4 getKey4()
	{
		return key4;
	}

	public void setValue(V value)
	{
		this.value = value;
	}

	public V getValue()
	{
		return value;
	}

	@Override
	public String toString()
	{
		return "(" + getKey1() + ":" + getKey2() + ":" + getKey3() + ":" + getKey4() + "," + getValue();
	}
}
