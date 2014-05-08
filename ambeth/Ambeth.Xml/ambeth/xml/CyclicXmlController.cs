using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml.Typehandler;

namespace De.Osthus.Ambeth.Xml
{
    public class CyclicXmlController : AbstractHandler, ICyclicXmlController, ITypeBasedHandlerExtendable, INameBasedHandlerExtendable
    {
        [LogInstance]
        public new ILogger Log { private get; set; }

        public virtual IProxyHelper ProxyHelper { protected get; set; }

        public virtual IXmlTypeRegistry XmlTypeRegistry { protected get; set; }

        protected IMapExtendableContainer<Type, ITypeBasedHandler> typeToElementHandlers = new ClassExtendableContainer<ITypeBasedHandler>("elementHandler", "type");

        protected IMapExtendableContainer<String, INameBasedHandler> nameBasedElementReaders = new MapExtendableContainer<String, INameBasedHandler>("nameBasedElementReader", "elementName");

        protected IList<INameBasedHandler> nameBasedElementWriters = new List<INameBasedHandler>();

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();

            ParamChecker.AssertNotNull(ProxyHelper, "ProxyHelper");
            ParamChecker.AssertNotNull(XmlTypeRegistry, "XmlTypeRegistry");
        }

        public virtual void RegisterElementHandler(ITypeBasedHandler elementHandler, Type type)
        {
            typeToElementHandlers.Register(elementHandler, type);
        }

        public virtual void UnregisterElementHandler(ITypeBasedHandler elementHandler, Type type)
        {
            typeToElementHandlers.Unregister(elementHandler, type);
        }

        public virtual void RegisterNameBasedElementHandler(INameBasedHandler nameBasedElementHandler, String elementName)
        {
            nameBasedElementReaders.Register(nameBasedElementHandler, elementName);
            nameBasedElementWriters.Add(nameBasedElementHandler);
        }

        public virtual void UnregisterNameBasedElementHandler(INameBasedHandler nameBasedElementHandler, String elementName)
        {
            nameBasedElementReaders.Unregister(nameBasedElementHandler, elementName);
            nameBasedElementWriters.Remove(nameBasedElementHandler);
        }

        public virtual Object ReadObject(IReader reader)
        {
            return ReadObject(typeof(Object), reader);
        }

        public virtual Object ReadObject(Type returnType, IReader reader)
        {
            String elementName = reader.GetElementName();
            if (XmlDictionary.NullElement.Equals(elementName))
            {
                reader.MoveOverElementEnd();
                return null;
            }
            String idValue = reader.GetAttributeValue(XmlDictionary.IdAttribute);
            int id = idValue != null && idValue.Length > 0 ? Int32.Parse(idValue) : 0;
            if (XmlDictionary.RefElement.Equals(elementName))
            {
                reader.MoveOverElementEnd();
                return reader.GetObjectById(id);
            }
            Object obj;
            if (XmlDictionary.ObjectElement.Equals(elementName))
            {
                Type type = ClassElementHandler.ReadFromAttribute(reader);
                obj = ReadObjectContent(returnType, type, id, reader);
            }
            else
            {
                INameBasedHandler nameBasedElementReader = nameBasedElementReaders.GetExtension(elementName);
                if (nameBasedElementReader == null)
                {
                    throw new Exception("Element name '" + elementName + "' not supported");
                }
                obj = nameBasedElementReader.ReadObject(returnType, elementName, id, reader);
            }
            if (id > 0)
            {
                reader.PutObjectWithId(obj, id);
            }
            reader.MoveOverElementEnd();
            return obj;
        }

        public virtual void WriteObject(Object obj, IWriter writer)
        {
            if (obj == null)
            {
                writer.WriteStartElement(XmlDictionary.NullElement);
                writer.WriteEndElement();
                return;
            }
            int id = writer.GetIdOfObject(obj);
            if (id > 0)
            {
                writer.WriteStartElement(XmlDictionary.RefElement);
                writer.WriteAttribute(XmlDictionary.IdAttribute, id.ToString());
                writer.WriteEndElement();
                return;
            }
            Type type = ProxyHelper.GetRealType(obj.GetType());
            for (int a = 0, size = nameBasedElementWriters.Count; a < size; a++)
            {
                INameBasedHandler nameBasedElementWriter = nameBasedElementWriters[a];
                if (nameBasedElementWriter.WritesCustom(obj, type, writer))
                {
                    return;
                }
            }

            id = writer.AcquireIdForObject(obj);
            String objectElement = XmlDictionary.ObjectElement;
            writer.WriteStartElement(objectElement);
            if (id > 0)
            {
                writer.WriteAttribute(XmlDictionary.IdAttribute, id.ToString());
            }
            ClassElementHandler.WriteAsAttribute(type, writer);
            WriteObjectContent(obj, type, writer);
            writer.WriteCloseElement(objectElement);
        }

        protected virtual Object ReadObjectContent(Type returnType, Type type, int id, IReader reader)
        {
            ITypeBasedHandler extension = typeToElementHandlers.GetExtension(type);
            if (extension == null)
            {
                throw new Exception("No extension mapped to type '" + type.Name + "' found");
            }
            return extension.ReadObject(returnType, type, id, reader);
        }

        protected virtual void WriteObjectContent(Object obj, Type type, IWriter writer)
        {
            ITypeBasedHandler extension = typeToElementHandlers.GetExtension(type);
            if (extension == null)
            {
                throw new Exception("No extension mapped to type '" + type.Name + "' found");
            }
            extension.WriteObject(obj, type, writer);
        }

        protected virtual Object ResolveObjectById(IDictionary<int, Object> idToObjectMap, int id)
        {
            Object obj = DictionaryExtension.ValueOrDefault(idToObjectMap, id);
            if (obj == null)
            {
                throw new Exception("No object found with id " + id);
            }
            return obj;
        }
    }
}