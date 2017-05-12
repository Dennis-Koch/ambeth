using System;

namespace De.Osthus.Ambeth.Util
{
    public class Strong2Key<V>
    {
        public readonly V extension;

        public readonly ConversionKey key;

        public Strong2Key(V extension, ConversionKey key)
        {
            this.extension = extension;
            this.key = key;
        }

        public override bool Equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (!(obj is Strong2Key<V>))
            {
                return false;
            }
            Strong2Key<V> other = (Strong2Key<V>)obj;
            return Object.ReferenceEquals(extension, other.extension) && key.Equals(other.key);
        }

        public override int GetHashCode()
        {
            return extension.GetHashCode() ^ key.GetHashCode();
        }
    }
}