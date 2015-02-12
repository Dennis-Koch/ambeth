using System;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;

namespace De.Osthus.Ambeth.Proxy
{
    public interface IValueHolderContainer : IObjRefContainer
    {
        IObjRelation Get__Self(int relationIndex);

        ICacheIntern __TargetCache { get; set; }

        Object Get__ValueDirect(int relationIndex);

        void Set__ValueDirect(int relationIndex, Object value);
        
        void Set__InitPending(int relationIndex);
        
        void Set__Uninitialized(int relationIndex, IObjRef[] objRefs);
    }
}