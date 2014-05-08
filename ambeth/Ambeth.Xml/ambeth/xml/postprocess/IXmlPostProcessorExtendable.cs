using System;

namespace De.Osthus.Ambeth.Xml.PostProcess
{
    public interface IXmlPostProcessorExtendable
    {
        void RegisterXmlPostProcessor(IXmlPostProcessor xmlPostProcessor, String tagName);

        void UnregisterXmlPostProcessor(IXmlPostProcessor xmlPostProcessor, String tagName);
    }
}
