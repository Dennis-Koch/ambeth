package de.osthus.ambeth.collections;

import java.lang.ref.Reference;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This special kind of HashMap is intended to be used in high-performance concurrent scenarios with many reads and only some single occurences of write
 * accesses. To allow extremely high concurrency there is NO lock in read access scenarios. The design pattern to synchronize the reads with the indeed
 * synchronized write accesses the internal table-structure well be REPLACED on each write.
 * 
 * Because of this the existing internal object graph will NEVER be modified allowing unsynchronized read access of any amount without performance loss.
 * 
 * @param <K>
 * @param <V>
 */
public class WeakSmartCopyMap<K, V> extends WeakHashMap<K, V>
{
	private final Lock writeLock = new ReentrantLock();

	private boolean autoCleanupReference;

	public WeakSmartCopyMap()
	{
		super(0.5f);
	}

	public WeakSmartCopyMap(float loadFactor)
	{
		super(loadFactor);
	}

	public WeakSmartCopyMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	public WeakSmartCopyMap(int initialCapacity)
	{
		super(initialCapacity, 0.5f);
	}

	public void setAutoCleanupReference(boolean autoCleanupReference)
	{
		this.autoCleanupReference = autoCleanupReference;
	}

	public Lock getWriteLock()
	{
		return writeLock;
	}

	protected WeakHashMap<K, V> createCopy()
	{
		final WeakSmartCopyMap<K, V> This = this;
		// Copy existing data in FULLY NEW STRUCTURE
		IMapEntry<K, V>[] table = this.table;
		WeakHashMap<K, V> backupMap = new WeakHashMap<K, V>(table.length, this.loadFactor)
		{
			@Override
			protected boolean equalKeys(K key, IMapEntry<K, V> entry)
			{
				return This.equalKeys(key, entry);
			}

			@Override
			protected int extractHash(K key)
			{
				return This.extractHash(key);
			}
		};
		if (autoCleanupReference)
		{
			for (int a = table.length; a-- > 0;)
			{
				IMapEntry<K, V> entry = table[a];
				while (entry != null)
				{
					K key = entry.getKey();
					if (key != null)
					{
						V value = entry.getValue();
						Reference<?> valueAsRef = (Reference<?>) value;
						if (valueAsRef.get() != null)
						{
							// Only copy the entry if the value content is still valid
							backupMap.put(cloneKey(key), cloneValue(value));
						}
					}
					entry = entry.getNextEntry();
				}
			}
		}
		else
		{
			for (int a = table.length; a-- > 0;)
			{
				IMapEntry<K, V> entry = table[a];
				while (entry != null)
				{
					K key = entry.getKey();
					if (key != null)
					{
						V value = entry.getValue();
						backupMap.put(cloneKey(key), cloneValue(value));
					}
					entry = entry.getNextEntry();
				}
			}
		}
		return backupMap;
	}

	protected void saveCopy(WeakHashMap<K, V> copy)
	{
		// Now the structure contains all necessary data, so we "retarget" the existing table
		table = copy.table;
		threshold = copy.threshold;
		size = copy.size;
	}

	protected K cloneKey(K key)
	{
		return key;
	}

	protected V cloneValue(V value)
	{
		return value;
	}

	@Override
	public void clear()
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			if (size() == 0)
			{
				return;
			}
			WeakHashMap<K, V> backupMap = createCopy();
			backupMap.clear();
			saveCopy(backupMap);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public V put(K key, V value)
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			WeakHashMap<K, V> backupMap = createCopy();
			// Write new data in the copied structure
			V existingValue = backupMap.put(key, value);
			saveCopy(backupMap);
			return existingValue;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map)
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			WeakHashMap<K, V> backupMap = createCopy();
			// Write new data in the copied structure
			backupMap.putAll(map);
			saveCopy(backupMap);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public boolean putIfNotExists(K key, V value)
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			WeakHashMap<K, V> backupMap = createCopy();
			// Write new data in the copied structure
			if (!backupMap.putIfNotExists(key, value))
			{
				return false;
			}
			saveCopy(backupMap);
			return true;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public V remove(Object key)
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			WeakHashMap<K, V> backupMap = createCopy();
			// Write new data in the copied structure
			V existingValue = backupMap.remove(key);
			saveCopy(backupMap);
			return existingValue;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public boolean removeIfValue(K key, V value)
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			WeakHashMap<K, V> backupMap = createCopy();
			// Write new data in the copied structure
			if (!backupMap.removeIfValue(key, value))
			{
				return false;
			}
			saveCopy(backupMap);
			return true;
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
