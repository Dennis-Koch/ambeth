using System;

namespace De.Osthus.Ambeth.Cache.Collections
{
    public class DefaultCacheMapEntry : CacheMapEntry
    {
        private readonly Type entityType;
        private readonly sbyte idIndex;
        private Object id;

        public DefaultCacheMapEntry(Type entityType, sbyte idIndex, Object id, Object value, CacheMapEntry nextEntry)
            : base(entityType, idIndex, id, value, nextEntry)
        {
            this.entityType = entityType;
            this.idIndex = idIndex;
        }

        public override object Id
        {
            get
            {
                return id;
            }
            protected set
            {
                this.id = value;
            }
        }

        public override Type EntityType
        {
            get
            {
                return entityType;
            }
        }

        public override sbyte IdIndex
        {
            get
            {
                return idIndex;
            }
        }
    }
}