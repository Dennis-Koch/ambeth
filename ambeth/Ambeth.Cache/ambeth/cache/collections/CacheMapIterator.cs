using De.Osthus.Ambeth.Collections;
using System;

namespace De.Osthus.Ambeth.Cache.Collections
{
    public class CacheMapIterator : AbstractIterator<CacheMapEntry>
    {
        protected CacheMapEntry currEntry, nextEntry;

        protected int index;

        protected readonly CacheMapEntry[] table;

        protected readonly CacheHashMap hashMap;

        protected bool first;

        public CacheMapIterator(CacheHashMap hashMap, CacheMapEntry[] table, bool removeAllowed)
            : base(removeAllowed)
        {
            this.hashMap = hashMap;
            this.table = table;
            nextEntry = GetNextBucketFromIndex(table.Length);
        }

        public override void Dispose()
        {
            // Intended blank
        }

        protected CacheMapEntry GetNextBucketFromIndex(int index)
        {
            CacheMapEntry[] table = this.table;
            while (index-- > 0)
            {
                CacheMapEntry entry = table[index];
                if (entry != null)
                {
                    this.index = index;
                    return entry;
                }
            }
            return null;
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
                currEntry = currEntry.GetNextEntry();
            }
            if (currEntry == null)
            {
                currEntry = GetNextBucketFromIndex(index);
            }
            return currEntry != null;
        }

        public override CacheMapEntry Current
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
            hashMap.Remove(currEntry.EntityType, currEntry.IdIndex, currEntry.Id);
            currEntry = null;
        }
    }
}