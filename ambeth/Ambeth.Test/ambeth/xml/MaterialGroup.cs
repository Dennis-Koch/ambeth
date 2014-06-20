using System;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Test.Model
{
    [XmlType]
    public class MaterialGroup
    {
        public virtual String Id { get; set; }

        public virtual short Version { get; set; }

        public virtual String Name { get; set; }

        public virtual String Buid { get; set; }

        public override bool Equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (!(obj is MaterialGroup))
            {
                return false;
            }
            MaterialGroup other = (MaterialGroup)obj;
            bool equals = Id != null ? Id.Equals(other.Id) : false;
            equals &= Version == other.Version;
            return equals;
        }

        public override int GetHashCode()
        {
            return typeof(MaterialGroup).GetHashCode() ^ (Id != null ? Id.GetHashCode() : 1);
        }
    }
}
