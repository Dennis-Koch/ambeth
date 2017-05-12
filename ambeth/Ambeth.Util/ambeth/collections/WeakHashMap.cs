using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    public class WeakHashMap<K, V> : AbstractHashMap<IMapEntry<K, V>, K, V>
    {
        public static WeakHashMap<K, V> Create(int size)
        {
            return Create(size, DEFAULT_LOAD_FACTOR);
        }

        public static WeakHashMap<K, V> Create(int size, float loadFactor)
        {
            return new WeakHashMap<K, V>((int)(size / loadFactor) + 1, loadFactor);
        }

        protected int size;

        public WeakHashMap()
            : this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, typeof(WeakMapEntry<K, V>))
        {
            // intended blank
        }

        public WeakHashMap(float loadFactor)
            : this(DEFAULT_INITIAL_CAPACITY, loadFactor, typeof(WeakMapEntry<K, V>))
        {
            // intended blank
        }

        public WeakHashMap(int initialCapacity)
            : this(initialCapacity, DEFAULT_LOAD_FACTOR, typeof(WeakMapEntry<K, V>))
        {
            // intended blank
        }

        public WeakHashMap(int initialCapacity, float loadFactor)
            : this(initialCapacity, loadFactor, typeof(WeakMapEntry<K, V>))
        {
            // intended blank
        }

        public WeakHashMap(int initialCapacity, float loadFactor, Type entryClass)
            : base(initialCapacity, loadFactor)
        {
            // intended blank
        }

        public WeakReference GetWeakReferenceEntry(K key)
        {
            int hash = Hash(ExtractHash(key));
            IMapEntry<K, V>[] table = this.table;
            int i = hash & (table.Length - 1);
            IMapEntry<K, V> entry = table[i];
            while (entry != null)
            {
                if (EqualKeys(key, entry))
                {
                    return (WeakReference)entry;
                }
                entry = entry.NextEntry;
            }
            return null;
        }

        protected override IMapEntry<K, V> CreateEntry(int hash, K key, V value, IMapEntry<K, V> nextEntry)
        {
            return new WeakMapEntry<K, V>(key, value, hash, nextEntry);
        }

        protected override void EntryAdded(IMapEntry<K, V> entry)
        {
            size++;
        }

        protected override void EntryRemoved(IMapEntry<K, V> entry)
        {
            size--;
        }

        protected override void SetNextEntry(IMapEntry<K, V> entry, IMapEntry<K, V> nextEntry)
        {
            ((WeakMapEntry<K, V>)entry).SetNextEntry(nextEntry);
        }

        protected override V SetValueForEntry(IMapEntry<K, V> entry, V value)
        {
            return ((WeakMapEntry<K, V>)entry).SetValue(value);
        }

        public override int Count
        {
            get
            {
                return size;
            }
        }

        protected override void Transfer(IMapEntry<K, V>[] newTable)
        {
            int newCapacityMinus1 = newTable.Length - 1;
            IMapEntry<K,V>[] table = this.table;

            for (int a = table.Length; a-- > 0; )
            {
                IMapEntry<K, V> entry = table[a], next;
                while (entry != null)
                {
                    next = GetNextEntry(entry);

                    // only handle this entry if it has still a valid key
                    if (entry.Key != null)
                    {
                        int i = entry.Hash & newCapacityMinus1;
                        SetNextEntry(entry, newTable[i]);
                        newTable[i] = entry;
                    }
                    entry = next;
                }
            }
            base.Transfer(newTable);
        }
    }
}