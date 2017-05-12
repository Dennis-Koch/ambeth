using System;
using System.Collections;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    /**
     * Erweiterte Map, welche zus&auml;tzlich zu den &uuml;blichen Key/Value-Entries eine Liste aller Eintr&auml;ge verwaltet. Somit die Komplexit&auml;t f&uuml;r
     * das Iterieren &uuml;ber eine solchen Map mit O(n) = n identisch mit jener einer &uuml;blichen Array-Liste. Der Tradeoff sind hierbei nat&uuml;rlich de leicht
     * aufw&auml;ndigeren put()- und remove()-Operationen, welche jedoch weiterhin bzgl. der Komplexit&auml;t mit O(n) = 1 konstant bleiben.
     * 
     * @author kochd
     * 
     * @param <K>
     *            Der Typ der in der Map enthaltenen Keys
     * @param <V>
     *            Der Typ der in der Map enthaltenen Values
     */
    public abstract class AbstractLinkedSet<E, K> : AbstractHashSet<E, K>, ICollection<K>, ICollection where E : SetLinkedEntry<K>
    {
        protected readonly InterfaceFastList<E> fastIterationList = new InterfaceFastList<E>();

        public AbstractLinkedSet()
            : base(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public AbstractLinkedSet(float loadFactor)
            : base(DEFAULT_INITIAL_CAPACITY, loadFactor)
        {
            // Intended blank
        }

        public AbstractLinkedSet(int initialCapacity)
            : base(initialCapacity, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public AbstractLinkedSet(int initialCapacity, float loadFactor)
            : base(initialCapacity, loadFactor)
        {
            // Intended blank
        }

        public override int Count
        {
            get
            {
                return fastIterationList.Count;
            }
        }

        protected override void EntryAdded(E entry)
        {
            fastIterationList.PushLast((IListElem<E>)entry);
        }

        protected override void EntryRemoved(E entry)
        {
            fastIterationList.Remove((IListElem<E>)entry);
        }

        protected void transfer(E[] newTable)
        {
            int newCapacityMinus1 = newTable.Length - 1;

            IListElem<E> pointer = fastIterationList.First, next;
            while (pointer != null)
            {
                next = pointer.Next;
                E entry = pointer.ElemValue;
                int i = entry.Hash & newCapacityMinus1;
                entry.NextEntry = newTable[i];
                newTable[i] = entry;
                pointer = next;
            }
        }

        public void ToList(ICollection<K> list)
        {
            IListElem<E> pointer = fastIterationList.First;
            while (pointer != null)
            {
                list.Add(pointer.ElemValue.Key);
                pointer = pointer.Next;
            }
        }

        protected override int GetHashOfEntry(E entry)
        {
            return entry.Hash;
        }

        public override K GetKeyOfEntry(E entry)
        {
            return entry.Key;
        }
        
        public override E GetNextEntry(E entry)
        {
            return (E)entry.NextEntry;
        }

        protected override void SetNextEntry(E entry, E nextEntry)
        {
            entry.NextEntry = nextEntry;
        }

        public override Iterator<K> Iterator()
        {
            return new SetLinkedIterator<E, K>(this, fastIterationList, true);
        }

        public override Iterator<K> Iterator(bool removeAllowed)
        {
            return new SetLinkedIterator<E, K>(this, fastIterationList, removeAllowed);
        }

        IEnumerator<K> IEnumerable<K>.GetEnumerator()
        {
            return new SetLinkedIterator<E, K>(this, fastIterationList, false);
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return new SetLinkedIterator<E, K>(this, fastIterationList, false);
        }

        public override IList<K> ToList()
        {
            List<K> list = new List<K>(Count);
            foreach (K key in this)
            {
                list.Add(key);
            }
            return list;
        }

        public override void Clear()
        {
            SetLinkedEntry<K>[] table = this.table;
            int tableLengthMinusOne = table.Length - 1;
            IListElem<E> entry = fastIterationList.First, next;
            while (entry != null)
            {
                next = entry.Next;
                E elem = entry.ElemValue;
                int i = GetHashOfEntry(elem) & tableLengthMinusOne;
                table[i] = null;
                EntryRemoved(elem);
                entry = next;
            }
            fastIterationList.Clear();
        }
        
        protected override K[] ToArray(K[] array)
        {
            int index = 0;
            IListElem<E> pointer = fastIterationList.First;
            while (pointer != null)
            {
                array[index++] = pointer.ElemValue.Key;
                pointer = pointer.Next;
            }
            return array;
        }
    }
}