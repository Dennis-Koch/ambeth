using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Xml
{
    public interface IXmlTypeHelper
    {
        String GetXmlName(Type valueObjectType);

        String GetXmlNamespace(Type valueObjectType);

        String GetXmlTypeName(Type valueObjectType);

        Type GetType(String xmlName);

        Type[] GetTypes(IList<String> xmlNames);
    }
}