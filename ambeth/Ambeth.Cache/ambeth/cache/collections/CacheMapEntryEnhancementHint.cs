using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Util;
using System;

namespace De.Osthus.Ambeth.Cache.Collections
{
    public class CacheMapEntryEnhancementHint : IEnhancementHint, ITargetNameEnhancementHint
    {
        protected readonly Type entityType;

        protected readonly sbyte idIndex;

        public CacheMapEntryEnhancementHint(Type entityType, sbyte idIndex)
        {
            this.entityType = entityType;
            this.idIndex = idIndex;
        }

        public Type GetEntityType()
        {
            return entityType;
        }

        public sbyte GetIdIndex()
        {
            return idIndex;
        }

        public override bool Equals(Object obj)
        {
            if (Object.ReferenceEquals(obj, this))
            {
                return true;
            }
            if (!(obj is CacheMapEntryEnhancementHint))
            {
                return false;
            }
            CacheMapEntryEnhancementHint other = (CacheMapEntryEnhancementHint)obj;
            return GetEntityType().Equals(other.GetEntityType()) && GetIdIndex() == other.GetIdIndex();
        }

        public override int GetHashCode()
        {
            return GetType().GetHashCode() ^ GetEntityType().GetHashCode() ^ GetIdIndex();
        }

        public T Unwrap<T>() where T : IEnhancementHint
        {
            return (T)Unwrap(typeof(T));
        }

        public Object Unwrap(Type includedHintType)
        {
            if (typeof(CacheMapEntryEnhancementHint).IsAssignableFrom(includedHintType))
            {
                return this;
            }
            return null;
        }

        public override string ToString()
        {
            return GetType().Name + ": " + GetTargetName(null);
        }

        public String GetTargetName(Type typeToEnhance)
        {
            return entityType.FullName + "$" + typeof(CacheMapEntry).Name + "$" + (idIndex == ObjRef.PRIMARY_KEY_INDEX ? "PK" : "" + idIndex);
        }
    }
}