using System;

namespace De.Osthus.Ambeth.Cache.Collections
{
    public abstract class CacheMapEntry
    {
        private CacheMapEntry nextEntry;
        
        private Object value;

        public CacheMapEntry(Type entityType, sbyte idIndex, Object id, Object value, CacheMapEntry nextEntry)
        {
            Id = id;
            this.value = value;
            this.nextEntry = nextEntry;
        }

        public abstract Object Id { get; protected set; }

        public abstract Type EntityType { get; }

        public abstract sbyte IdIndex { get; }

        public virtual bool IsEqualTo(Type entityType, sbyte idIndex, Object id)
        {
            return Id.Equals(id) && EntityType.Equals(entityType) && IdIndex == idIndex;
        }

        public CacheMapEntry GetNextEntry()
        {
            return nextEntry;
        }

        public void SetNextEntry(CacheMapEntry nextEntry)
        {
            this.nextEntry = nextEntry;
        }

        public void SetValue(Object value)
        {
            this.value = value;
        }

        public Object GetValue()
        {
            return value;
        }
    }
}