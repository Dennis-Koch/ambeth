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

namespace De.Osthus.Ambeth.Cache
{
    public class ServiceResultCacheDCL : IDataChangeListener, IInitializingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public IServiceResultCache ServiceResultCache { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ServiceResultCache, "ServiceResultCache");
        }

        public virtual void DataChanged(IDataChange dataChange, DateTime dispatchTime, long sequenceId)
        {
            if (dataChange.IsEmpty)
            {
                return;
            }
            ServiceResultCache.InvalidateAll();
        }
    }
}