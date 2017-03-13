package com.koch.ambeth.util.collections;

public interface ISetEntry<K>
{
	int getHash();

	K getKey();

	ISetEntry<K> getNextEntry();
}
