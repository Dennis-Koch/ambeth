using System;
using System.IO;

namespace De.Osthus.Ambeth.Xml
{
    public interface ICyclicXmlReader
    {
	    Object Read(String cyclicXmlContent);

	    Object ReadFromStream(Stream inputStream);

	    Object ReadFromReader(TextReader reader);
    }
}