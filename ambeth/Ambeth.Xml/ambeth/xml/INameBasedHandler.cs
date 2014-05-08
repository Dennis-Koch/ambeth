using System;

namespace De.Osthus.Ambeth.Xml
{
    public interface INameBasedHandler
    {
	    Object ReadObject(Type returnType, String elementName, int id, IReader reader);

	    bool WritesCustom(Object obj, Type type, IWriter writer);
    }
}