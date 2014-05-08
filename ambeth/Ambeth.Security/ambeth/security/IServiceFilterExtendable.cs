using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Security
{
    public interface IServiceFilterExtendable
    {
        void RegisterServiceFilter(IServiceFilter serviceFilter);

        void UnregisterServiceFilter(IServiceFilter serviceFilter);
    }
}
