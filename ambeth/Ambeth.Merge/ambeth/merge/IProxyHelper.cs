using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Cache;

namespace De.Osthus.Ambeth.Merge
{
    public interface IProxyHelper
    {
        Type GetRealType(Type type);

        bool IsInitialized(Object parentObj, String memberName);

        bool IsInitialized(Object parentObj, IRelationInfoItem member);

        void SetUninitialized(Object parentObj, IRelationInfoItem member, IObjRef[] objRefs);

        void SetInitialized(Object parentObj, IRelationInfoItem member, Object value);

        IObjRef[] GetObjRefs(Object parentObj, String memberName);

        IObjRef[] GetObjRefs(Object parentObj, IRelationInfoItem member);

        void SetObjRefs(Object parentObj, IRelationInfoItem member, IObjRef[] objRefs);

        bool ObjectEquals(Object leftObject, Object rightObject);

        ValueHolderState GetState(Object parentObj, IRelationInfoItem member);

        bool GetInitPending(Object parentObj, IRelationInfoItem member);

        void SetInitPending(Object parentObj, IRelationInfoItem member);

        Object GetValueDirect(Object parentObj, IRelationInfoItem member);

        void SetValueDirect(Object parentObj, IRelationInfoItem member, Object value);
    }
}
