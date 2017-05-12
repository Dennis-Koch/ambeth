using De.Osthus.Ambeth.Ioc;
using System;

namespace De.Osthus.Ambeth.Platform
{
    public interface IAmbethPlatformContext : IDisposable
    {
        IServiceContext GetBeanContext();

        void ClearThreadLocal();

        void AfterBegin();

        void AfterCommit();

        void AfterRollback();
    }
}