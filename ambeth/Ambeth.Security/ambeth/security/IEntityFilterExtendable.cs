using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Security
{
    public interface IEntityFilterExtendable
    {
        void RegisterEntityFilter(IEntityFilter entityFilter);

        void UnregisterEntityFilter(IEntityFilter entityFilter);
    }
}
