using System;
using System.IO;

namespace De.Osthus.Ambeth.Xml
{
    public interface ICyclicXmlHandler
    {
        String Write(Object obj);

	    void WriteToStream(Stream outputStream, Object obj);

	    Object Read(String cyclicXmlContent);

        Object ReadFromStream(Stream inputStream);
    }
}
