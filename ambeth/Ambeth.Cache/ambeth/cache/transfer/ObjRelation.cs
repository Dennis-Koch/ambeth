using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Merge.Transfer;
using System.Text;

namespace De.Osthus.Ambeth.Cache.Transfer
{
    [DataContract(Name = "ObjRelation", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class ObjRelation : IObjRelation, IPrintable
    {
        [DataMember(IsRequired=true)]
        public String MemberName { get; set; }

        [DataMember(IsRequired = true)]
    	public Type RealType { get; set; }

        [DataMember(IsRequired = true)]
        public Object[] Ids { get; set; }

        [DataMember(IsRequired = true)]
        public Object Version { get; set; }

        [DataMember(IsRequired = true)]
        public sbyte[] IdIndices { get; set; }

        [IgnoreDataMember]
        protected IObjRef[] objRefs;

        [IgnoreDataMember]
        public IObjRef[] ObjRefs
        {
            get
            {
                if (this.objRefs == null)
		        {
			        Type realType = RealType;
			        Object version = Version;
			        Object[] ids = Ids;
			        sbyte[] idIndices = IdIndices;
			        IObjRef[] objRefs = new IObjRef[ids.Length];
			        for (int a = ids.Length; a-- > 0;)
			        {
				        objRefs[a] = new ObjRef(realType, idIndices[a], ids[a], version);
			        }
			        this.objRefs = objRefs;
		        }
                return this.objRefs;
            }
            set
            {
                objRefs = value;
                if (objRefs == null)
                {
                    Ids = null;
                    IdIndices = null;
                    Version = null;
                    RealType = null;
                }
                else
                {
                    int length = objRefs.Length;
                    Ids = new Object[length];
                    IdIndices = new sbyte[length];
                    for (int a = length; a-- > 0; )
                    {
                        IObjRef objRef = objRefs[a];
                        Ids[a] = objRef.Id;
                        IdIndices[a] = objRef.IdNameIndex;
                    }
                    IObjRef firstObjRef = objRefs[0];
                    Version = firstObjRef.Version;
                    RealType = firstObjRef.RealType;
                }
            }
        }

        public ObjRelation()
        {
        }

        public ObjRelation(IObjRef[] objRefs, String memberName)
        {
            this.ObjRefs = objRefs;
            this.MemberName = memberName;
        }

        public override bool Equals(object obj)
        {
		    if (obj == this)
		    {
			    return true;
		    }
		    if (!(obj is ObjRelation))
		    {
			    return false;
		    }
		    ObjRelation other = (ObjRelation) obj;
            IObjRef[] otherObjRefs = other.ObjRefs;
            if (ObjRefs.Length != otherObjRefs.Length)
            {
                return false;
            }
            for (int a = otherObjRefs.Length; a-- > 0;)
            {
                if (!Object.Equals(ObjRefs[a], otherObjRefs[a]))
                {
                    return false;
                }
            }
            return Object.Equals(RealType, other.RealType) && Object.Equals(MemberName, other.MemberName);
	    }

        public override int GetHashCode()
        {
		    return RealType.GetHashCode() ^ MemberName.GetHashCode();
	    }

        public override String ToString()
	    {
		    StringBuilder sb = new StringBuilder();
		    ToString(sb);
		    return sb.ToString();
	    }

	    public void ToString(StringBuilder sb)
	    {
		    sb.Append("ObjRel: memberName=").Append(MemberName).Append(", ref=");
            StringBuilderUtil.AppendPrintable(sb, objRefs);
	    }
    }
}
