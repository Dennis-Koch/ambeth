using System;

namespace De.Osthus.Ambeth.Collections
{
    public class Tuple2KeyIterator<Key1, Key2, V> : AbstractIterator<Tuple2KeyEntry<Key1, Key2, V>>
    {
        protected Tuple2KeyEntry<Key1, Key2, V> currEntry, nextEntry;

        protected int index;

        protected readonly Tuple2KeyEntry<Key1, Key2, V>[] table;

        protected bool first = true;

        private readonly AbstractTuple2KeyHashMap<Key1, Key2, V> hashMap;

        public Tuple2KeyIterator(AbstractTuple2KeyHashMap<Key1, Key2, V> hashMap, Tuple2KeyEntry<Key1, Key2, V>[] table, bool removeAllowed) : base(removeAllowed)
        {
            this.hashMap = hashMap;
            this.table = table;
        }

        public override void Dispose()
        {
            // Intended blank
        }

        protected Tuple2KeyEntry<Key1, Key2, V> GetNextBucketFromIndex(int index)
        {
            Tuple2KeyEntry<Key1, Key2, V>[] table = this.table;
            while (index-- > 0)
            {
                Tuple2KeyEntry<Key1, Key2, V> entry = table[index];
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

        public override Tuple2KeyEntry<Key1, Key2, V> Current
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
            hashMap.Remove(currEntry.GetKey1(), currEntry.GetKey2());
            currEntry = null;
        }
    }
}