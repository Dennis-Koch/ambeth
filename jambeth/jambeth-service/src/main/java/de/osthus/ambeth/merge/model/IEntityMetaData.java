package de.osthus.ambeth.merge.model;

import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.metadata.RelationMember;

public interface IEntityMetaData
{
	Class<?> getEntityType();

	Class<?> getRealType();

	Class<?> getEnhancedType();

	boolean isLocalEntity();

	PrimitiveMember getIdMember();

	PrimitiveMember getIdMemberByIdIndex(byte idIndex);

	byte getIdIndexByMemberName(String memberName);

	PrimitiveMember getVersionMember();

	PrimitiveMember[] getAlternateIdMembers();

	int[][] getAlternateIdMemberIndicesInPrimitives();

	int getAlternateIdCount();

	PrimitiveMember getCreatedOnMember();

	PrimitiveMember getCreatedByMember();

	PrimitiveMember getUpdatedOnMember();

	PrimitiveMember getUpdatedByMember();

	PrimitiveMember[] getFulltextMembers();

	PrimitiveMember[] getPrimitiveMembers();

	RelationMember[] getRelationMembers();

	boolean isFulltextRelevant(Member primitiveMember);

	boolean isMergeRelevant(Member primitiveMember);

	boolean isAlternateId(Member primitiveMember);

	Member getMemberByName(String memberName);

	int getIndexByRelationName(String relationMemberName);

	int getIndexByRelation(Member relationMember);

	int getIndexByPrimitiveName(String primitiveMemberName);

	int getIndexByPrimitive(Member primitiveMember);

	boolean isPrimitiveMember(String primitiveMemberName);

	boolean isRelationMember(String relationMemberName);

	Class<?>[] getTypesRelatingToThis();

	boolean isRelatingToThis(Class<?> childType);

	boolean isCascadeDelete(Class<?> other);

	boolean hasInterningBehavior(Member primitiveMember);

	void changeInterningBehavior(Member primitiveMember, boolean state);

	void postProcessNewEntity(Object newEntity);

	void postLoad(Object entity);

	void prePersist(Object entity);

	Object newInstance();
}
