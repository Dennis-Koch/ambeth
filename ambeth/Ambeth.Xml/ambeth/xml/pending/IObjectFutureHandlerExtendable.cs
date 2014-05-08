using System;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public interface IObjectFutureHandlerExtendable
    {
        void RegisterObjectFutureHandler(IObjectFutureHandler objectFutureHandler, Type type);

        void UnregisterObjectFutureHandler(IObjectFutureHandler objectFutureHandler, Type type);
    }
}
