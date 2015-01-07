using System;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
    public interface INamedRunnable : Runnable
    {
        String Name { get; }
    }
}