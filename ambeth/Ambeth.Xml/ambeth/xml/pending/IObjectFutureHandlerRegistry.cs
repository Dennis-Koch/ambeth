using System;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public interface IObjectFutureHandlerRegistry
    {
        IObjectFutureHandler GetObjectFutureHandler(Type type);
    }
}
