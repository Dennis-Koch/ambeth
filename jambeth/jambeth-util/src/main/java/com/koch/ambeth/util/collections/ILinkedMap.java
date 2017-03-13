package com.koch.ambeth.util.collections;

import java.util.Iterator;
import java.util.Map.Entry;

public interface ILinkedMap<K, V> extends IMap<K, V>, Iterable<Entry<K, V>>
{
	Iterator<Entry<K, V>> iterator(boolean removeAllowed);
}