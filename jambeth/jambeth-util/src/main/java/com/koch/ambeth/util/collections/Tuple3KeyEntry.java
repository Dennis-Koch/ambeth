package com.koch.ambeth.util.collections;

public class Tuple3KeyEntry<Key1, Key2, Key3, V>
{
	private final Key1 key1;

	private final Key2 key2;

	private final Key3 key3;

	private Tuple3KeyEntry<Key1, Key2, Key3, V> nextEntry;

	private final int hash;

	private V value;

	public Tuple3KeyEntry(Key1 key1, Key2 key2, Key3 key3, V value, int hash, Tuple3KeyEntry<Key1, Key2, Key3, V> nextEntry)
	{
		this.key1 = key1;
		this.key2 = key2;
		this.key3 = key3;
		this.value = value;
		this.hash = hash;
		this.nextEntry = nextEntry;
	}

	public int getHash()
	{
		return hash;
	}

	public Tuple3KeyEntry<Key1, Key2, Key3, V> getNextEntry()
	{
		return nextEntry;
	}

	public void setNextEntry(Tuple3KeyEntry<Key1, Key2, Key3, V> nextEntry)
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
		return "(" + getKey1() + ":" + getKey2() + ":" + getKey3() + "," + getValue();
	}
}
