package de.osthus.ambeth.ioc.extendable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.ioc.exception.ExtendableException;
import de.osthus.ambeth.util.ParamChecker;

public class MapExtendableContainer<K, V> extends SmartCopyMap<K, Object> implements IMapExtendableContainer<K, V>
{
	protected final boolean multiValue;

	protected final String message, keyMessage;

	public MapExtendableContainer(String message, String keyMessage)
	{
		this(message, keyMessage, false);
	}

	public MapExtendableContainer(String message, String keyMessage, boolean multiValue)
	{
		ParamChecker.assertParamNotNull(message, "message");
		ParamChecker.assertParamNotNull(keyMessage, "keyMessage");
		this.multiValue = multiValue;
		this.message = message;
		this.keyMessage = keyMessage;
	}

	@Override
	public void register(V extension, K key)
	{
		ParamChecker.assertParamNotNull(extension, message);
		ParamChecker.assertParamNotNull(key, keyMessage);
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			boolean putted = false;
			if (!multiValue)
			{
				putted = putIfNotExists(key, extension);
			}
			else
			{
				@SuppressWarnings("unchecked")
				ArrayList<V> values = (ArrayList<V>) get(key);
				if (values == null)
				{
					values = new ArrayList<V>(1);
				}
				else
				{
					values = new ArrayList<V>(values);
				}
				if (!values.contains(extension))
				{
					values.add(extension);
					putted = true;
					put(key, values);
				}
			}
			if (!putted)
			{
				throw new ExtendableException("Key '" + keyMessage + "' already added: " + key);
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void unregister(V extension, K key)
	{
		ParamChecker.assertParamNotNull(extension, message);
		ParamChecker.assertParamNotNull(key, keyMessage);
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			if (!multiValue)
			{
				ParamChecker.assertTrue(removeIfValue(key, extension), message);
			}
			else
			{
				@SuppressWarnings("unchecked")
				ArrayList<V> values = (ArrayList<V>) get(key);
				values = new ArrayList<V>(values);
				ParamChecker.assertNotNull(values, message);
				ParamChecker.assertTrue(values.remove(extension), message);
				if (values.isEmpty())
				{
					remove(key);
				}
				else
				{
					put(key, values);
				}
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public IList<V> getExtensions(K key)
	{
		ParamChecker.assertParamNotNull(key, "key");
		Object item = get(key);
		if (item == null)
		{
			return EmptyList.getInstance();
		}
		if (!multiValue)
		{
			return new ArrayList<V>(new Object[] { item });
		}
		return new ArrayList<V>((ArrayList<V>) item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V getExtension(K key)
	{
		ParamChecker.assertParamNotNull(key, "key");
		Object item = get(key);
		if (item == null)
		{
			return null;
		}
		if (!multiValue)
		{
			return (V) item;
		}
		ArrayList<V> values = (ArrayList<V>) item;
		// unregister removes empty value lists -> at least one entry
		return values.get(0);
	}

	@Override
	public ILinkedMap<K, V> getExtensions()
	{
		LinkedHashMap<K, V> targetMap = LinkedHashMap.create(size());
		getExtensions(targetMap);
		return targetMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getExtensions(Map<K, V> targetMap)
	{
		if (!multiValue)
		{
			for (Entry<K, Object> entry : this)
			{
				targetMap.put(entry.getKey(), (V) entry.getValue());
			}
		}
		else
		{
			for (Entry<K, Object> entry : this)
			{
				// unregister removes empty value lists -> at least one entry
				targetMap.put(entry.getKey(), ((List<V>) entry.getValue()).get(0));
			}
		}
	}
}
