using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Typeinfo
{
    public interface ITypeInfoProvider
    {
        ITypeInfo GetTypeInfo(Type type);

        ITypeInfoItem GetHierarchicMember(Type entityType, String hierarchicMemberName);

        ITypeInfoProvider GetInstance();
        
        ITypeInfoItem GetMember(Type entityType, PropertyInfo propertyInfo);

        ITypeInfoItem GetMember(Type entityType, IPropertyInfo propertyInfo);

        ITypeInfoItem GetMember(FieldInfo field);

        ITypeInfoItem GetMember(String propertyName, FieldInfo field);
    }
}
