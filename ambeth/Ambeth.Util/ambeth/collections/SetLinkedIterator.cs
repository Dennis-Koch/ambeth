using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Collections
{
    public class SetLinkedIterator<E, K> : AbstractIterator<K> where E : SetLinkedEntry<K>
    {
        protected SetLinkedEntry<K> currEntry;

        protected IListElem<SetLinkedEntry<K>> next;

        protected bool first = true;

        private readonly AbstractLinkedSet<E, K> hashSet;

        private readonly InterfaceFastList<E> fastIterationList;

        public SetLinkedIterator(AbstractLinkedSet<E, K> hashSet, InterfaceFastList<E> fastIterationList, bool removeAllowed)
            : base(removeAllowed)
        {
            this.hashSet = hashSet;
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

        public override K Current
        {
            get
            {
                return currEntry.Key;
            }
        }

        public override void Remove()
        {
            if (!removeAllowed)
            {
                throw new NotSupportedException();
            }
            hashSet.Remove(currEntry.Key);
            currEntry = null;
        }
    }
}