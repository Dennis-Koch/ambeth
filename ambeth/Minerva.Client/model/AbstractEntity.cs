using De.Osthus.Ambeth.Annotation;
using System;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Helloworld.Model
{
    [DataContract(Namespace = "HelloWorld")]
    public class AbstractEntity
    {
        public override bool Equals(object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            long id = this.Id;
            if (id == 0 || !obj.GetType().Equals(GetType()))
            {
                // Entity without an id can not be equal to anything beside itself
                // if it has an id it can not be equal to anything beside entities
                // of the IDENTICAL class
                return false;
            }
            long otherId = ((AbstractEntity)obj).Id;
            if (otherId == 0)
            {
                // Entity without an id can not be equal to anything beside itself
                return false;
            }
            return otherId == id;

        }

        public override string ToString()
        {
            return base.ToString() + ", Id=" + Id + ", Version=" + Version;
        }

        public override int GetHashCode()
        {
            return GetType().GetHashCode() ^ (int)Id;
        }

        [DataMember]
        public virtual long Id { get; set; }

        [DataMember]
        public virtual int Version { get; set; }

        [DataMember]
        public virtual String CreatedBy { get; set; }

        [DataMember]
        public virtual String UpdatedBy { get; set; }

        [DataMember]
        public virtual DateTime? CreatedOn { get; set; }

        [DataMember]
        public virtual DateTime? UpdatedOn { get; set; }

        [IgnoreDataMember]
        [FireThisOnPropertyChange("UpdatedOn")]
        public virtual DateTime? UpdatedOnLocal
        {
            get
            {
                return UpdatedOn.HasValue ? new Nullable<DateTime>(UpdatedOn.Value.ToLocalTime()) : new Nullable<DateTime>();
            }
        }

        [IgnoreDataMember]
        [FireThisOnPropertyChange("CreatedOn")]
        public virtual DateTime? CreatedOnLocal
        {
            get
            {
                return CreatedOn.HasValue ? new Nullable<DateTime>(CreatedOn.Value.ToLocalTime()) : new Nullable<DateTime>();
            }
        }
    }
}
