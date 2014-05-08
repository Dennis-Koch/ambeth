using System;
using De.Osthus.Ambeth.Ioc;
using System.IO;

namespace De.Osthus.Ambeth.Xml
{
    public interface ICyclicXmlWriter
    {
        String Write(Object obj);

	    void WriteToStream(Stream outputStream, Object obj);
    }
}