using De.Osthus.Ambeth.Util;
using System;
using System.Text;
namespace De.Osthus.Ambeth.Objrefstore
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
    public class ObjRefStoreSet
    {
        public static readonly int DEFAULT_INITIAL_CAPACITY = 16;

        public static readonly int MAXIMUM_CAPACITY = 1 << 30;

        public static readonly float DEFAULT_LOAD_FACTOR = 0.75f;

        protected readonly float loadFactor;

        protected int threshold;

        protected ObjRefStore[] table;

        protected int size;

        protected readonly IObjRefStoreEntryProvider objRefStoreEntryProvider;

        public ObjRefStoreSet(IObjRefStoreEntryProvider objRefStoreEntryProvider)
            : this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, objRefStoreEntryProvider)
        {
        }

        public ObjRefStoreSet(int initialCapacity, float loadFactor, IObjRefStoreEntryProvider objRefStoreEntryProvider)
        {
            this.loadFactor = loadFactor;
            this.objRefStoreEntryProvider = objRefStoreEntryProvider;

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

        protected ObjRefStore[] CreateTable(int capacity)
        {
            return new ObjRefStore[capacity];
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

        protected ObjRefStore AddEntry(Type entityType, Object id, sbyte idIndex, int bucketIndex)
        {
            ObjRefStore[] table = this.table;
            ObjRefStore e = table[bucketIndex];
            e = CreateEntry(entityType, id, idIndex, e);
            table[bucketIndex] = e;
            EntryAdded(e);
            if (Count >= threshold)
            {
                Resize(2 * table.Length);
            }
            return e;
        }

        protected void Resize(int newCapacity)
        {
            ObjRefStore[] oldTable = table;
            int oldCapacity = oldTable.Length;
            if (oldCapacity == MAXIMUM_CAPACITY)
            {
                threshold = Int32.MaxValue;
                return;
            }

            ObjRefStore[] newTable = CreateTable(newCapacity);
            Transfer(newTable);
            table = newTable;
            threshold = (int)(newCapacity * loadFactor);
        }

        public bool ContainsKey(Type entityType, sbyte idIndex, Object id)
        {
            int hash = Hash(ExtractHash(entityType, id, idIndex));
            ObjRefStore[] table = this.table;
            int i = hash & (table.Length - 1);
            ObjRefStore entry = table[i];

            while (entry != null)
            {
                if (EqualKeys(entityType, id, idIndex, entry))
                {
                    return true;
                }
                entry = entry.NextEntry;
            }
            return false;
        }

        protected bool EqualKeys(Type entityType, Object id, sbyte idIndex, ObjRefStore entry)
        {
            return entry.IsEqualTo(entityType, idIndex, id);
        }

        public ObjRefStore Put(Type entityType, sbyte idIndex, Object id)
        {
            int hash = Hash(ExtractHash(entityType, id, idIndex));
            ObjRefStore[] table = this.table;
            int i = hash & (table.Length - 1);

            ObjRefStore entry = table[i];
            while (entry != null)
            {
                if (EqualKeys(entityType, id, idIndex, entry))
                {
                    return entry;
                }
                entry = entry.NextEntry;
            }
            return AddEntry(entityType, id, idIndex, i);
        }

        public ObjRefStore Remove(ObjRefStore objRefStore)
        {
            return RemoveEntryForKey(objRefStore.RealType, objRefStore.IdNameIndex, objRefStore.Id);
        }

        public ObjRefStore Remove(Type entityType, sbyte idIndex, Object id)
        {
            return RemoveEntryForKey(entityType, idIndex, id);
        }

        protected ObjRefStore RemoveEntryForKey(Type entityType, sbyte idIndex, Object id)
        {
            int hash = Hash(ExtractHash(entityType, id, idIndex));
            ObjRefStore[] table = this.table;
            int i = hash & (table.Length - 1);
            ObjRefStore entry = table[i];
            if (entry != null)
            {
                if (EqualKeys(entityType, id, idIndex, entry))
                {
                    table[i] = entry.NextEntry;
                    EntryRemoved(entry);
                    return entry;
                }
                ObjRefStore prevEntry = entry;
                entry = entry.NextEntry;
                while (entry != null)
                {
                    if (EqualKeys(entityType, id, idIndex, entry))
                    {
                        prevEntry.NextEntry = entry.NextEntry;
                        EntryRemoved(entry);
                        return entry;
                    }
                    prevEntry = entry;
                    entry = entry.NextEntry;
                }
            }
            return null;
        }

        public ObjRefStore Get(Type entityType, sbyte idIndex, Object id)
        {
            int hash = Hash(ExtractHash(entityType, id, idIndex));
            ObjRefStore[] table = this.table;
            int i = hash & (table.Length - 1);
            ObjRefStore entry = table[i];
            while (entry != null)
            {
                if (EqualKeys(entityType, id, idIndex, entry))
                {
                    return entry;
                }
                entry = entry.NextEntry;
            }
            return null;
        }

        protected ObjRefStore CreateEntry(Type entityType, Object id, sbyte idIndex, ObjRefStore nextEntry)
        {
            return objRefStoreEntryProvider.CreateObjRefStore(entityType, idIndex, id, nextEntry);
        }

        public bool IsEmpty
        {
            get
            {
                return Count == 0;
            }
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

        protected void EntryAdded(ObjRefStore e)
        {
            size++;
        }

        protected void EntryRemoved(ObjRefStore e)
        {
            size--;
        }

        protected void Transfer(ObjRefStore[] newTable)
        {
            int newCapacityMinus1 = newTable.Length - 1;
            ObjRefStore[] table = this.table;

            for (int a = table.Length; a-- > 0; )
            {
                ObjRefStore entry = table[a], next;
                while (entry != null)
                {
                    next = entry.NextEntry;
                    int hash = Hash(ExtractHash(entry.RealType, entry.Id, entry.IdNameIndex));
                    int i = hash & newCapacityMinus1;
                    entry.NextEntry = newTable[i];
                    newTable[i] = entry;
                    entry = next;
                }
            }
        }

        public Object[] ToArray()
        {
            int index = 0;
            Object[] targetArray = new Object[Count];
            ObjRefStore[] table = this.table;
            for (int a = table.Length; a-- > 0; )
            {
                ObjRefStore entry = table[a];
                while (entry != null)
                {
                    targetArray[index++] = entry;
                    entry = entry.NextEntry;
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
            ObjRefStore[] table = this.table;

            for (int a = table.Length; a-- > 0; )
            {
                ObjRefStore entry = table[a];
                if (entry != null)
                {
                    table[a] = null;
                    while (entry != null)
                    {
                        ObjRefStore nextEntry = entry.NextEntry;
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

            ObjRefStore[] table = this.table;
            for (int a = table.Length; a-- > 0; )
            {
                ObjRefStore entry = table[a];
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
                    entry = entry.NextEntry;
                }
            }
            sb.Append(']');
        }
    }
}