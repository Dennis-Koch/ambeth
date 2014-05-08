using De.Osthus.Ambeth.Annotation;
using System;

namespace De.Osthus.Ambeth.Cache.Valueholdercontainer
{
    [EntityEqualsAspect]
    public class MaterialType
    {
        public virtual int Id { get; set; }

        public virtual int Version { get; set; }

        [FireTargetOnPropertyChange("Temp2")]
        public virtual String Name { get; set; }

        [FireThisOnPropertyChange("Name")]
        public virtual String Temp1
        {
            get
            {
                return Name + "$Temp1";
            }
        }

        public virtual String Temp2
        {
            get
            {
                return Name + "$Temp2";
            }
        }
    }
}