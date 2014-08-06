using De.Osthus.Ambeth.Util;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;

namespace De.Osthus.Ambeth.Collections
{
    public abstract class AbstractTuple3KeyHashMap<Key1, Key2, Key3, V> : IPrintable, Iterable<Tuple3KeyEntry<Key1, Key2, Key3, V>>
    {
        public static readonly int DEFAULT_INITIAL_CAPACITY = 16;

        public static readonly int MAXIMUM_CAPACITY = 1 << 30;

        public static readonly float DEFAULT_LOAD_FACTOR = 0.75f;

        protected readonly float loadFactor;

        protected int threshold;

        protected Tuple3KeyEntry<Key1, Key2, Key3, V>[] table;

        public AbstractTuple3KeyHashMap(int initialCapacity, float loadFactor)
        {
            this.loadFactor = loadFactor;

            if (initialCapacity < 0)
            {
                throw new ArgumentException("Illegal initial capacity: " + initialCapacity);
            }
            if (initialCapacity > MAXIMUM_CAPACITY)
            {
                initialCapacity = MAXIMUM_CAPACITY;
            }
            if (loadFactor <= 0 || Single.IsNaN(loadFactor))
            {
                throw new ArgumentException("Illegal load factor: " + loadFactor);
            }

            // Find a power of 2 >= initialCapacity
            int capacity = 1;
            while (capacity < initialCapacity)
            {
                capacity <<= 1;
            }

            threshold = (int)(capacity * loadFactor);
            table = CreateTable(capacity);

            Init();
        }

        protected Tuple3KeyEntry<Key1, Key2, Key3, V>[] CreateTable(int capacity)
        {
            return new Tuple3KeyEntry<Key1, Key2, Key3, V>[capacity];
        }

        protected virtual void Init()
        {

        }

        protected virtual int ExtractHash(Key1 key1, Key2 key2, Key3 key3)
        {
            return (key1 != null ? key1.GetHashCode() : 3) ^ (key2 != null ? key2.GetHashCode() : 5) ^ (key3 != null ? key3.GetHashCode() : 7);
        }

        protected static int Hash(int hash)
        {
            uint uhash = (uint)hash;

            uhash += ~(uhash << 9);
            uhash ^= uhash >> 14;
            uhash += uhash << 4;
            uhash ^= uhash >> 10;
            return (int)uhash;
        }

        protected void AddEntry(int hash, Key1 key1, Key2 key2, Key3 key3, V value, int bucketIndex)
        {
            Tuple3KeyEntry<Key1, Key2, Key3, V>[] table = this.table;
            Tuple3KeyEntry<Key1, Key2, Key3, V> e = table[bucketIndex];
            e = CreateEntry(hash, key1, key2, key3, value, e);
            table[bucketIndex] = e;
            EntryAdded(e);
            if (Count >= threshold)
            {
                Resize(2 * table.Length);
            }
        }

        protected virtual void EntryAdded(Tuple3KeyEntry<Key1, Key2, Key3, V> entry)
        {
            // Intended blank
        }

        protected virtual void EntryRemoved(Tuple3KeyEntry<Key1, Key2, Key3, V> entry)
        {
            // Intended blank
        }

        /**
         * Rehashes the contents of this map into a new array with a larger capacity. This method is called automatically when the number of keys in this map
         * reaches its threshold. If current capacity is MAXIMUM_CAPACITY, this method does not resize the map, but sets threshold to Integer.MAX_VALUE. This has
         * the effect of preventing future calls.
         * 
         * @param newCapacity
         *            the new capacity, MUST be a power of two; must be greater than current capacity unless current capacity is MAXIMUM_CAPACITY (in which case
         *            value is irrelevant).
         */
        protected void Resize(int newCapacity)
        {
            Tuple3KeyEntry<Key1, Key2, Key3, V>[] oldTable = table;
            int oldCapacity = oldTable.Length;
            if (oldCapacity == MAXIMUM_CAPACITY)
            {
                threshold = Int32.MaxValue;
                return;
            }

            Tuple3KeyEntry<Key1, Key2, Key3, V>[] newTable = CreateTable(newCapacity);
            Transfer(newTable);
            table = newTable;
            threshold = (int)(newCapacity * loadFactor);
        }

        protected void Transfer(Tuple3KeyEntry<Key1, Key2, Key3, V>[] newTable)
        {
            int newCapacityMinus1 = newTable.Length - 1;
            Tuple3KeyEntry<Key1, Key2, Key3, V>[] table = this.table;

            for (int a = table.Length; a-- > 0; )
            {
                Tuple3KeyEntry<Key1, Key2, Key3, V> entry = table[a], next;
                while (entry != null)
                {
                    next = entry.GetNextEntry();
                    int i = entry.GetHash() & newCapacityMinus1;
                    entry.SetNextEntry(newTable[i]);
                    newTable[i] = entry;
                    entry = next;
                }
            }
        }

