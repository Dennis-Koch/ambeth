using De.Osthus.Ambeth.Collections;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Typeinfo
{
    public interface IPropertyInfoProvider
    {
        IPropertyInfo GetProperty(Object obj, String propertyName);

        IPropertyInfo GetProperty(Type type, String propertyName);

        IPropertyInfo[] GetProperties(Object obj);

        IPropertyInfo[] GetProperties(Type type);

        IMap<String, IPropertyInfo> GetPropertyMap(Object obj);

        IMap<String, IPropertyInfo> GetPropertyMap(Type type);

        String GetPropertyNameFor(FieldInfo field);

        String GetPropertyNameFor(MethodInfo method);
    }
}
