using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Transfer;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Cache.Model;

namespace De.Osthus.Ambeth.Cache
{
    public interface IServiceResultCache
    {
        IServiceResult GetORIsOfService(IServiceDescription serviceDescription, ExecuteServiceDelegate executeServiceDelegate);

        void InvalidateAll();
    }
}
