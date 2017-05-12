using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Text;

namespace De.Osthus.Ambeth.Mixin
{
    public class ObjRefMixin
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public bool ObjRefEquals(IObjRef objRef, Object obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }
            if (!(obj is IObjRef))
            {
                return false;
            }
            IObjRef other = (IObjRef)obj;
            if (objRef.IdNameIndex != other.IdNameIndex || !objRef.RealType.Equals(other.RealType))
            {
                return false;
            }
            Object id = objRef.Id;
            Object otherId = other.Id;
            if (id == null || otherId == null)
            {
                return false;
            }
            if (!id.GetType().IsArray || !otherId.GetType().IsArray)
            {
                return id.Equals(otherId);
            }
            Object[] idArray = (Object[])id;
            Object[] otherIdArray = (Object[])otherId;
            if (idArray.Length != otherIdArray.Length)
            {
                return false;
            }
            for (int a = idArray.Length; a-- > 0; )
            {
                if (!idArray[a].Equals(otherIdArray[a]))
                {
                    return false;
                }
            }
            return true;
        }

        public int ObjRefHashCode(IObjRef objRef)
        {
            return objRef.Id.GetHashCode() ^ objRef.RealType.GetHashCode() ^ objRef.IdNameIndex;
        }

        public void ObjRefToString(IObjRef objRef, StringBuilder sb)
        {
            sb.Append("ObjRef ");
            sbyte idIndex = objRef.IdNameIndex;
            if (idIndex == ObjRef.PRIMARY_KEY_INDEX)
            {
                sb.Append("PK=");
            }
            else
            {
                sb.Append("AK").Append(idIndex).Append('=');
            }
            StringBuilderUtil.AppendPrintable(sb, objRef.Id);
            sb.Append(" version=").Append(objRef.Version).Append(" type=").Append(objRef.RealType.FullName);
        }
    }
}