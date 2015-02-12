using System;

namespace De.Osthus.Ambeth.Cache
{
    public class CacheKey
    {
        public Type EntityType;

        public Object Id;

        public sbyte IdIndex;

        public override int GetHashCode()
        {
            return EntityType.GetHashCode() ^ Id.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }
            if (!(obj is CacheKey))
            {
                return false;
            }
            CacheKey other = (CacheKey)obj;
            return Object.Equals(Id, other.Id)
                && Object.Equals(EntityType, other.EntityType)
                && IdIndex == other.IdIndex;
        }

        public override string ToString()
        {
            return "CacheKey: " + EntityType.FullName + "(" + IdIndex + "," + Id + ")";
        }
    }
}