using System;

namespace De.Osthus.Ambeth.Util
{
    public interface IPrefetchHandle
    {
        IPrefetchState Prefetch(Object objects);

        IPrefetchState Prefetch(params Object[] objects);

        IPrefetchHandle Union(IPrefetchHandle otherPrefetchHandle);
    }
}
