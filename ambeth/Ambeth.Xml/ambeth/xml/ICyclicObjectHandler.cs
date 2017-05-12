using System;

namespace De.Osthus.Ambeth.Xml
{
    public interface ICyclicObjectHandler
    {
	    Object ReadObject(IReader reader);

	    Object ReadObject(Type type, int id, IReader reader);
    }
}