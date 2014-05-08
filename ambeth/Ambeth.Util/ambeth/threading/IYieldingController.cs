using System.Threading;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Threading
{
    public interface IYieldingController
    {
        bool IsShouldYield { get; }
    }
}
