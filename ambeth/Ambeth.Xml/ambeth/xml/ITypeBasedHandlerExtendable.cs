using System;

namespace De.Osthus.Ambeth.Xml
{
    public interface ITypeBasedHandlerExtendable
    {
        void RegisterElementHandler(ITypeBasedHandler elementHandler, Type type);

        void UnregisterElementHandler(ITypeBasedHandler elementHandler, Type type);
    }
}