using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Cache.Model;

namespace De.Osthus.Ambeth.Cache
{
    public interface IServiceResultProcessorExtendable
    {
        void RegisterServiceResultProcessor(IServiceResultProcessor serviceResultProcessor, Type returnType);

        void UnregisterServiceResultProcessor(IServiceResultProcessor serviceResultProcessor, Type returnType);
    }
}
