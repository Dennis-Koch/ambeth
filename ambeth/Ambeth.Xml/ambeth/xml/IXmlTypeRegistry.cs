using System;
using De.Osthus.Ambeth.Ioc;

namespace De.Osthus.Ambeth.Xml
{
    public interface IXmlTypeRegistry
    {
        Type GetType(String name, String namespaceString);

        IXmlTypeKey GetXmlType(Type type);

    	IXmlTypeKey GetXmlType(Type type, bool expectExisting);
    }
}