        public V[] ToArray(V[] targetArray)
        {
            int index = 0;
            Tuple3KeyEntry<Key1, Key2, Key3, V>[] table = this.table;
            for (int a = table.Length; a-- > 0; )
            {
                Tuple3KeyEntry<Key1, Key2, Key3, V> entry = table[a];
                while (entry != null)
                {
                    targetArray[index++] = entry.GetValue();
                    entry = entry.GetNextEntry();
                }
            }
            return targetArray;
        }

        public void Clear()
        {
            if (IsEmpty)
            {
                return;
            }
            Tuple3KeyEntry<Key1, Key2, Key3, V>[] table = this.table;

            for (int a = table.Length; a-- > 0; )
            {
                Tuple3KeyEntry<Key1, Key2, Key3, V> entry = table[a];
                if (entry != null)
                {
                    table[a] = null;
                    while (entry != null)
                    {
                        Tuple3KeyEntry<Key1, Key2, Key3, V> nextEntry = entry.GetNextEntry();
                        EntryRemoved(entry);
                        entry = nextEntry;
                    }
                }
            }
        }

        public bool ContainsKey(Key1 key1, Key2 key2, Key3 key3)
        {
            int hash = Hash(ExtractHash(key1, key2, key3));
            Tuple3KeyEntry<Key1, Key2, Key3, V>[] table = this.table;
            int i = hash & (table.Length - 1);
            Tuple3KeyEntry<Key1, Key2, Key3, V> entry = table[i];

            while (entry != null)
            {
                if (EqualKeys(key1, key2, key3, entry))
                {
                    return true;
                }
                entry = entry.GetNextEntry();
            }
            return false;
        }

        /**
         * @see java.util.Map#containsValue(java.lang.Object)
         */
        public bool ContainsValue(V value)
        {
            Tuple3KeyEntry<Key1, Key2, Key3, V>[] table = this.table;
            if (value == null)
            {
                for (int a = table.Length; a-- > 0; )
                {
                    Tuple3KeyEntry<Key1, Key2, Key3, V> entry = table[a];
                    while (entry != null)
                    {
                        Object entryValue = entry.GetValue();
                        if (entryValue == null)
                        {
                            return true;
                        }
                        entry = entry.GetNextEntry();
                    }
                }
            }
            else
            {
                for (int a = table.Length; a-- > 0; )
                {
                    Tuple3KeyEntry<Key1, Key2, Key3, V> entry = table[a];
                    while (entry != null)
                    {
                        Object entryValue = entry.GetValue();
                        if (value.Equals(entryValue))
                        {
                            return true;
                        }
                        entry = entry.GetNextEntry();
                    }
                }
            }
            return false;
        }

        protected virtual bool EqualKeys(Key1 key1, Key2 key2, Key3 key3, Tuple3KeyEntry<Key1, Key2, Key3, V> entry)
        {
            return Object.Equals(key1, entry.GetKey1()) && Object.Equals(key2, entry.GetKey2()) && Object.Equals(key3, entry.GetKey3());
        }

        public V Put(Key1 key1, Key2 key2, Key3 key3, V value)
        {
            int hash = Hash(ExtractHash(key1, key2, key3));
            Tuple3KeyEntry<Key1, Key2, Key3, V>[] table = this.table;
            int i = hash & (table.Length - 1);

            Tuple3KeyEntry<Key1, Key2, Key3, V> entry = table[i];
            while (entry != null)
            {
                if (EqualKeys(key1, key2, key3, entry))
                {
                    if (IsSetValueForEntryAllowed())
                    {
                        return SetValueForEntry(entry, value);
                    }
                    V oldValue = entry.GetValue();
                    RemoveEntryForKey(key1, key2, key3);
                    AddEntry(hash, key1, key2, key3, value, i);
                    return oldValue;
                }
                entry = entry.GetNextEntry();
            }
            AddEntry(hash, key1, key2, key3, value, i);
            return default(V);
        }

        public bool PutIfNotExists(Key1 key1, Key2 key2, Key3 key3, V value)
        {
            int hash = Hash(ExtractHash(key1, key2, key3));
            Tuple3KeyEntry<Key1, Key2, Key3, V>[] table = this.table;
            int i = hash & (table.Length - 1);

            Tuple3KeyEntry<Key1, Key2, Key3, V> entry = table[i];
            while (entry != null)
            {
                if (EqualKeys(key1, key2, key3, entry))
                {
                    return false;
                }
                entry = entry.GetNextEntry();
            }
            AddEntry(hash, key1, key2, key3, value, i);
            return true;
        }

        public bool RemoveIfValue(Key1 key1, Key2 key2, Key3 key3, V value)
        {
            int hash = Hash(ExtractHash(key1, key2, key3));
            Tuple3KeyEntry<Key1, Key2, Key3, V>[] table = this.table;
            int i = hash & (table.Length - 1);
            Tuple3KeyEntry<Key1, Key2, Key3, V> entry = table[i];
            if (entry != null)
            {
                if (EqualKeys(key1, key2, key3, entry))
                {
                    table[i] = entry.GetNextEntry();
                    V existingValue = entry.GetValue();
                    if (!Object.ReferenceEquals(existingValue, value)) // Test if reference identical
                    {
                        return false;
                    }
                    EntryRemoved(entry);
                    return true;
                }
                Tuple3KeyEntry<Key1, Key2, Key3, V> prevEntry = entry;
                entry = entry.GetNextEntry();
                while (entry != null)
                {
                    if (EqualKeys(key1, key2, key3, entry))
                    {
                        prevEntry.SetNextEntry(entry.GetNextEntry());
                        V existingValue = entry.GetValue();
                        if (!Object.ReferenceEquals(existingValue, value)) // Test if reference identical
                        {
                            return false;
                        }
                        EntryRemoved(entry);
                        return true;
                    }
                    prevEntry = entry;
                    entry = entry.GetNextEntry();
                }
            }
            return false;
        }

