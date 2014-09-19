using De.Osthus.Ambeth.Metadata;
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

        PrimitiveMember IdMember { get; }

        PrimitiveMember GetIdMemberByIdIndex(sbyte idIndex);

        sbyte GetIdIndexByMemberName(String memberName);

        PrimitiveMember VersionMember { get; }

        PrimitiveMember[] AlternateIdMembers { get; }

        int[][] AlternateIdMemberIndicesInPrimitives { get; }

        int GetAlternateIdCount();

        PrimitiveMember CreatedOnMember { get; }

        PrimitiveMember CreatedByMember { get; }

        PrimitiveMember UpdatedOnMember { get; }

        PrimitiveMember UpdatedByMember { get; }

        PrimitiveMember[] PrimitiveMembers { get; }

        RelationMember[] RelationMembers { get; }

        bool IsMergeRelevant(Member primitiveMember);

        Member GetMemberByName(String memberName);

        int GetIndexByRelationName(String relationMemberName);

        int GetIndexByRelation(Member relationMember);

        int GetIndexByPrimitiveName(String primitiveMemberName);

        int GetIndexByPrimitive(Member primitiveMember);
        
        bool IsPrimitiveMember(String primitiveMemberName);

        bool IsRelationMember(String relationMemberName);

        Type[] TypesRelatingToThis { get; }

        bool IsRelatingToThis(Type childType);

        bool IsCascadeDelete(Type other);

        void PostProcessNewEntity(Object newEntity);

        void PostLoad(Object entity);

        void PrePersist(Object entity);

        Object NewInstance();
    }
}