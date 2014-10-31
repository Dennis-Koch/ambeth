using System;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using System.Text;

namespace De.Osthus.Ambeth.Collections
{

    /**
     * Abstrakte HashMap als Basisklasse fuerr verschiedene spezialisierte Anwendungsfaelle
     * 
     * @author kochd
     * @param <E>
     *            Typ der Entrys der Map
     * @param <K>
     *            Typ der Keys
     * @param <V>
     *            Typ der Values
     */
    public abstract class AbstractHashMap<E, K, V> : IMap<K, V> where E : IMapEntry<K, V>
    {
        public const int DEFAULT_INITIAL_CAPACITY = 16;

        public const int MAXIMUM_CAPACITY = 1 << 30;

        public const float DEFAULT_LOAD_FACTOR = 0.75f;

        protected readonly float loadFactor;

        protected int threshold, tableLengthMinusOne;

        protected E[] table;

        public AbstractHashMap(int initialCapacity, float loadFactor)
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
            tableLengthMinusOne = table.Length - 1;

            Init();
        }

        protected E[] CreateTable(int capacity)
        {
            return new E[capacity];
        }
        
        protected virtual void Init()
        {

        }

        protected virtual int ExtractHash(K key)
        {
            return key.GetHashCode();
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

        protected void AddEntry(int hash, K key, V value, int bucketIndex)
        {
            E e = table[bucketIndex];
            e = CreateEntry(hash, key, value, e);
            table[bucketIndex] = e;
            EntryAdded(e);
            if (Count >= threshold)
            {
                Resize(2 * table.Length);
            }
        }

        protected virtual void EntryAdded(E entry)
        {
            // Intended blank
        }

        protected virtual void EntryRemoved(E entry)
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
            E[] oldTable = table;
            int oldCapacity = oldTable.Length;
            if (oldCapacity == MAXIMUM_CAPACITY)
            {
                threshold = Int32.MaxValue;
                return;
            }
            E[] newTable = CreateTable(newCapacity);
            Transfer(newTable);
            table = newTable;
            tableLengthMinusOne = table.Length - 1;
            threshold = (int)(newCapacity * loadFactor);
        }

        protected virtual void Transfer(E[] newTable)
        {
            int newCapacityMinus1 = newTable.Length - 1;
            E[] table = this.table;

            for (int a = table.Length; a-- > 0; )
            {
                E entry = table[a], next;
                while (entry != null)
                {
                    next = GetNextEntry(entry);
                    int i = entry.Hash & newCapacityMinus1;
                    SetNextEntry(entry, newTable[i]);
                    newTable[i] = entry;
                    entry = next;
                }
            }
        }

        public V[] ToArray()
        {
            return ToArray(new V[Count]);
        }

        public virtual V[] ToArray(V[] targetArray)
        {
            int index = 0;
            for (int a = table.Length; a-- > 0; )
            {
                E entry = table[a];
                while (entry != null)
                {
                    targetArray[index++] = entry.Value;
                    entry = GetNextEntry(entry);
                }
            }
            return targetArray;
        }

        public virtual void Clear()
        {
            if (IsEmpty())
            {
                return;
            }
            E[] table = this.table;

            for (int a = table.Length; a-- > 0; )
            {
                E entry = table[a];
                if (entry != null)
                {
                    table[a] = default(E);
                    while (entry != null)
                    {
                        E nextEntry = GetNextEntry(entry);
                        EntryRemoved(entry);
                        entry = nextEntry;
                    }
                }
            }
        }

        public virtual bool ContainsKey(K key)
        {
            int hash = Hash(ExtractHash(key));
            int i = hash & tableLengthMinusOne;
            E entry = table[i];

            while (entry != null)
            {
                if (EqualKeys(key, entry))
                {
                    return true;
                }
                entry = GetNextEntry(entry);
            }
            return false;
        }

        public bool ContainsValue(V value)
        {
            if (value == null)
            {
                for (int a = table.Length; a-- > 0; )
                {
                    E entry = table[a];
                    while (entry != null)
                    {
                        Object entryValue = entry.Value;
                        if (entryValue == null)
                        {
                            return true;
                        }
                        entry = GetNextEntry(entry);
                    }
                }
            }
            else
            {
                for (int a = table.Length; a-- > 0; )
                {
                    E entry = table[a];
                    while (entry != null)
                    {
                        Object entryValue = entry.Value;
                        if (value.Equals(entryValue))
                        {
                            return true;
                        }
                        entry = GetNextEntry(entry);
                    }
                }
            }
            return false;
        }

        protected virtual bool EqualKeys(K key, E entry)
        {
            return key.Equals(entry.Key);
        }

        public virtual V Put(K key, V value)
        {
            int hash = Hash(ExtractHash(key));
            int i = hash & tableLengthMinusOne;

            E entry = table[i];
            while (entry != null)
            {
                if (EqualKeys(key, entry))
                {
                    return SetValueForEntry(entry, value);
                }
                entry = GetNextEntry(entry);
            }
            AddEntry(hash, key, value, i);
            return default(V);
        }

        public virtual bool PutIfNotExists(K key, V value)
        {
            int hash = Hash(ExtractHash(key));
            int i = hash & tableLengthMinusOne;

            E entry = table[i];
            while (entry != null)
            {
                if (EqualKeys(key, entry))
                {
                    return false;
                }
                entry = GetNextEntry(entry);
            }
            AddEntry(hash, key, value, i);
            return true;
        }

