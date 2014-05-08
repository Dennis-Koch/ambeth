using De.Osthus.Ambeth.Log;
#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif
using System;
using System.Collections;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Cache;

namespace De.Osthus.Ambeth.Merge
{
    public class DefaultProxyHelper : IProxyHelper
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        public virtual Type GetRealType(Type type)
        {
            if (typeof(IProxyTargetAccessor).IsAssignableFrom(type))
            {
                Type baseType = type.BaseType;
                if (typeof(Object).Equals(baseType))
                {
                    return type.GetInterfaces()[0];
                }
                return baseType;
            }
            return type;
        }

        public bool ObjectEquals(Object leftObject, Object rightObject)
        {
            if (leftObject == null)
            {
                return rightObject == null;
            }
            if (rightObject == null)
            {
                return false;
            }
            if (leftObject == rightObject)
            {
                return true;
            }
            return Object.Equals(leftObject, rightObject);
        }

        public bool IsInitialized(Object parentObj, String memberName)
        {
            throw new NotSupportedException();
        }

        public bool IsInitialized(Object parentObj, IRelationInfoItem member)
        {
            throw new NotSupportedException();
        }

        public void SetUninitialized(Object parentObj, IRelationInfoItem member, IObjRef[] objRefs)
        {
            throw new NotSupportedException();
        }

        public IObjRef[] GetObjRefs(Object parentObj, String memberName)
        {
            throw new NotSupportedException();
        }

        public IObjRef[] GetObjRefs(Object parentObj, IRelationInfoItem member)
        {
            throw new NotSupportedException();
        }

        public void SetObjRefs(Object parentObj, String memberName, IObjRef[] objRefs)
        {
            throw new NotSupportedException();
        }

        public void SetObjRefs(Object parentObj, IRelationInfoItem member, IObjRef[] objRefs)
        {
            throw new NotSupportedException();
        }

        public object GetValueDirect(Object parentObj, IRelationInfoItem member)
        {
            throw new NotSupportedException();
        }

        public void SetInitPending(Object parentObj, IRelationInfoItem member)
        {
            throw new NotSupportedException();
        }

        public void SetValueDirect(Object parentObj, IRelationInfoItem member, Object value)
        {
            throw new NotSupportedException();
        }

        public void SetInitialized(Object parentObj, IRelationInfoItem member, Object value)
        {
            throw new NotSupportedException();
        }

        public bool GetInitPending(Object parentObj, IRelationInfoItem member)
        {
            throw new NotSupportedException();
        }

        public ValueHolderState GetState(Object parentObj, IRelationInfoItem member)
        {
            throw new NotSupportedException();
        }
    }
}