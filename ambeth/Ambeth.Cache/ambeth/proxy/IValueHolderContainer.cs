using System;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;

namespace De.Osthus.Ambeth.Proxy
{
    public interface IValueHolderContainer
    {
	    IObjRelation GetSelf(IRelationInfoItem member);

      	IObjRelation GetSelf(String memberName);

	    ICacheIntern __TargetCache { get; set; }

        ValueHolderState GetState(IRelationInfoItem member);

	    IObjRef[] GetObjRefs(IRelationInfoItem member);
    }
}