using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Collections
{
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
    public class WeakSmartCopyMap<K, V> : WeakHashMap<K, V> where V : class
    {
        private readonly Object writeLock = new Object();

        public bool AutoCleanupNullValue { get; set; }

        public WeakSmartCopyMap()
            : base()
        {
            // intended blank
        }

        public WeakSmartCopyMap(float loadFactor)
            : base(loadFactor)
        {
            // intended blank
        }

        public WeakSmartCopyMap(int initialCapacity, float loadFactor)
            : base(initialCapacity, loadFactor)
        {
            // intended blank
        }

        public WeakSmartCopyMap(int initialCapacity)
            : base(initialCapacity)
        {
            // intended blank
        }

        public Object GetWriteLock()
        {
            return writeLock;
        }

        protected virtual TempHashMap<IMapEntry<K, V>, K, V> CreateEmptyInstance()
        {
            WeakSmartCopyMap<K, V> This = this;
            return new TempHashMap<IMapEntry<K, V>, K, V>(table.Length, this.loadFactor,
                delegate(int hash, K key, V value, IMapEntry<K, V> nextEntry)
                {
                    return This.CreateEntry(hash, key, value, nextEntry);
                },
                delegate(K key, IMapEntry<K, V> entry)
                {
                    return This.EqualKeys(key, entry);
                },
                delegate(K key)
                {
                    return This.ExtractHash(key);
                },
                delegate(IMapEntry<K, V> entry, IMapEntry<K, V> nextEntry)
                {
                    This.SetNextEntry(entry, nextEntry);
                },
                delegate(IMapEntry<K, V> entry, V value)
                {
                    return This.SetValueForEntry(entry, value);
                });
        }

        protected TempHashMap<IMapEntry<K, V>, K, V> CreateCopy()
        {
            // Copy existing data in FULLY NEW STRUCTURE
            IMapEntry<K, V>[] table = this.table;
            TempHashMap<IMapEntry<K, V>, K, V> backupMap = CreateEmptyInstance();
            if (AutoCleanupNullValue)
            {
                for (int a = table.Length; a-- > 0; )
                {
                    IMapEntry<K, V> entry = table[a];
                    while (entry != null)
                    {
                        K key = entry.Key;
                        if (key != null)
                        {
                            V value = entry.Value;
                            WeakReference valueAsRef = value as WeakReference;
                            if (valueAsRef.Target != null)
                            {
                                // Only copy the entry if the value content is still valid
                                backupMap.Put(CloneKey(key), CloneValue(value));
                            }
                        }
                        entry = entry.NextEntry;
                    }
                }
            }
            else
            {
                for (int a = table.Length; a-- > 0; )
                {
                    IMapEntry<K, V> entry = table[a];
                    while (entry != null)
                    {
                        K key = entry.Key;
                        if (key != null)
                        {
                            V value = entry.Value;
                            backupMap.Put(CloneKey(key), CloneValue(value));
                        }
                        entry = entry.NextEntry;
                    }
                }
            }
            return backupMap;
        }

        protected void SaveCopy(TempHashMap<IMapEntry<K, V>, K, V> copy)
        {
            // Now the structure contains all necessary data, so we "retarget" the existing table
            table = copy.GetTable();
            threshold = copy.GetThreshold();
            size = copy.Count;
        }

        protected virtual K CloneKey(K key)
        {
            return key;
        }

        protected virtual V CloneValue(V value)
        {
            return value;
        }

        public override void Clear()
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                if (Count == 0)
                {
                    return;
                }
                TempHashMap<IMapEntry<K, V>, K, V> backupMap = CreateCopy();
                // Write new data in the copied structure
                backupMap.Clear();
                SaveCopy(backupMap);
            }
        }

        public override V Put(K key, V value)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                TempHashMap<IMapEntry<K, V>, K, V> backupMap = CreateCopy();
                // Write new data in the copied structure
                V existingValue = backupMap.Put(key, value);
                SaveCopy(backupMap);
                return existingValue;
            }
        }

        public override void PutAll(IMap<K, V> map)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                TempHashMap<IMapEntry<K, V>, K, V> backupMap = CreateCopy();
                // Write new data in the copied structure
                backupMap.PutAll(map);
                SaveCopy(backupMap);
            }
        }

        public override bool PutIfNotExists(K key, V value)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                TempHashMap<IMapEntry<K, V>, K, V> backupMap = CreateCopy();
                // Write new data in the copied structure
                if (!backupMap.PutIfNotExists(key, value))
                {
                    return false;
                }
                SaveCopy(backupMap);
                return true;
            }
        }

        public override V Remove(K key)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                TempHashMap<IMapEntry<K, V>, K, V> backupMap = CreateCopy();
                // Write new data in the copied structure
                V existingValue = backupMap.Remove(key);
                SaveCopy(backupMap);
                return existingValue;
            }
        }

        public override bool RemoveIfValue(K key, V value)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                TempHashMap<IMapEntry<K, V>, K, V> backupMap = CreateCopy();
                // Write new data in the copied structure
                if (!backupMap.RemoveIfValue(key, value))
                {
                    return false;
                }
                SaveCopy(backupMap);
                return true;
            }
        }
    }
}