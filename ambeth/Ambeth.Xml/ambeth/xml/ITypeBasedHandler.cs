using System;

namespace De.Osthus.Ambeth.Xml
{
    public interface ITypeBasedHandler
    {
        Object ReadObject(Type returnType, Type objType, int id, IReader reader);

        void WriteObject(Object obj, Type type, IWriter writer);
    }
}