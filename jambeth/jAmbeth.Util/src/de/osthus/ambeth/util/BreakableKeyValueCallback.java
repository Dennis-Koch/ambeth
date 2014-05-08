package de.osthus.ambeth.util;

public interface BreakableKeyValueCallback<TKey, TValue>
{

	boolean invoke(TKey key, TValue value) throws Throwable;

}
