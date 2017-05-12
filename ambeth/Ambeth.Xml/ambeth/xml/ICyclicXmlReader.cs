using System;
using System.IO;
using System.Text;

namespace De.Osthus.Ambeth.Xml
{
    public interface ICyclicXmlReader
    {
	    Object Read(String cyclicXmlContent);

	    Object ReadFromStream(Stream inputStream);

        Object ReadFromStream(Stream inputStream, Encoding encoding);

        Object ReadFromReader(TextReader reader);
    }
}