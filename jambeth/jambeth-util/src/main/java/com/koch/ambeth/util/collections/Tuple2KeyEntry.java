package com.koch.ambeth.util.collections;

public class Tuple2KeyEntry<Key1, Key2, V>
{
	private final Key1 key1;

	private final Key2 key2;

	private Tuple2KeyEntry<Key1, Key2, V> nextEntry;

	private final int hash;

	private V value;

	public Tuple2KeyEntry(Key1 key1, Key2 key2, V value, int hash, Tuple2KeyEntry<Key1, Key2, V> nextEntry)
	{
		this.key1 = key1;
		this.key2 = key2;
		this.value = value;
		this.hash = hash;
		this.nextEntry = nextEntry;
	}

	public int getHash()
	{
		return hash;
	}

	public Tuple2KeyEntry<Key1, Key2, V> getNextEntry()
	{
		return nextEntry;
	}

	public void setNextEntry(Tuple2KeyEntry<Key1, Key2, V> nextEntry)
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
		return "(" + getKey1() + ":" + getKey2() + "," + getValue();
	}
}
