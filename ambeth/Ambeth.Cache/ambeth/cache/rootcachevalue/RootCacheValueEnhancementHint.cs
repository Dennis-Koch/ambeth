using De.Osthus.Ambeth.Bytecode;
using System;

namespace De.Osthus.Ambeth.Cache.Rootcachevalue
{
    public class RootCacheValueEnhancementHint : IEnhancementHint, ITargetNameEnhancementHint
    {
        protected readonly Type entityType;

        public RootCacheValueEnhancementHint(Type entityType)
        {
            this.entityType = entityType;
        }

        public Type EntityType
        {
            get
            {
                return entityType;
            }
        }

        public override bool Equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (!(obj is RootCacheValueEnhancementHint))
            {
                return false;
            }
            RootCacheValueEnhancementHint other = (RootCacheValueEnhancementHint)obj;
            return EntityType.Equals(other.EntityType);
        }

        public override int GetHashCode()
        {
            return GetType().GetHashCode() ^ EntityType.GetHashCode();
        }

        public T Unwrap<T>() where T : IEnhancementHint
        {
            return (T)Unwrap(typeof(T));
        }

        public Object Unwrap(Type includedHintType)
        {
            if (typeof(RootCacheValueEnhancementHint).IsAssignableFrom(includedHintType))
            {
                return this;
            }
            return null;
        }

        public String GetTargetName(Type typeToEnhance)
        {
            return EntityType.FullName + "$" + typeof(RootCacheValue).Name;
        }
    }
}