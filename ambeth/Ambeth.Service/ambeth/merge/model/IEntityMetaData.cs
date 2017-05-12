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

        PrimitiveMember GetIdMemberByIdIndex(int idIndex);

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

        /// <summary>
        /// Returns the member which matches the given memberPath best. This is useful in cases where embedded relational members should be traversed in multiple
	    /// hierarchies. Example:
	    /// 
	    /// Given a memberPath "embA.b.c" for an Entity of type A could return a member "embA.b" if A has a relation to B which is mapped to the embedded member
	    /// "embA.b".
        /// </summary>
        /// <param name="memberPath">Any multi-traversal path where the regarding relational member on this meta data should be searched for</param>
        /// <returns>The relational member which is mentioned in the multi-traversal path</returns>
        Member GetWidenedMatchingMember(String memberPath);

        /// <summary>
        /// Returns the member which matches the given memberPath best. This is useful in cases where embedded relational members should be traversed in multiple
        /// hierarchies. Example:
        /// 
        /// Given a memberPath "embA.b.c" for an Entity of type A could return a member "embA.b" if A has a relation to B which is mapped to the embedded member
        /// "embA.b".
        /// </summary>
        /// <param name="memberPath">Any multi-traversal path where the regarding relational member on this meta data should be searched for</param>
        /// <returns>The relational member which is mentioned in the multi-traversal path</returns>
        Member GetWidenedMatchingMember(String[] memberPath);
    }
}