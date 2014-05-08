using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Merge.Model;
using System.Text;

namespace De.Osthus.Ambeth.Merge.Transfer
{
    [DataContract(Name = "ObjRef", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class ObjRef : IObjRef, IPrintable
    {
        public static readonly IObjRef[] EMPTY_ARRAY = new IObjRef[0];

        public static readonly IObjRef[][] EMPTY_ARRAY_ARRAY = new IObjRef[0][];

        public const sbyte PRIMARY_KEY_INDEX = -1;

        [DataMember]
        public sbyte IdNameIndex { get; set; }

        [DataMember]
        public Object Id { get; set; }

        [DataMember]
        public Object Version { get; set; }

        [DataMember]
        public Type RealType { get; set; }

        public ObjRef()
        {
            IdNameIndex = ObjRef.PRIMARY_KEY_INDEX;
        }

        public ObjRef(Type realType, Object id, Object version) : this(realType, ObjRef.PRIMARY_KEY_INDEX, id, version)
        {
            // Intended blank
        }

        public ObjRef(Type realType, sbyte idNameIndex, Object id, Object version)
        {
            RealType = realType;
            this.IdNameIndex = idNameIndex;
            this.Id = id;
            this.Version = version;
        }

        public void Init(Type entityType, sbyte idNameIndex, Object id, Object version)
	    {
		    RealType = entityType;
		    IdNameIndex = idNameIndex;
		    Id = id;
		    Version = version;
	    }

        public override bool Equals(Object obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }
            if (!(obj is IObjRef))
            {
                return false;
            }
            return Equals((IObjRef)obj);
        }

        public virtual bool Equals(IObjRef obj)
        {
            if (obj == null)
            {
                return false;
            }
            return (IdNameIndex == obj.IdNameIndex) && Object.Equals(Id, obj.Id) && Object.Equals(RealType, obj.RealType);
        }

        public override int GetHashCode()
        {
            return (Id.GetHashCode() ^ RealType.GetHashCode());
        }

	    public override String ToString()
	    {
		    StringBuilder sb = new StringBuilder();
		    ToString(sb);
		    return sb.ToString();
	    }

	    protected virtual String ClassName
	    {
            get
            {
		        return "ORI";
            }
	    }

        public virtual void ToString(StringBuilder sb)
        {
            sb.Append(ClassName).Append(" id=").Append(IdNameIndex).Append(",").Append(Id).Append(" version=").Append(Version).Append(" type=")
                .Append(RealType.FullName);
        }
    }
}
