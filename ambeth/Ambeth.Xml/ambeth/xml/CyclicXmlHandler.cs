using System;
using System.IO;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;

namespace De.Osthus.Ambeth.Xml
{
    public class CyclicXmlHandler : ICyclicXmlHandler, ICyclicXmlWriter, ICyclicXmlReader
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        [Autowired]
	    public virtual ICyclicXmlReader CyclicXmlReader { protected get; set; }

        [Autowired]
        public virtual ICyclicXmlWriter CyclicXmlWriter { protected get; set; }

	    public virtual String Write(Object obj)
	    {
            return this.CyclicXmlWriter.Write(obj);
	    }

	    public virtual void WriteToStream(Stream outputStream, Object obj)
	    {
		    this.CyclicXmlWriter.WriteToStream(outputStream, obj);
	    }

	    public virtual Object ReadFromReader(TextReader reader)
	    {
            return this.CyclicXmlReader.ReadFromReader(reader);
	    }

	    public virtual Object Read(String cyclicXmlContent)
	    {
            return this.CyclicXmlReader.Read(cyclicXmlContent);
	    }

	    public virtual Object ReadFromStream(Stream inputStream)
	    {
            return this.CyclicXmlReader.ReadFromStream(inputStream);
	    }
    }
}