        public V Remove(Key1 key1, Key2 key2, Key3 key3)
        {
            return RemoveEntryForKey(key1, key2, key3);
        }

        protected V RemoveEntryForKey(Key1 key1, Key2 key2, Key3 key3)
        {
            int hash = Hash(ExtractHash(key1, key2, key3));
            Tuple3KeyEntry<Key1, Key2, Key3, V>[] table = this.table;
            int i = hash & (table.Length - 1);
            Tuple3KeyEntry<Key1, Key2, Key3, V> entry = table[i];
            if (entry != null)
            {
                if (EqualKeys(key1, key2, key3, entry))
                {
                    table[i] = entry.GetNextEntry();
                    V value = entry.GetValue();
                    EntryRemoved(entry);
                    return value;
                }
                Tuple3KeyEntry<Key1, Key2, Key3, V> prevEntry = entry;
                entry = entry.GetNextEntry();
                while (entry != null)
                {
                    if (EqualKeys(key1, key2, key3, entry))
                    {
                        prevEntry.SetNextEntry(entry.GetNextEntry());
                        V value = entry.GetValue();
                        EntryRemoved(entry);
                        return value;
                    }
                    prevEntry = entry;
                    entry = entry.GetNextEntry();
                }
            }
            return default(V);
        }

        public V Get(Key1 key1, Key2 key2, Key3 key3)
        {
            int hash = Hash(ExtractHash(key1, key2, key3));
            Tuple3KeyEntry<Key1, Key2, Key3, V>[] table = this.table;
            int i = hash & (table.Length - 1);
            Tuple3KeyEntry<Key1, Key2, Key3, V> entry = table[i];
            while (entry != null)
            {
                if (EqualKeys(key1, key2, key3, entry))
                {
                    return entry.GetValue();
                }
                entry = entry.GetNextEntry();
            }
            return default(V);
        }

        protected virtual bool IsSetValueForEntryAllowed()
        {
            return true;
        }

        protected V SetValueForEntry(Tuple3KeyEntry<Key1, Key2, Key3, V> entry, V value)
        {
            V oldValue = entry.GetValue();
            entry.SetValue(value);
            return oldValue;
        }

        protected abstract Tuple3KeyEntry<Key1, Key2, Key3, V> CreateEntry(int hash, Key1 key1, Key2 key2, Key3 key3, V value,
                Tuple3KeyEntry<Key1, Key2, Key3, V> nextEntry);

        public abstract int Count { get; }

        public bool IsEmpty
        {
            get
            {
                return Count == 0;
            }
        }

        public Iterator<Tuple3KeyEntry<Key1, Key2, Key3, V>> Iterator()
        {
            return new Tuple3KeyIterator<Key1, Key2, Key3, V>(this, table, true);
        }

        public Iterator<Tuple3KeyEntry<Key1, Key2, Key3, V>> Iterator(bool removeAllowed)
        {
            return new Tuple3KeyIterator<Key1, Key2, Key3, V>(this, table, removeAllowed);
        }

        public IList<V> Values()
        {
            Tuple3KeyEntry<Key1, Key2, Key3, V>[] table = this.table;
            List<V> valueList = new List<V>(Count);
            for (int a = table.Length; a-- > 0; )
            {
                Tuple3KeyEntry<Key1, Key2, Key3, V> entry = table[a];
                while (entry != null)
                {
                    valueList.Add(entry.GetValue());
                    entry = entry.GetNextEntry();
                }
            }
            return valueList;
        }

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append(Count).Append(" items: [");
            bool first = true;

            Tuple3KeyEntry<Key1, Key2, Key3, V>[] table = this.table;
            for (int a = table.Length; a-- > 0; )
            {
                Tuple3KeyEntry<Key1, Key2, Key3, V> entry = table[a];
                while (entry != null)
                {
                    if (first)
                    {
                        first = false;
                    }
                    else
                    {
                        sb.Append(',');
                    }
                    StringBuilderUtil.AppendPrintable(sb, entry);
                    entry = entry.GetNextEntry();
                }
            }
            sb.Append(']');
        }

        public IEnumerator<Tuple3KeyEntry<Key1, Key2, Key3, V>> GetEnumerator()
        {
            return Iterator();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return Iterator();
        }

        Iterator Iterable.Iterator()
        {
            return Iterator();
        }

        Iterator Iterable.Iterator(bool removeAllowed)
        {
            return Iterator(removeAllowed);
        }
    }
}