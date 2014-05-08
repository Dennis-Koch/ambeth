package de.osthus.ambeth.collections;

public interface ISetEntry<K>
{
	int getHash();

	K getKey();

	ISetEntry<K> getNextEntry();
}
