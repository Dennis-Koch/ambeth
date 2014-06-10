using System;
using System.IO;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using System.Text;

namespace De.Osthus.Ambeth.Xml
{
    public class CyclicXmlHandler : ICyclicXmlHandler, ICyclicXmlWriter, ICyclicXmlReader, ITypeBasedHandlerExtendable, INameBasedHandlerExtendable
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        [Autowired]
        public INameBasedHandlerExtendable NameBasedHandlerExtendable { protected get; set; }

        [Autowired]
        public ITypeBasedHandlerExtendable TypeBasedHandlerExtendable { protected get; set; }

        [Autowired]
	    public ICyclicXmlReader CyclicXmlReader { protected get; set; }

        [Autowired]
        public ICyclicXmlWriter CyclicXmlWriter { protected get; set; }

	    public virtual String Write(Object obj)
	    {
            return CyclicXmlWriter.Write(obj);
	    }

	    public virtual void WriteToStream(Stream outputStream, Object obj)
	    {
		    CyclicXmlWriter.WriteToStream(outputStream, obj);
	    }

	    public virtual Object ReadFromReader(TextReader reader)
	    {
            return CyclicXmlReader.ReadFromReader(reader);
	    }

	    public virtual Object Read(String cyclicXmlContent)
	    {
            return CyclicXmlReader.Read(cyclicXmlContent);
	    }

	    public virtual Object ReadFromStream(Stream inputStream)
	    {
            return CyclicXmlReader.ReadFromStream(inputStream);
	    }

        public virtual Object ReadFromStream(Stream inputStream, Encoding encoding)
        {
            return CyclicXmlReader.ReadFromStream(inputStream, encoding);
        }

        public void RegisterElementHandler(ITypeBasedHandler elementHandler, Type type)
        {
            TypeBasedHandlerExtendable.RegisterElementHandler(elementHandler, type);
        }

        public void UnregisterElementHandler(ITypeBasedHandler elementHandler, Type type)
        {
            TypeBasedHandlerExtendable.UnregisterElementHandler(elementHandler, type);
        }

        public void RegisterNameBasedElementHandler(INameBasedHandler nameBasedElementHandler, String elementName)
        {
            NameBasedHandlerExtendable.RegisterNameBasedElementHandler(nameBasedElementHandler, elementName);
        }

        public void UnregisterNameBasedElementHandler(INameBasedHandler nameBasedElementHandler, String elementName)
        {
            NameBasedHandlerExtendable.UnregisterNameBasedElementHandler(nameBasedElementHandler, elementName);
        }
    }
}