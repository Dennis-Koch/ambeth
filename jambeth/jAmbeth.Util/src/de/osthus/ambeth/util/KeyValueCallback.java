package de.osthus.ambeth.util;

public interface KeyValueCallback<TKey, TValue>
{
	void invoke(TKey key, TValue value) throws Throwable;
}
