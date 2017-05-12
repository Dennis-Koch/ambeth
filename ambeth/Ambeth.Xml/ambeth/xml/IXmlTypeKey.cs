using System;
using De.Osthus.Ambeth.Ioc;

namespace De.Osthus.Ambeth.Xml
{
    public interface IXmlTypeKey
    {
        String Name { get; }

        String Namespace { get; }
    }
}