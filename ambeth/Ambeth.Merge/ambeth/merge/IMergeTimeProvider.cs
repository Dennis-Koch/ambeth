using System;
using System.Net;

namespace De.Osthus.Ambeth.Merge
{
    public interface IMergeTimeProvider
    {
        long GetStartTime();
    }
}
