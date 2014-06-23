using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Testutil;
using De.Osthus.Ambeth.Transfer;
using De.Osthus.Ambeth.Util;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using De.Osthus.Ambeth.Test.Model;

namespace De.Osthus.Ambeth.Xml.Test
{
    public class TestXmlPostProcessor : IXmlPostProcessor
    {
        public IList<String> handledTags = new List<String>();

        public Object processWrite(IPostProcessWriter writer)
        {
            return "";
        }

        public void processRead(IPostProcessReader reader)
        {
            String elementName = reader.getElementName();
            handledTags.add(elementName);
        }
    }
}
