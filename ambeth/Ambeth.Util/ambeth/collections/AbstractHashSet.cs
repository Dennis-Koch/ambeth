using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Collections
{
    /**
     * Abstrakte HashSet als Basisklasse fuer verschiedene spezialisierte Anwendungsfaelle
     * 
     * @author kochd
     * 
     * @param <E>
     *            Typ der Entrys der Set
     * @param <K>
     *            Typ der Keys
     * @param <V>
     *            Typ der Values
     */
    public abstract class AbstractHashSet<E, K> : IISet<K>, IPrintable where E : class
    {
        public const int DEFAULT_INITIAL_CAPACITY = 16;

        public const int MAXIMUM_CAPACITY = 1 << 30;

        public const float DEFAULT_LOAD_FACTOR = 0.75f;

        protected readonly float loadFactor;

        protected int threshold;

        protected E[] table;

        public AbstractHashSet(int initialCapacity, float loadFactor)
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

        protected void AddEntry(int hash, K key, int bucketIndex)
        {
            E[] table = this.table;
            E e = table[bucketIndex];
            e = CreateEntry(hash, key, e);
            table[bucketIndex] = e;
            EntryAdded(e);
			if (IsResizeNeeded())
            {
                Resize(2 * table.Length);
            }
        }

		protected virtual bool IsResizeNeeded()
		{
			return Count >= threshold;
		}

        public K Get(K key)
        {
            E[] table = this.table;
            int hash = Hash(ExtractHash(key));
            int i = hash & (table.Length - 1);
            E entry = table[i];

            while (entry != null)
            {
                if (EqualKeys(key, entry))
                {
                    return GetKeyOfEntry(entry);
                }
                entry = GetNextEntry(entry);
            }
            return default(K);
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
         * reaches its threshold.
         * 
         * If current capacity is MAXIMUM_CAPACITY, this method does not resize the map, but sets threshold to Integer.MAX_VALUE. This has the effect of preventing
         * future calls.
         * 
         * @param newCapacity
         *            the new capacity, MUST be a power of two; must be greater than current capacity unless current capacity is MAXIMUM_CAPACITY (in which case
         *            value is irrelevant).
         */
        protected virtual void Resize(int newCapacity)
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
            threshold = (int)(newCapacity * loadFactor);
        }

        protected virtual void Transfer(E[] newTable)
        {
            int newCapacityMinus1 = newTable.Length - 1;
            E[] table = this.table;
            if (Object.ReferenceEquals(table, newTable))
            {
                // re-check entries on existing table
                for (int a = table.Length; a-- > 0; )
                {
                    E entry = table[a], previous = null, next;
                    while (entry != null)
                    {
                        next = GetNextEntry(entry);
                        if (IsEntryValid(entry))
                        {
                            previous = entry;
                        }
                        else
                        {
                            if (Object.ReferenceEquals(entry, table[a]))
                            {
                                // first entry in bucket
                                table[a] = next;
                            }
                            else
                            {
                                SetNextEntry(previous, next);
                            }
                            EntryRemoved(entry);
                        }
                        entry = next;
                    }
                }
                return;
            }

            for (int a = table.Length; a-- > 0; )
            {
                E entry = table[a], next;
                while (entry != null)
                {
                    next = GetNextEntry(entry);
                    if (IsEntryValid(entry))
                    {
                        int i = GetHashOfEntry(entry) & newCapacityMinus1;
                        SetNextEntry(entry, newTable[i]);
                        newTable[i] = entry;
                    }
				    else
				    {
					    EntryRemoved(entry);
				    }
                    entry = next;
                }
            }
        }

        protected virtual bool IsEntryValid(E entry)
        {
            return true;
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

        public bool Contains(K key)
        {
            E[] table = this.table;
            int hash = Hash(ExtractHash(key));
            int i = hash & (table.Length - 1);
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

        public bool Contains(Object key)
        {
            return Contains((K)key);
        }

        protected virtual bool EqualKeys(K key, E entry)
        {
            return key.Equals(GetKeyOfEntry(entry));
        }

        public virtual bool Add(K key)
        {
            return AddIntern(key);
        }

        public bool AddIntern(K key)
        {
            E[] table = this.table;
            int hash = Hash(ExtractHash(key));
            int i = hash & (table.Length - 1);

            E entry = table[i];
            while (entry != null)
            {
                if (EqualKeys(key, entry))
                {
                    return false;
                }
                entry = GetNextEntry(entry);
            }
            AddEntry(hash, key, i);
            return true;
        }

        public virtual bool Remove(K key)
        {
            return RemoveEntryForKey(key) != null;
        }

        public virtual K RemoveAndGet(K key)
        {
            return RemoveEntryForKey(key);
        }

        protected K RemoveEntryForKey(K key)
        {
            E[] table = this.table;
            int hash = Hash(ExtractHash(key));
            int i = hash & (table.Length - 1);
            E entry = table[i];
            if (entry != null)
            {
                if (EqualKeys(key, entry))
                {
                    table[i] = GetNextEntry(entry);
                    K keyOfEntry = GetKeyOfEntry(entry);
                    EntryRemoved(entry);
                    return keyOfEntry;
                }
                E prevEntry = entry;
                entry = GetNextEntry(entry);
                while (entry != null)
                {
                    if (EqualKeys(key, entry))
                    {
                        SetNextEntry(prevEntry, GetNextEntry(entry));
                        K keyOfEntry = GetKeyOfEntry(entry);
                        EntryRemoved(entry);
                        return keyOfEntry;
                    }
                    prevEntry = entry;
                    entry = GetNextEntry(entry);
                }
            }
            return default(K);
        }

        public abstract K GetKeyOfEntry(E entry);

        protected abstract int GetHashOfEntry(E entry);

        public abstract E GetNextEntry(E entry);

        protected abstract void SetNextEntry(E entry, E nextEntry);

        protected abstract E CreateEntry(int hash, K key, E nextEntry);
        
        public bool IsEmpty()
        {
            return Count == 0;
        }

        public virtual bool AddAll<S>(S[] array) where S : K
        {
            bool changed = false;
            foreach (S key in array)
            {
                changed |= AddIntern(key);
            }
            return changed;
        }

        public virtual bool AddAll<S>(IEnumerable<S> coll) where S : K
        {
            bool changed = false;
            foreach (K key in coll)
            {
                changed |= AddIntern(key);
            }
            return changed;
        }

        public virtual bool AddAll(IEnumerable coll)
        {
            bool changed = false;
            foreach (K key in coll)
            {
                changed |= AddIntern(key);
            }
            return changed;
        }

        public bool ContainsAll(IEnumerable c)
        {
            foreach (Object key in c)
            {
                if (!Contains((K)key))
                {
                    return false;
                }
            }
            return true;
        }

        public bool ContainsAny(IEnumerable c)
        {
            foreach (Object key in c)
            {
                if (Contains((K)key))
                {
                    return true;
                }
            }
            return false;
        }

		public bool ContainsAny<S>(S[] array) where S : K
		{
			foreach (S key in array)
			{
				if (Contains(key))
				{
					return true;
				}
			}
			return false;
		}

        public virtual bool RemoveAll(IEnumerable c)
        {
            bool changed = false;
            if (c is IList)
            {
                IList list = (IList)c;
                for (int a = list.Count; a-- > 0; )
                {
                    changed |= Remove((K)list[a]);
                }
            }
            else
            {
                foreach (Object key in c)
                {
                    changed |= Remove((K)key);
                }
            }
            return changed;
        }

        public virtual bool RetainAll(IICollection c)
        {
            bool changed = false;
            Iterator iter = Iterator();
            while (iter.MoveNext())
            {
                Object key = iter.Current;
                if (!c.Contains(key))
                {
                    iter.Remove();
                    changed = true;
                }
            }
            return changed;
        }

        public virtual bool RetainAll(IList c)
        {
            bool changed = false;
            CHashSet<K> set = new CHashSet<K>(c);
            Iterator iter = Iterator();
            while (iter.MoveNext())
            {
                Object key = iter.Current;
                if (!c.Contains(key))
                {
                    iter.Remove();
                    changed = true;
                }
            }
            return changed;
        }

        public K[] ToArray()
        {
            return ToArray(new K[Count]);
        }

        protected virtual K[] ToArray(K[] array)
        {
            int index = 0;
            E[] table = this.table;
            for (int a = table.Length; a-- > 0; )
            {
                E entry = table[a];
                while (entry != null)
                {
                    array[index++] = GetKeyOfEntry(entry);
                    entry = GetNextEntry(entry);
                }
            }
            return array;
        }

        public virtual IList<K> ToList()
        {
            List<K> list = new List<K>(Count);
            ToList(list);
            return list;
        }

        public virtual void ToList(IList<K> list)
        {
            E[] table = this.table;
            for (int a = table.Length; a-- > 0; )
            {
                E entry = table[a];
                while (entry != null)
                {
                    list.Add(GetKeyOfEntry(entry));
                    entry = GetNextEntry(entry);
                }
            }
        }

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append(GetType().FullName).Append(' ').Append(Count).Append(" items: [");

            bool first = true;
            foreach (K item in this)
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    sb.Append(',');
                }
                sb.Append(item);
            }
            sb.Append(']');
        }

        public virtual Iterator<K> Iterator()
        {
            return new SetIterator<E, K>(this, table, true);
        }

        public virtual Iterator<K> Iterator(bool removeAllowed)
        {
            return new SetIterator<E, K>(this, table, removeAllowed);
        }

        Iterator Iterable.Iterator()
        {
            return new SetIterator<E, K>(this, table, true);
        }

        Iterator Iterable.Iterator(bool removeAllowed)
        {
            return new SetIterator<E, K>(this, table, removeAllowed);
        }

        IEnumerator<K> IEnumerable<K>.GetEnumerator()
        {
            return new SetIterator<E, K>(this, table, false);
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return new SetIterator<E, K>(this, table, false);
        }

        void System.Collections.Generic.ICollection<K>.Add(K item)
        {
            Add(item);
        }

        public void CopyTo(K[] array, int arrayIndex)
        {
            E[] table = this.table;
            for (int a = table.Length; a-- > 0; )
            {
                E entry = table[a];
                while (entry != null)
                {
                    array[arrayIndex++] = GetKeyOfEntry(entry);
                    entry = GetNextEntry(entry);
                }
            }
        }

        public bool IsReadOnly
        {
            get { return false; }
        }

        void ISet<K>.ExceptWith(IEnumerable<K> other)
        {
            throw new NotImplementedException();
        }

        void ISet<K>.IntersectWith(IEnumerable<K> other)
        {
            throw new NotImplementedException();
        }

        bool ISet<K>.IsProperSubsetOf(IEnumerable<K> other)
        {
            throw new NotImplementedException();
        }

        bool ISet<K>.IsProperSupersetOf(IEnumerable<K> other)
        {
            throw new NotImplementedException();
        }

        bool ISet<K>.IsSubsetOf(IEnumerable<K> other)
        {
            throw new NotImplementedException();
        }

        bool ISet<K>.IsSupersetOf(IEnumerable<K> other)
        {
            throw new NotImplementedException();
        }

        bool ISet<K>.Overlaps(IEnumerable<K> other)
        {
            throw new NotImplementedException();
        }

        bool ISet<K>.SetEquals(IEnumerable<K> other)
        {
            throw new NotImplementedException();
        }

        void ISet<K>.SymmetricExceptWith(IEnumerable<K> other)
        {
            throw new NotImplementedException();
        }

        void ISet<K>.UnionWith(IEnumerable<K> other)
        {
            AddAll(other);
        }
        
        public abstract int Count { get; }

        bool ICollection<K>.IsReadOnly
        {
            get { return false; }
        }
        
        void ICollection.CopyTo(Array array, int index)
        {
            throw new NotImplementedException();
        }

        bool ICollection.IsSynchronized
        {
            get { throw new NotImplementedException(); }
        }

        object ICollection.SyncRoot
        {
            get { throw new NotImplementedException(); }
        }
    }
}