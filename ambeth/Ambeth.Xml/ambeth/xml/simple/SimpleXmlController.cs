using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Merge;
using System;
namespace De.Osthus.Ambeth.Xml.Simple
{
    public class SimpleXmlController : ICyclicXmlController, ITypeBasedHandlerExtendable, INameBasedHandlerExtendable
    {
        protected readonly ClassExtendableContainer<ITypeBasedHandler> typeToElementHandlers = new ClassExtendableContainer<ITypeBasedHandler>("elementHandler",
                "type");

        protected readonly MapExtendableContainer<String, INameBasedHandler> nameToElementHandlers = new MapExtendableContainer<String, INameBasedHandler>(
                "elementHandler", "name");

        [Autowired]
        public IProxyHelper ProxyHelper { protected get; set; }

        public Object ReadObject(IReader reader)
        {
            return ReadObject(typeof(Object), reader);
        }

        public Object ReadObject(Type returnType, IReader reader)
        {
            String elementName = reader.GetElementName();
            INameBasedHandler nameBasedHandler = nameToElementHandlers.GetExtension(elementName);
            if (nameBasedHandler == null)
            {
                throw new ArgumentException("Could not read object: " + elementName);
            }
            return nameBasedHandler.ReadObject(returnType, elementName, 0, reader);
        }

        public void WriteObject(Object obj, IWriter writer)
        {
            Type type = ProxyHelper.GetRealType(obj.GetType());
            ITypeBasedHandler extension = typeToElementHandlers.GetExtension(type);
            if (extension == null)
            {
                throw new ArgumentException("Could not write object: " + obj);
            }
            extension.WriteObject(obj, type, writer);
        }

        public void RegisterElementHandler(ITypeBasedHandler elementHandler, Type type)
        {
            typeToElementHandlers.Register(elementHandler, type);
        }

        public void UnregisterElementHandler(ITypeBasedHandler elementHandler, Type type)
        {
            typeToElementHandlers.Unregister(elementHandler, type);
        }

        public void RegisterNameBasedElementHandler(INameBasedHandler nameBasedElementHandler, String elementName)
        {
            nameToElementHandlers.Register(nameBasedElementHandler, elementName);
        }

        public void UnregisterNameBasedElementHandler(INameBasedHandler nameBasedElementHandler, String elementName)
        {
            nameToElementHandlers.Unregister(nameBasedElementHandler, elementName);
        }
    }
}