using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    /// <summary>
    /// This special kind of HashMap is intended to be used in high-performance concurrent scenarios with many reads and only some single occurences of write
    /// accesses. To allow extremely high concurrency there is NO lock in read access scenarios. The design pattern to synchronize the reads with the indeed
    /// synchronized write accesses the internal table-structure well be REPLACED on each write.
    /// 
    /// Because of this the existing internal object graph will NEVER be modified allowing unsynchronized read access of any amount without performance
    /// loss.
    /// </summary>
    /// <typeparam name="K"></typeparam>
    /// <typeparam name="V"></typeparam>   
    public class SmartCopyMap<K, V> : HashMap<K, V> where V : class
    {
        private readonly Object writeLock = new Object();

        public bool AutoCleanupNullValue { get; set; }

        public SmartCopyMap() : base()
        {
            // Intended blank
        }

        public SmartCopyMap(float loadFactor) : base(loadFactor)
        {
            // Intended blank
        }

        public SmartCopyMap(int initialCapacity, float loadFactor) : base(initialCapacity, loadFactor)
        {
            // Intended blank
        }

        public SmartCopyMap(int initialCapacity) : base(initialCapacity)
        {
            // Intended blank
        }

        public Object GetWriteLock()
        {
            return writeLock;
        }

        protected virtual TempHashMap<MapEntry<K, V>, K, V> CreateEmptyInstance()
        {
            SmartCopyMap<K, V> This = this;
            return new TempHashMap<MapEntry<K, V>, K, V>(table.Length, this.loadFactor,
                delegate(int hash, K key, V value, MapEntry<K, V> nextEntry)
                {
                    return This.CreateEntry(hash, key, value, nextEntry);
                },
                delegate(K key, MapEntry<K, V> entry)
                {
                    return This.EqualKeys(key, entry);
                },
                delegate(K key)
                {
                    return This.ExtractHash(key);
                },
                delegate(MapEntry<K, V> entry, MapEntry<K, V> nextEntry)
                {
                    This.SetNextEntry(entry, nextEntry);
                },
                delegate(MapEntry<K, V> entry, V value)
                {
                    return This.SetValueForEntry(entry, value);
                });            
        }

        protected TempHashMap<MapEntry<K, V>, K, V> CreateCopy()
        {
            SmartCopyMap<K, V> This = this;
            // Copy existing data in FULLY NEW STRUCTURE
            MapEntry<K, V>[] table = this.table;
            TempHashMap<MapEntry<K, V>, K, V> backupMap = CreateEmptyInstance();
            if (AutoCleanupNullValue)
            {
                for (int a = table.Length; a-- > 0;)
			    {
				    MapEntry<K, V> entry = table[a];
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
					    entry = entry.NextEntryReal;
				    }
			    }
            }
            else
            {
                for (int a = table.Length; a-- > 0; )
                {
                    MapEntry<K, V> entry = table[a];
                    while (entry != null)
                    {
                        K key = entry.Key;
                        if (key != null)
                        {
                            V value = entry.Value;
                            backupMap.Put(CloneKey(key), CloneValue(value));
                        }
                        entry = GetNextEntry(entry);
                    }
                }
            }
            return backupMap;
        }

        protected void SaveCopy(TempHashMap<MapEntry<K,V>, K, V> copy)
        {
            // Now the structure contains all necessary data, so we "retarget" the existing table
            table = copy.GetTable();
            threshold = copy.GetThreshold();
            tableLengthMinusOne = copy.GetTableLengthMinusOne();
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
                TempHashMap<MapEntry<K, V>, K, V> backupMap = CreateCopy();
                backupMap.Clear();
                SaveCopy(backupMap);
            }
        }

        public override V Put(K key, V value)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                TempHashMap<MapEntry<K, V>, K, V> backupMap = CreateCopy();
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
                TempHashMap<MapEntry<K, V>, K, V> backupMap = CreateCopy();
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
                TempHashMap<MapEntry<K, V>, K, V> backupMap = CreateCopy();
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
                TempHashMap<MapEntry<K, V>, K, V> backupMap = CreateCopy();
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
                TempHashMap<MapEntry<K, V>, K, V> backupMap = CreateCopy();
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