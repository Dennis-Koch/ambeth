using System;

namespace De.Osthus.Ambeth.Ioc.Extendable
{
    public class StrongKey<V>
    {
        public readonly V extension;

        public readonly Type strongType;

        public StrongKey(V extension, Type strongType)
        {
            this.extension = extension;
            this.strongType = strongType;
        }

        public override bool Equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (!(obj is StrongKey<V>))
            {
                return false;
            }
            StrongKey<V> other = (StrongKey<V>)obj;
            return Object.ReferenceEquals(extension, other.extension) && strongType.Equals(other.strongType);
        }

        public override int GetHashCode()
        {
            return extension.GetHashCode() ^ strongType.GetHashCode();
        }

        public override String ToString()
        {
            return "(Key: " + strongType.Name + " Extension: " + extension.ToString() + ")";
        }
    }
}