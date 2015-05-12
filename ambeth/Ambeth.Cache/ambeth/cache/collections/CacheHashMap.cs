using De.Osthus.Ambeth.Util;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using System.Text;

namespace De.Osthus.Ambeth.Cache.Collections
{
    /**
     * 66 percent faster compared to a normal HashMap with a Tuple2 (Composite-)Key as the Map-Key. This is due to the fact that there is no need to instantiate
     * Tuple2 Keys for put() or get() operations. Of course the overall memory footprint is also the half compared to a normal map: There is only the entry object
     * with 2 key-fields compared to the entry object compared to 1 key-field which contains a Tuple2 Key instance
     * 
     * @param <Key1>
     * @param <Key2>
     * @param 
     */
    public class CacheHashMap : IEnumerable<CacheMapEntry>
    {
        public static readonly int DEFAULT_INITIAL_CAPACITY = 16;

        public static readonly int MAXIMUM_CAPACITY = 1 << 30;

        public static readonly float DEFAULT_LOAD_FACTOR = 0.75f;

        protected readonly float loadFactor;

        protected int threshold;

        protected CacheMapEntry[] table;

        protected int size;

        protected readonly ICacheMapEntryTypeProvider cacheMapEntryTypeProvider;

        public CacheHashMap(ICacheMapEntryTypeProvider cacheMapEntryTypeProvider)
            : this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, cacheMapEntryTypeProvider)
        {
            // Intended blank
        }

        public CacheHashMap(int initialCapacity, float loadFactor, ICacheMapEntryTypeProvider cacheMapEntryTypeProvider)
        {
            this.loadFactor = loadFactor;
            this.cacheMapEntryTypeProvider = cacheMapEntryTypeProvider;

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
        }

        protected CacheMapEntry[] CreateTable(int capacity)
        {
            return new CacheMapEntry[capacity];
        }

