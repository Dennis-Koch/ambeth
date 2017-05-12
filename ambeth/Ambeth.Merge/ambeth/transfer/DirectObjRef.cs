using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Merge.Transfer
{
    [DataContract(Name = "DirectObjRef", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class DirectObjRef : ObjRef, IDirectObjRef
    {
        [IgnoreDataMember]
        public Object Direct { get; set; }

        [DataMember]
        public int CreateContainerIndex { get; set; }

        public DirectObjRef()
        {
            CreateContainerIndex = -1;
        }

        public DirectObjRef(Type realType, Object direct)
        {
            CreateContainerIndex = -1;
            RealType = realType;
            this.Direct = direct;
        }

        public override bool Equals(IObjRef obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (Direct != null)
            {
                if (!(obj is IDirectObjRef))
                {
                    return false;
                }
                return Object.ReferenceEquals(Direct, ((IDirectObjRef)obj).Direct); // Identity - not equals - intentionally here!
            }
            return base.Equals(obj);
        }

        public override int GetHashCode()
        {
            if (Direct != null)
            {
                return Direct.GetHashCode();
            }
            return base.GetHashCode();
        }

        public override string ToString()
        {
            if (Direct != null)
            {
                return "ObjRef (new) type=" + RealType.FullName;
            }
            return base.ToString();
        }
    }
}
