using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Collections
{
    public class MapIterator<E, K, V> : AbstractIterator<Entry<K, V>> where E : IMapEntry<K, V>
    {
        protected IMapEntry<K, V> currEntry;

        protected int index;

        protected readonly E[] table;

        protected bool first = true;

        private readonly AbstractHashMap<E, K, V> hashMap;

        public MapIterator(AbstractHashMap<E, K, V> hashMap, E[] table, bool removeAllowed)
            : base(removeAllowed)
        {
            this.hashMap = hashMap;
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
                currEntry = currEntry.NextEntry;
            }
            if (currEntry == null)
            {
                currEntry = GetNextBucketFromIndex(index);
            }
            return currEntry != null;
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