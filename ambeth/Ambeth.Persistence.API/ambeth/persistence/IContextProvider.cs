using De.Osthus.Ambeth.Util;
using System;

namespace De.Osthus.Ambeth.Persistence
{
    public interface IContextProvider
    {
        long? CurrentTime { get; set; }

        String CurrentUser { get; set; }

        void Acquired();

        void Clear();

        void ClearAfterMerge();
    }
}