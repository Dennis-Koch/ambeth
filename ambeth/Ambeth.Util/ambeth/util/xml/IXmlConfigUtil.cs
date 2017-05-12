using De.Osthus.Ambeth.Collections;
using System;
using System.Collections.Generic;
using System.Xml.Linq;

namespace De.Osthus.Ambeth.Util.Xml
{
    public interface IXmlConfigUtil
    {
        XDocument[] ReadXmlFiles(String xmlFileNames);

        IXmlValidator CreateValidator(params String[] xsdFileNames);

        String ReadDocumentNamespace(XDocument doc);

        XElement GetChildUnique(XElement parent, XName childTagName);

        IList<XElement> NodesToElements(IEnumerable<XElement> nodes);

        IMap<String, IList<XElement>> ChildrenToElementMap(XElement parent);

        IMap<String, IList<XElement>> ToElementMap(IEnumerable<XElement> elements);

        String GetAttribute(XElement entityTag, XName xName);

        String GetRequiredAttribute(XElement element, XName attrName);

        String GetRequiredAttribute(XElement element, XName attrName, bool firstToUpper);

        String GetChildElementAttribute(XElement parent, XName childName, XName attrName, String error);

        bool AttributeIsTrue(XElement element, XName attrName);

        Type GetTypeForName(String name);
    }
}
