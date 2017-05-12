using System;

namespace De.Osthus.Ambeth.Merge
{
    public interface IValueObjectConfig
    {
        Type EntityType { get; }

        Type ValueType { get; }

        String GetValueObjectMemberName(String businessObjectMemberName);

        ValueObjectMemberType GetValueObjectMemberType(String valueObjectMemberName);

        bool HoldsListType(String memberName);

        bool IsIgnoredMember(String memberName);

        Type GetMemberType(String memberName);
    }
}
