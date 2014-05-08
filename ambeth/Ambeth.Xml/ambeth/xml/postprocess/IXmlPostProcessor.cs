using System;

namespace De.Osthus.Ambeth.Xml.PostProcess
{
    public interface IXmlPostProcessor
    {
        Object ProcessWrite(IPostProcessWriter writer);

        void ProcessRead(IPostProcessReader reader);
    }
}
