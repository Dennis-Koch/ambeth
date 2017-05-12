using System;

namespace De.Osthus.Ambeth.Xml
{
    public interface INameBasedHandlerExtendable
    {
        void RegisterNameBasedElementHandler(INameBasedHandler nameBasedElementHandler, String elementName);

        void UnregisterNameBasedElementHandler(INameBasedHandler nameBasedElementHandler, String elementName);
    }
}
