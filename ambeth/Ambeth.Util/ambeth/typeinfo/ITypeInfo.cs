using System;

namespace De.Osthus.Ambeth.Typeinfo
{
    public interface ITypeInfo
    {
        String SimpleName { get; }

        Type RealType { get; }

        ITypeInfoItem[] Members { get; }

        ITypeInfoItem GetMemberByName(String memberName);

        ITypeInfoItem GetMemberByXmlName(String xmlMemberName);

        bool DoesImplement(Type interfaceArgument);

        String ToString();
    }
}
