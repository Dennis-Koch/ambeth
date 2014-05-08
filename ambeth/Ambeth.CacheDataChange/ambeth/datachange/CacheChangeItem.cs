using System;
using De.Osthus.Ambeth.Datachange;
using De.Osthus.Ambeth.Datachange.Transfer;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Typeinfo;
using System.Collections;
using De.Osthus.Ambeth.Log;
using System.Threading;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge;

namespace De.Osthus.Ambeth.Cache
{
    public struct CacheChangeItem
    {
        public IWritableCache Cache;

        public IList<IObjRef> UpdatedObjRefs;

        public IList<IObjRef> DeletedObjRefs;

        public IList<Object> UpdatedObjects;
    }
}