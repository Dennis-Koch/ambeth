using System;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.Xml.PostProcess
{
    public interface IXmlPostProcessorRegistry
    {
        IXmlPostProcessor GetXmlPostProcessor(String tagName);

        ILinkedMap<String, IXmlPostProcessor> GetXmlPostProcessors();
    }
}
