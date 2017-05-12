using De.Osthus.Ambeth.Util;
using System;
using System.Collections;
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
    public class SmartCopySet<K> : CHashSet<K>
    {
        protected readonly Object writeLock = new Object();

        public SmartCopySet()
            : base(0.5f)
        {
            // Intended blank
        }

        public SmartCopySet(float loadFactor)
            : base(loadFactor)
        {
            // Intended blank
        }

        public SmartCopySet(int initialCapacity, float loadFactor)
            : base(initialCapacity, loadFactor)
        {
            // Intended blank
        }

        public SmartCopySet(int initialCapacity)
            : base(initialCapacity)
        {
            // Intended blank
        }

        public Object GetWriteLock()
        {
            return writeLock;
        }

        protected TempHashSet<K> CreateCopy()
        {
            // Copy existing data in FULLY NEW STRUCTURE
            SetEntry<K>[] table = this.table;
            TempHashSet<K> backupSet = new TempHashSet<K>(table.Length, this.loadFactor, delegate(K key, SetEntry<K> entry)
                {
                    return EqualKeys(key, entry);
                }, delegate(K key)
                {
                    return ExtractHash(key);
                });
            for (int a = table.Length; a-- > 0; )
            {
                SetEntry<K> entry = table[a];
                while (entry != null)
                {
                    K key = GetKeyOfEntry(entry);
                    if (key != null)
                    {
                        backupSet.Add(CloneKey(key));
                    }
                    entry = GetNextEntry(entry);
                }
            }
            return backupSet;
        }

        protected void SaveCopy(TempHashSet<K> copy)
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

        public override void Clear()
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                if (Count == 0)
                {
                    return;
                }
                TempHashSet<K> backupMap = CreateCopy();
                backupMap.Clear();
                SaveCopy(backupMap);
            }
        }

        public override bool AddAll<S>(IEnumerable<S> coll)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                TempHashSet<K> backupMap = CreateCopy();
                // Write new data in the copied structure
                if (!backupMap.AddAll(coll))
                {
                    return false;
                }
                SaveCopy(backupMap);
                return true;
            }
        }

        public override bool AddAll(IEnumerable coll)
    {
        Object writeLock = GetWriteLock();
        lock (writeLock)
        {
            TempHashSet<K> backupMap = CreateCopy();
            // Write new data in the copied structure
            if (!backupMap.AddAll(coll))
            {
                return false;
            }
            SaveCopy(backupMap);
            return true;
        }
    }

        public override bool AddAll<S>(S[] array)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                TempHashSet<K> backupMap = CreateCopy();
                // Write new data in the copied structure
                if (!backupMap.AddAll(array))
                {
                    return false;
                }
                SaveCopy(backupMap);
                return true;
            }
        }

        public override bool Add(K key)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                TempHashSet<K> backupMap = CreateCopy();
                // Write new data in the copied structure
                if (!backupMap.Add(key))
                {
                    return false;
                }
                SaveCopy(backupMap);
                return true;
            }
        }

        public override bool Remove(K key)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                TempHashSet<K> backupMap = CreateCopy();
                // Write new data in the copied structure
                if (!backupMap.Remove(key))
                {
                    return false;
                }
                SaveCopy(backupMap);
                return true;
            }
        }

        public override bool RetainAll(IICollection c)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                TempHashSet<K> backupMap = CreateCopy();
                // Write new data in the copied structure
                if (!backupMap.RetainAll(c))
                {
                    return false;
                }
                SaveCopy(backupMap);
                return true;
            }
        }

        public override bool RetainAll(IList c)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                TempHashSet<K> backupMap = CreateCopy();
                // Write new data in the copied structure
                if (!backupMap.RetainAll(c))
                {
                    return false;
                }
                SaveCopy(backupMap);
                return true;
            }
        }

        public override bool RemoveAll(IEnumerable c)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                TempHashSet<K> backupMap = CreateCopy();
                // Write new data in the copied structure
                if (!backupMap.RemoveAll(c))
                {
                    return false;
                }
                SaveCopy(backupMap);
                return true;
            }
        }

        public override K RemoveAndGet(K key)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                TempHashSet<K> backupMap = CreateCopy();
                // Write new data in the copied structure
                K existingKey = backupMap.RemoveAndGet(key);
                SaveCopy(backupMap);
                return existingKey;
            }
        }
    }
}