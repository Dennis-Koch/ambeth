using System;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Test.Model
{
    [XmlType]
    public class Material
    {
        public virtual int Id { get; set; }

        public virtual short Version { get; set; }

        public virtual String Name { get; set; }

        public virtual String Buid { get; set; }

        public virtual MaterialGroup MaterialGroup { get; set; }

        public virtual DateTime? CreatedOn { get; set; }

        public virtual String CreatedBy { get; set; }

        public virtual DateTime? UpdatedOn { get; set; }

        public virtual String UpdatedBy { get; set; }
        
        public override int GetHashCode()
        {
            return Id ^ typeof(Material).GetHashCode();
        }

        public override bool Equals(Object obj)
        {
            if (!(obj is Material))
            {
                return false;
            }
            Material other = (Material)obj;
            return other.Id == Id && other.Version == Version;
        }
    }
}
