using System;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;

namespace De.Osthus.Ambeth.Log
{
    public interface ILoggerCache
    {
        ILogger GetCachedLogger(IServiceContext serviceContext, Type loggerBeanType);

        ILogger GetCachedLogger(IProperties properties, Type loggerBeanType);
    }
}