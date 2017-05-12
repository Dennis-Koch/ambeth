using System;
using De.Osthus.Ambeth.Ioc;

namespace De.Osthus.Ambeth.Xml
{
    public interface IXmlTypeExtendable
    {
        void RegisterXmlType(Type type, String name, String namespaceString);

        void UnregisterXmlType(Type type, String name, String namespaceString);
    }
}
