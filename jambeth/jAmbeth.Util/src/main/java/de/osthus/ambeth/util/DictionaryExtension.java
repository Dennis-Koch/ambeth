package de.osthus.ambeth.util;

import java.util.Map;
import java.util.Map.Entry;

import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public final class DictionaryExtension
{
	@SuppressWarnings("unchecked")
	public static <TKey, TValue> TValue getValueOrPutNew(Map<TKey, TValue> dictionary, TKey key, Class<?> valueType)
	{
		TValue value = dictionary.get(key);
		if (value == null)
		{
			try
			{
				value = (TValue) valueType.newInstance();
			}
			catch (Exception e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			dictionary.put(key, value);
		}
		return value;
	}

	public static <TKey, TValue> void loopModifiable(Map<TKey, TValue> dictionary, KeyValueCallback<TKey, TValue> keyValueCallback)
	{
		if (dictionary == null)
		{
			return;
		}
		LinkedHashMap<TKey, TValue> cloneDict = LinkedHashMap.create(dictionary.size());
		cloneDict.putAll(dictionary);
		DictionaryExtension.loop(cloneDict, keyValueCallback);
	}

	public static <TKey, TValue> void loop(Iterable<Entry<TKey, TValue>> map, KeyValueCallback<TKey, TValue> keyValueCallback)
	{
		if (map == null)
		{
			return;
		}
		try
		{
			for (Entry<TKey, TValue> entry : map)
			{
				keyValueCallback.invoke(entry.getKey(), entry.getValue());
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	private DictionaryExtension()
	{
		// Intended blank
	}
}
