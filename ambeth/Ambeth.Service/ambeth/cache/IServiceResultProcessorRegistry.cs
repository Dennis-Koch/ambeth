using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Cache.Model;

namespace De.Osthus.Ambeth.Cache
{
    public interface IServiceResultProcessorRegistry
    {
        IServiceResultProcessor GetServiceResultProcessor(Type returnType);
    }
}