        public virtual bool RemoveIfValue(K key, V value)
        {
            int hash = Hash(ExtractHash(key));
            int i = hash & tableLengthMinusOne;
            E entry = table[i];
            if (entry != null)
            {
                if (EqualKeys(key, entry))
                {
                    table[i] = GetNextEntry(entry);
                    V existingValue = entry.Value;
                    if (!Object.ReferenceEquals(existingValue, value)) // Test if reference identical
                    {
                        return false;
                    }
                    EntryRemoved(entry);
                    return true;
                }
                E prevEntry = entry;
                entry = GetNextEntry(entry);
                while (entry != null)
                {
                    if (EqualKeys(key, entry))
                    {
                        SetNextEntry(prevEntry, GetNextEntry(entry));
                        V existingValue = entry.Value;
                        if (!Object.ReferenceEquals(existingValue, value)) // Test if reference identical
                        {
                            return false;
                        }
                        EntryRemoved(entry);
                        return true;
                    }
                    prevEntry = entry;
                    entry = GetNextEntry(entry);
                }
            }
            return false;
        }

        public virtual V Remove(K key)
        {
            return RemoveEntryForKey(key);
        }

        protected V RemoveEntryForKey(K key)
        {
            int hash = Hash(ExtractHash(key));
            int i = hash & tableLengthMinusOne;
            E entry = table[i];
            if (entry != null)
            {
                if (EqualKeys(key, entry))
                {
                    table[i] = GetNextEntry(entry);
                    V value = entry.Value;
                    EntryRemoved(entry);
                    return value;
                }
                E prevEntry = entry;
                entry = GetNextEntry(entry);
                while (entry != null)
                {
                    if (EqualKeys(key, entry))
                    {
                        SetNextEntry(prevEntry, GetNextEntry(entry));
                        V value = entry.Value;
                        EntryRemoved(entry);
                        return value;
                    }
                    prevEntry = entry;
                    entry = GetNextEntry(entry);
                }
            }
            return default(V);
        }

        public V Get(K key)
        {
            int hash = Hash(ExtractHash(key));
            int i = hash & tableLengthMinusOne;
            E entry = table[i];
            while (entry != null)
            {
                if (EqualKeys(key, entry))
                {
                    return entry.Value;
                }
                entry = GetNextEntry(entry);
            }
            return default(V);
        }

        public K GetKey(K key)
        {
            int hash = Hash(ExtractHash(key));
            int i = hash & tableLengthMinusOne;
            E entry = table[i];
            while (entry != null)
            {
                if (EqualKeys(key, entry))
                {
                    return entry.Key;
                }
                entry = GetNextEntry(entry);
            }
            return default(K);
        }
        
        protected abstract V SetValueForEntry(E entry, V value);

        protected virtual E GetNextEntry(E entry)
        {
            return (E)entry.NextEntry;
        }

        protected abstract void SetNextEntry(E entry, E nextEntry);

        protected abstract E CreateEntry(int hash, K key, V value, E nextEntry);

        public abstract int Count { get; }

        public bool IsEmpty()
        {
            return Count == 0;
        }
        
        public virtual void PutAll(IMap<K, V> map)
        {
            if (map is IMap<K, V>)
            {
                foreach (Entry<K, V> entry in map)
                {
                    Put(entry.Key, entry.Value);
                }
            }
        }

        public virtual IISet<K> KeySet()
        {
            LinkedHashSet<K> keySet = LinkedHashSet<K>.Create(Count);
            KeySet(keySet);
            return keySet;
        }

        public virtual void KeySet(IISet<K> targetKeySet)
        {
            for (int a = table.Length; a-- > 0; )
            {
                E entry = table[a];
                while (entry != null)
                {
                    targetKeySet.Add(entry.Key);
                    entry = GetNextEntry(entry);
                }
            }
        }

        public virtual IList<V> Values()
        {
            List<V> valueList = new List<V>(Count);
            for (int a = table.Length; a-- > 0; )
            {
                E entry = table[a];
                while (entry != null)
                {
                    valueList.Add(entry.Value);
                    entry = GetNextEntry(entry);
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

        public virtual void ToString(StringBuilder sb)
        {
            sb.Append(GetType().FullName).Append(' ').Append(Count).Append(" items: [");

            bool first = true;

            for (int a = table.Length; a-- > 0; )
            {
                E entry = table[a];
                while (entry != null)
                {
                    K key = entry.Key;
                    V value = entry.Value;

                    if (first)
                    {
                        first = false;
                    }
                    else
                    {
                        sb.Append(',');
                    }
                    sb.Append('(').Append(key).Append(',').Append(value).Append(')');
                    entry = GetNextEntry(entry);
                }
            }
            sb.Append(']');
        }

        public virtual Iterator<Entry<K, V>> Iterator()
        {
            return new MapIterator<E, K, V>(this, table, true);
        }

        public virtual Iterator<Entry<K, V>> Iterator(bool removeAllowed)
        {
            return new MapIterator<E, K, V>(this, table, removeAllowed);
        }
        
        Iterator Iterable.Iterator()
        {
            return new MapIterator<E, K, V>(this, table, true);
        }

        Iterator Iterable.Iterator(bool removeAllowed)
        {
            return new MapIterator<E, K, V>(this, table, removeAllowed);
        }

        public virtual IEnumerator<Entry<K, V>> GetEnumerator()
        {
            return new MapIterator<E, K, V>(this, table, false);
        }

        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
        {
            return new MapIterator<E, K, V>(this, table, false);
        }
    }
}