using System;
using De.Osthus.Ambeth.Xml;

namespace De.Osthus.Ambeth.Xml
{
    public interface ICyclicXmlController
    {
        Object ReadObject(IReader reader);
        Object ReadObject(Type returnType, IReader reader);
        void WriteObject(Object obj, IWriter writer);
    }
}
