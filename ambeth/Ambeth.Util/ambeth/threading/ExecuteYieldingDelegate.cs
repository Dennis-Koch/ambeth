using System.Threading;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Threading
{
    public delegate bool ExecuteYieldingDelegate<T>(T state, IYieldingController yieldingController);
}
