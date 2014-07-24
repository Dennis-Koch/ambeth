using System;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;

namespace De.Osthus.Ambeth.Proxy
{
    public interface IObjRefContainer : IEntityMetaDataHolder
    {
        ValueHolderState Get__State(int relationIndex);

        void Set__InitPending(int relationIndex);

        bool Is__Initialized(int relationIndex);

        IObjRef[] Get__ObjRefs(int relationIndex);

        void Set__ObjRefs(int relationIndex, IObjRef[] objRefs);

        void Set__Uninitialized(int relationIndex, IObjRef[] objRefs);
    }
}