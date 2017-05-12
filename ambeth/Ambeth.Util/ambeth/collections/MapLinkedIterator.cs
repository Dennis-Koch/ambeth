using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Collections
{
    public class MapLinkedIterator<E, K, V> : AbstractIterator<Entry<K, V>> where E : MapLinkedEntry<K, V>
    {
        protected MapLinkedEntry<K, V> currEntry;

        protected IListElem<MapLinkedEntry<K, V>> next;

        protected bool first = true;

        private readonly AbstractLinkedMap<E, K, V> hashMap;

        private readonly InterfaceFastList<E> fastIterationList;

        public MapLinkedIterator(AbstractLinkedMap<E, K, V> hashMap, InterfaceFastList<E> fastIterationList, bool removeAllowed)
            : base(removeAllowed)
        {
            this.hashMap = hashMap;
            this.fastIterationList = fastIterationList;
        }

        public override void Dispose()
        {
            // Intended blank
        }

        public override bool MoveNext()
        {
            if (first)
            {
                IListElem<E> firstElem = fastIterationList.First;
                if (firstElem == null)
                {
                    return false;
                }
                currEntry = firstElem.ElemValue;
                next = currEntry.Next;
                first = false;
                return true;
            }
            else if (next == null)
            {
                return false;
            }
            currEntry = next.ElemValue;
            next = currEntry.Next;
            return true;
        }

        public override Entry<K, V> Current
        {
            get
            {
                return currEntry;
            }
        }

        public override void Remove()
        {
            if (!removeAllowed)
            {
                throw new NotSupportedException();
            }
            hashMap.Remove(currEntry.Key);
            currEntry = null;
        }
    }
}