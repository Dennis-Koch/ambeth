using System;
using System.Collections.Generic;
using System.IO;
using System.Xml;
using System.Xml.Linq;
using System.Xml.Schema;
using De.Osthus.Ambeth.Io;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util.Xml;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.Util
{
    public class XmlConfigUtil : IXmlConfigUtil
    {
        private const char PATH_SEPARATOR = '\\';

        private const String STRING_NULL = "null";

        [LogInstance]
        public ILogger Log { private get; set; }

        public XDocument[] ReadXmlFiles(String xmlFileNames)
        {
            Stream[] streams = FileUtil.OpenFileStreams(xmlFileNames, Log);
            List<XDocument> docs = new List<XDocument>(streams.Length);

            foreach (Stream stream in streams)
            {
                if (stream == null)
                {
                    continue;
                }

                XDocument doc = ReadXmlStream(stream);
                docs.Add(doc);
            }

            return docs.ToArray();
        }

        protected XDocument ReadXmlStream(Stream xmlStream)
        {
            XDocument doc = null;
            Exception throwLater = null;
            try
            {
                XmlReader reader = XmlReader.Create(xmlStream);
                doc = XDocument.Load(reader);
            }
            catch (Exception e)
            {
                throwLater = e;
            }
            finally
            {
                try
                {
                    xmlStream.Dispose();
                    xmlStream = null;
                }
                catch (Exception e)
                {
                    if (throwLater == null)
                    {
                        throwLater = e;
                    }
                }
                if (throwLater != null)
                {
                    throw new Exception("Exception while reading xml stream", throwLater);
                }
            }
            return doc;
        }

        public IXmlValidator CreateValidator(params String[] xsdFileNames)
        {
            // Silverlight does not support xml validation agains xsd
#if SILVERLIGHT
            return null;
#else
            XmlSchemaSet schemaSet = new XmlSchemaSet();
            Stream[] xsdStreams = FileUtil.OpenFileStreams(xsdFileNames);
            foreach (Stream xsdStream in xsdStreams)
            {
                XmlSchema xmlSchema = XmlSchema.Read(xsdStream, null);
                schemaSet.Add(xmlSchema);
            }
            IXmlValidator validator = new XmlValidator(schemaSet);
            return validator;
#endif
        }

        public String ReadDocumentNamespace(XDocument doc)
        {
            XElement root = doc.Root;
            String value = GetAttribute(root, "xmlns");
            return value;
        }

        public XElement GetChildUnique(XElement parent, XName childTagName)
        {
            IList<XElement> matchingChildren = new List<XElement>(parent.Elements(childTagName));
            if (matchingChildren.Count == 0)
            {
                return null;
            }
            else if (matchingChildren.Count == 1)
            {
                return matchingChildren[0];
            }
            else
            {
                throw new InvalidOperationException("There should only be one '" + childTagName.LocalName + "' child node");
            }
        }

        public IList<XElement> NodesToElements(IEnumerable<XElement> nodes)
        {
            List<XElement> elements = new List<XElement>(nodes);
            return elements;
        }

        public IMap<String, IList<XElement>> ChildrenToElementMap(XElement parent)
        {
            return ToElementMap(parent.Elements());
        }

        public IMap<String, IList<XElement>> ToElementMap(IEnumerable<XElement> elements)
        {
            HashMap<String, IList<XElement>> elementMap = new HashMap<String, IList<XElement>>();
            IEnumerator<XElement> enumerator = elements.GetEnumerator();
            while (enumerator.MoveNext())
            {
                XElement element = enumerator.Current;
                String nodeName = element.Name.LocalName;
                IList<XElement> list = elementMap.Get(nodeName);
                if (list == null)
                {
                    list = new List<XElement>();
                    elementMap.Put(nodeName, list);
                }
                list.Add(element);
            }

            return elementMap;
        }

        public String GetAttribute(XElement element, XName attrName)
        {
            return GetRequiredAttribute(element, attrName, false, false);
        }

        public String GetRequiredAttribute(XElement element, XName attrName)
        {
            return GetRequiredAttribute(element, attrName, true, false);
        }

        public String GetRequiredAttribute(XElement element, XName attrName, bool firstToUpper)
        {
            return GetRequiredAttribute(element, attrName, true, firstToUpper);
        }

        protected String GetRequiredAttribute(XElement element, XName attrName, bool required, bool firstToUpper)
        {
            XAttribute attribute = element.Attribute(attrName);
            String value;
            if (attribute == null)
            {
                if (required)
                {
                    throw new ArgumentException("Attribute '" + attrName.LocalName + "' has to be set on tag '" + element.Name + "'");
                }
                value = "";
            }
            else
            {
                value = attribute.Value;
            }
            if (required && value.Length == 0)
            {
                throw new ArgumentException("Attribute '" + attrName.LocalName + "' has to be set on tag '" + element.Name + "'");
            }
            if (firstToUpper)
            {
                value = StringConversionHelper.UpperCaseFirst(value);
            }
            return value;
        }

        public String GetChildElementAttribute(XElement parent, XName childName, XName attrName, String error)
        {
            String value = null;
            IList<XElement> tags = new List<XElement>(parent.Elements(childName));
            if (tags.Count == 1)
            {
                value = GetAttribute(tags[0], attrName);
            }
            else
            {
                throw new ArgumentException(error);
            }

            return value;
        }

        public bool AttributeIsTrue(XElement element, XName attrName)
        {
            String attr = GetAttribute(element, attrName);
            if (attr != null)
            {
                return XmlConstants.TRUE.Equals(attr);
            }
            return false;
        }

        public Type GetTypeForName(String name)
        {
            Type entityType = null;
            String name2 = null;

            entityType = AssemblyHelper.GetTypeFromAssemblies(name);
            if (entityType == null)
            {
                name2 = StringConversionHelper.PackageNameToNameSpace(name);
                entityType = AssemblyHelper.GetTypeFromAssemblies(name2);
            }

            if (entityType == null && Log.ErrorEnabled)
            {
                String message;
                if (name2 == null)
                {
                    message = "Configured type '" + name + "' was not found";
                }
                else
                {
                    message = "Configured type '" + name + "' was not found (also not '" + name2 + "')";
                }
                throw new Exception(message);
            }

            return entityType;
        }
    }
}
