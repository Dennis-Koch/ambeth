using System;
using System.Runtime.CompilerServices;

namespace De.Osthus.Ambeth.Mapping
{
    public class CompositIdentityClassKey
    {
        private readonly Object entity;

        private readonly Type type;

        private int hash;

        public CompositIdentityClassKey(Object entity, Type type)
        {
            this.entity = entity;
            this.type = type;

            hash = RuntimeHelpers.GetHashCode(entity) * 13;
            if (type != null)
            {
                hash += type.GetHashCode() * 23;
            }
        }

        public override bool Equals(Object obj)
        {
            if (!(obj is CompositIdentityClassKey))
            {
                return false;
            }
            CompositIdentityClassKey otherKey = (CompositIdentityClassKey)obj;
            bool ee = entity == otherKey.entity;
            bool te = type == otherKey.type;
            return ee && te;
        }

        public override int GetHashCode()
        {
            return hash;
        }
    }
}