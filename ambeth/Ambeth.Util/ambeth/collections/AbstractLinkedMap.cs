using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{/**
 * Erweiterte Map, welche zus�tzlich zu den �blichen Key/Value-Entries eine Liste aller Eintr�ge verwaltet. Somit die Komplexit�t f�r das Iterieren
 * �ber eine solchen Map mit O(n) = n identisch mit jener einer �blichen Array-Liste. Der Tradeoff sind hierbei nat�rlich die leicht aufw�ndigeren
 * put()- und remove()-Operationen, welche jedoch weiterhin bzgl. der Komplexit�t mit O(n) = 1 konstant bleiben.
 * 
 * @author kochd
 * @param <K>
 *            Der Typ der in der Map enthaltenen Keys
 * @param <V>
 *            Der Typ der in der Map enthaltenen Values
 */
    public abstract class AbstractLinkedMap<E, K, V> : AbstractHashMap<E, K, V>, ILinkedMap<K, V> where E : MapLinkedEntry<K, V>
    {
        protected readonly InterfaceFastList<E> fastIterationList = new InterfaceFastList<E>();

        public AbstractLinkedMap()
            : base(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public AbstractLinkedMap(float loadFactor)
            : base(DEFAULT_INITIAL_CAPACITY, loadFactor)
        {
            // Intended blank
        }

        public AbstractLinkedMap(int initialCapacity)
            : base(initialCapacity, DEFAULT_LOAD_FACTOR)
        {
            // Intended blank
        }

        public AbstractLinkedMap(int initialCapacity, float loadFactor)
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

        protected override void Transfer(E[] newTable)
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

        public override V[] ToArray(V[] targetArray)
        {
            int index = 0;
            IListElem<E> pointer = fastIterationList.First;
            while (pointer != null)
            {
                targetArray[index++] = pointer.ElemValue.Value;
                pointer = pointer.Next;
            }
            return targetArray;
        }

        public void ToList(IList<V> list)
        {
            IListElem<E> pointer = fastIterationList.First;
            while (pointer != null)
            {
                list.Add(pointer.ElemValue.Value);
                pointer = pointer.Next;
            }
        }

        public void ToKeysList(IList<K> list)
        {
            IListElem<E> pointer = fastIterationList.First;
            while (pointer != null)
            {
                list.Add(pointer.ElemValue.Key);
                pointer = pointer.Next;
            }
        }

        protected override void SetNextEntry(E entry, E nextEntry)
        {
            entry.NextEntry = nextEntry;
        }

        protected override V SetValueForEntry(E entry, V value)
        {
            return entry.SetValue(value);
        }

        //public ISet<Entry<K, V>> EntrySet()
        //{
        //    LinkedHashSet<Entry<K, V>> set = new LinkedHashSet<Entry<K, V>>((int) (size() / AbstractHashSet.DEFAULT_LOAD_FACTOR) + 1);
        //    entrySet(set);
        //    return set;
        //}

        //public void EntrySet(ISet<Entry<K, V>> targetEntrySet)
        //{
        //    MapIterator<K, V> iter = iterator();
        //    while (iter.hasNext())
        //    {
        //        MapLinkedEntry<K, V> entry = iter.nextEntry();
        //        targetEntrySet.add(entry);
        //    }
        //}

        //@Override
        //public ISet<K> KeySet()
        //{
        //    LinkedHashSet<K> set = new LinkedHashSet<K>((int) (size() / AbstractHashSet.DEFAULT_LOAD_FACTOR) + 1);
        //    keySet(set);
        //    return set;
        //}

        //@Override
        //public void KeySet(ISet<K> targetKeySet)
        //{
        //    MapIterator<K, V> iter = iterator();
        //    while (iter.hasNext())
        //    {
        //        MapLinkedEntry<K, V> entry = iter.nextEntry();
        //        targetKeySet.add(entry.getKey());
        //    }
        //}

        public override IList<V> Values()
        {
            List<V> list = new List<V>(Count);
            IListElem<E> pointer = fastIterationList.First;
            while (pointer != null)
            {
                list.Add(pointer.ElemValue.Value);
                pointer = pointer.Next;
            }
            return list;
        }

        public override void Clear()
        {
            int tableLengthMinusOne = this.tableLengthMinusOne;
            IListElem<E> pointer = fastIterationList.First, next;
            while (pointer != null)
            {
                next = pointer.Next;
                E entry = pointer.ElemValue;
                int i = entry.Hash & tableLengthMinusOne;
                table[i] = null;
                EntryRemoved(entry);
                pointer = next;
            }
            fastIterationList.Clear();
        }

        public bool ContainsValue(Object value)
        {
            IListElem<E> pointer = fastIterationList.First;
            if (value == null)
            {
                while (pointer != null)
                {
                    if (pointer.ElemValue.Value == null)
                    {
                        return true;
                    }
                    pointer = pointer.Next;
                }
            }
            else
            {
                while (pointer != null)
                {
                    if (value.Equals(pointer.ElemValue.Value))
                    {
                        return true;
                    }
                    pointer = pointer.Next;
                }
            }
            return false;
        }

        IEnumerator<Entry<K, V>> System.Collections.Generic.IEnumerable<Entry<K, V>>.GetEnumerator()
        {
            return new MapLinkedIterator<E, K, V>(this, fastIterationList, false);
        }

        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
        {
            return new MapLinkedIterator<E, K, V>(this, fastIterationList, false);
        }

        //public override IEnumerator<Entry<K, V>> GetEnumerator()
        //{
        //    return new MapLinkedIterator<E, K, V>(this, fastIterationList, false);
        //}

        public override Iterator<Entry<K, V>> Iterator()
        {
            return new MapLinkedIterator<E, K, V>(this, fastIterationList, true);
        }

	    public override Iterator<Entry<K, V>> Iterator(bool removeAllowed)
	    {
            return new MapLinkedIterator<E, K, V>(this, fastIterationList, removeAllowed);
	    }

        //System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
        //{
        //    return new MapLinkedIterator<E, K, V>(this, fastIterationList, false);
        //}
    }
}