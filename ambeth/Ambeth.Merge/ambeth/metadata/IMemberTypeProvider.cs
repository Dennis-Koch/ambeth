using System;

namespace De.Osthus.Ambeth.Metadata
{
    public interface IMemberTypeProvider
    {
        PrimitiveMember GetPrimitiveMember(Type type, String propertyName);

        RelationMember GetRelationMember(Type type, String propertyName);

        Member GetMember(Type type, String propertyName);
    }
}