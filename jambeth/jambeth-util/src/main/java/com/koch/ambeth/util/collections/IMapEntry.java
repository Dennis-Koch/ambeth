package com.koch.ambeth.util.collections;

import java.util.Map.Entry;

/**
 * Wird von verschiedenen Map-Implementierungen als Entry f�r die Key-Value Mappings ben�tigt
 * 
 * @author kochd
 * 
 * @param <K>
 *            Der Typ des Keys
 * @param <V>
 *            Der Typ des Values
 */
public interface IMapEntry<K, V> extends Entry<K, V>
{
	int getHash();

	IMapEntry<K, V> getNextEntry();

	@Override
	V setValue(V value);

	// void setNextEntry(IMapEntry<K, V> nextEntry);
}