        protected int ExtractHash(Type entityType, Object id, sbyte idIndex)
        {
            return entityType.GetHashCode() ^ id.GetHashCode() ^ idIndex;
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

        protected void AddEntry(Type entityType, Object id, sbyte idIndex, Object value, int bucketIndex)
        {
            CacheMapEntry[] table = this.table;
            CacheMapEntry e = table[bucketIndex];
            e = CreateEntry(entityType, id, idIndex, value, e);
            table[bucketIndex] = e;
            EntryAdded(e);
			if (IsResizeNeeded())
            {
                Resize(2 * table.Length);
            }
            if (value is ICacheMapEntryAware)
		    {
			    ((ICacheMapEntryAware) value).SetCacheMapEntry(e);
		    }
        }

		protected bool IsResizeNeeded()
		{
			return Count >= threshold;
		}

        protected void Resize(int newCapacity)
        {
            CacheMapEntry[] oldTable = table;
            int oldCapacity = oldTable.Length;
            if (oldCapacity == MAXIMUM_CAPACITY)
            {
                threshold = Int32.MaxValue;
                return;
            }

            CacheMapEntry[] newTable = CreateTable(newCapacity);
            Transfer(newTable);
            table = newTable;
            threshold = (int)(newCapacity * loadFactor);
        }

        public bool ContainsKey(Type entityType, sbyte idIndex, Object id)
        {
            int hash = Hash(ExtractHash(entityType, id, idIndex));
            CacheMapEntry[] table = this.table;
            int i = hash & (table.Length - 1);
            CacheMapEntry entry = table[i];

            while (entry != null)
            {
                if (EqualKeys(entityType, id, idIndex, entry))
                {
                    return true;
                }
                entry = entry.GetNextEntry();
            }
            return false;
        }

        protected bool EqualKeys(Type entityType, Object id, sbyte idIndex, CacheMapEntry entry)
        {
            return entry.IsEqualTo(entityType, idIndex, id);
        }

        public Object Put(Type entityType, sbyte idIndex, Object id, Object value)
        {
            int hash = Hash(ExtractHash(entityType, id, idIndex));
            CacheMapEntry[] table = this.table;
            int i = hash & (table.Length - 1);

            CacheMapEntry entry = table[i];
            while (entry != null)
            {
                if (EqualKeys(entityType, id, idIndex, entry))
                {
                    return SetValueForEntry(entry, value);
                }
                entry = entry.GetNextEntry();
            }
            AddEntry(entityType, id, idIndex, value, i);
            return null;
        }

        public bool PutIfNotExists(Type entityType, sbyte idIndex, Object id, Object value)
        {
            int hash = Hash(ExtractHash(entityType, id, idIndex));
            CacheMapEntry[] table = this.table;
            int i = hash & (table.Length - 1);

            CacheMapEntry entry = table[i];
            while (entry != null)
            {
                if (EqualKeys(entityType, id, idIndex, entry))
                {
                    return false;
                }
                entry = entry.GetNextEntry();
            }
            AddEntry(entityType, id, idIndex, value, i);
            return true;
        }

        public bool RemoveIfValue(Type entityType, sbyte idIndex, Object id, Object value)
        {
            int hash = Hash(ExtractHash(entityType, id, idIndex));
            CacheMapEntry[] table = this.table;
            int i = hash & (table.Length - 1);
            CacheMapEntry entry = table[i];
            if (entry != null)
            {
                if (EqualKeys(entityType, id, idIndex, entry))
                {
                    table[i] = entry.GetNextEntry();
                    Object existingValue = entry.GetValue();
                    if (!Object.ReferenceEquals(existingValue, value)) // Test if reference identical
                    {
                        return false;
                    }
                    EntryRemoved(entry);
                    return true;
                }
                CacheMapEntry prevEntry = entry;
                entry = entry.GetNextEntry();
                while (entry != null)
                {
                    if (EqualKeys(entityType, id, idIndex, entry))
                    {
                        prevEntry.SetNextEntry(entry.GetNextEntry());
                        Object existingValue = entry.GetValue();
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

        public Object Remove(Type entityType, sbyte idIndex, Object id)
        {
            return RemoveEntryForKey(entityType, idIndex, id);
        }

        protected Object RemoveEntryForKey(Type entityType, sbyte idIndex, Object id)
        {
            int hash = Hash(ExtractHash(entityType, id, idIndex));
            CacheMapEntry[] table = this.table;
            int i = hash & (table.Length - 1);
            CacheMapEntry entry = table[i];
            if (entry != null)
            {
                if (EqualKeys(entityType, id, idIndex, entry))
                {
                    table[i] = entry.GetNextEntry();
                    Object value = entry.GetValue();
                    EntryRemoved(entry);
                    return value;
                }
                CacheMapEntry prevEntry = entry;
                entry = entry.GetNextEntry();
                while (entry != null)
                {
                    if (EqualKeys(entityType, id, idIndex, entry))
                    {
                        prevEntry.SetNextEntry(entry.GetNextEntry());
                        Object value = entry.GetValue();
                        EntryRemoved(entry);
                        return value;
                    }
                    prevEntry = entry;
                    entry = entry.GetNextEntry();
                }
            }
            return null;
        }

        public Object Get(Type entityType, sbyte idIndex, Object id)
        {
            int hash = Hash(ExtractHash(entityType, id, idIndex));
            CacheMapEntry[] table = this.table;
            int i = hash & (table.Length - 1);
            CacheMapEntry entry = table[i];
            while (entry != null)
            {
                if (EqualKeys(entityType, id, idIndex, entry))
                {
                    return entry.GetValue();
                }
                entry = entry.GetNextEntry();
            }
            return null;
        }

        protected Object SetValueForEntry(CacheMapEntry entry, Object value)
        {
            Object oldValue = entry.GetValue();
            entry.SetValue(value);
            if (value is ICacheMapEntryAware)
            {
                ((ICacheMapEntryAware)value).SetCacheMapEntry(entry);
            }
            return oldValue;
        }

        protected CacheMapEntry CreateEntry(Type entityType, Object id, sbyte idIndex, Object value,
                CacheMapEntry nextEntry)
        {
            ICacheMapEntryFactory factory = cacheMapEntryTypeProvider.GetCacheMapEntryType(entityType, idIndex);
            return factory.CreateCacheMapEntry(entityType, idIndex, id, value, nextEntry);
        }

        public bool IsEmpty()
        {
            return Count == 0;
        }

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public int Count
        {
            get
            {
                return size;
            }
        }

        protected void EntryAdded(CacheMapEntry e)
        {
            size++;
        }

        protected void EntryRemoved(CacheMapEntry e)
        {
            size--;
        }

        protected void Transfer(CacheMapEntry[] newTable)
        {
            int newCapacityMinus1 = newTable.Length - 1;
            CacheMapEntry[] table = this.table;

            for (int a = table.Length; a-- > 0; )
            {
                CacheMapEntry entry = table[a], next;
                while (entry != null)
                {
                    next = entry.GetNextEntry();
                    int hash = Hash(ExtractHash(entry.EntityType, entry.Id, entry.IdIndex));
                    int i = hash & newCapacityMinus1;
                    entry.SetNextEntry(newTable[i]);
                    newTable[i] = entry;
                    entry = next;
                }
            }
        }

        public Object[] ToArray()
        {
            int index = 0;
            Object[] targetArray = new Object[Count];
            CacheMapEntry[] table = this.table;
            for (int a = table.Length; a-- > 0; )
            {
                CacheMapEntry entry = table[a];
                while (entry != null)
                {
                    targetArray[index++] = entry.GetValue();
                    entry = entry.GetNextEntry();
                }
            }
            return targetArray;
        }

        public CacheMapIterator Iterator()
        {
            return new CacheMapIterator(this, table, true);
        }

        public CacheMapIterator Iterator(bool removeAllowed)
        {
            return new CacheMapIterator(this, table, removeAllowed);
        }

        public IEnumerator<CacheMapEntry> GetEnumerator()
        {
            return Iterator();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return Iterator();
        }

        public void Clear()
        {
            if (IsEmpty())
            {
                return;
            }
            CacheMapEntry[] table = this.table;

            for (int a = table.Length; a-- > 0; )
            {
                CacheMapEntry entry = table[a];
                if (entry != null)
                {
                    table[a] = null;
                    while (entry != null)
                    {
                        CacheMapEntry nextEntry = entry.GetNextEntry();
                        EntryRemoved(entry);
                        entry = nextEntry;
                    }
                }
            }
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append(Count).Append(" items: [");
            bool first = true;

            CacheMapEntry[] table = this.table;
            for (int a = table.Length; a-- > 0; )
            {
                CacheMapEntry entry = table[a];
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
    }
}