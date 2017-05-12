using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Collections
{
    public class SetIterator<E, K> : AbstractIterator<K> where E : class
    {
        protected E currEntry;

        protected int index;

        protected readonly E[] table;

        protected bool first = true;

        private readonly AbstractHashSet<E, K> hashSet;

        public SetIterator(AbstractHashSet<E, K> hashSet, E[] table, bool removeAllowed)
            : base(removeAllowed)
        {
            this.hashSet = hashSet;
            this.table = table;
        }

        public override void Dispose()
        {
            // Intended blank
        }

        protected E GetNextBucketFromIndex(int index)
        {
            E[] table = this.table;
            while (index-- > 0)
            {
                E entry = table[index];
                if (entry != null)
                {
                    this.index = index;
                    return entry;
                }
            }
            return default(E);
        }

        public override bool MoveNext()
        {
            if (first)
            {
                currEntry = GetNextBucketFromIndex(table.Length);
                if (currEntry == null)
                {
                    return false;
                }
                first = false;
                return true;
            }
            else if (currEntry != null)
            {
                currEntry = hashSet.GetNextEntry(currEntry);
            }
            if (currEntry == null)
            {
                currEntry = GetNextBucketFromIndex(index);
            }
            return currEntry != null;
        }

        public override K Current
        {
            get
            {
                return hashSet.GetKeyOfEntry(currEntry);
            }
        }

        public override void Remove()
        {
            if (!removeAllowed)
            {
                throw new NotSupportedException();
            }
            hashSet.Remove(hashSet.GetKeyOfEntry(currEntry));
            currEntry = default(E);
        }
    }
}