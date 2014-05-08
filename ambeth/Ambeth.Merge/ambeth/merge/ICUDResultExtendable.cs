using System;
using System.Net;

namespace De.Osthus.Ambeth.Merge
{
    public interface ICUDResultExtendable
    {
        void RegisterCUDResultExtension(ICUDResultExtension cudResultExtension, Type entityType);

        void UnregisterCUDResultExtension(ICUDResultExtension cudResultExtension, Type entityType);
    }
}
