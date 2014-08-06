using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Merge.Model
{
    public interface IEntityMetaData
    {
        Type EntityType { get; }

        Type RealType { get; }

        Type EnhancedType { get; }

        bool LocalEntity { get; }

        ITypeInfoItem IdMember { get; }

        ITypeInfoItem GetIdMemberByIdIndex(sbyte idIndex);

        sbyte GetIdIndexByMemberName(String memberName);

        ITypeInfoItem VersionMember { get; }

        ITypeInfoItem[] AlternateIdMembers { get; }

        int[][] AlternateIdMemberIndicesInPrimitives { get; }

        int GetAlternateIdCount();

        ITypeInfoItem CreatedOnMember { get; }

        ITypeInfoItem CreatedByMember { get; }

        ITypeInfoItem UpdatedOnMember { get; }

        ITypeInfoItem UpdatedByMember { get; }

        ITypeInfoItem[] PrimitiveMembers { get; }

        IRelationInfoItem[] RelationMembers { get; }

        bool IsMergeRelevant(ITypeInfoItem primitiveMember);

        ITypeInfoItem GetMemberByName(String memberName);

        int GetIndexByRelationName(String relationMemberName);

        int GetIndexByRelation(IRelationInfoItem relationMember);

        int GetIndexByPrimitiveName(String primitiveMemberName);

        int GetIndexByPrimitive(ITypeInfoItem primitiveMember);
        
        bool IsPrimitiveMember(String primitiveMemberName);

        bool IsRelationMember(String relationMemberName);

        Type[] TypesRelatingToThis { get; }

        bool IsRelatingToThis(Type childType);

        bool IsCascadeDelete(Type other);

        void PostLoad(Object entity);

        void PrePersist(Object entity);

        Object NewInstance();
    }
}