package de.osthus.ambeth.merge.model;

import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;

public interface IEntityMetaData
{
	Class<?> getEntityType();

	Class<?> getRealType();

	Class<?> getEnhancedType();

	boolean isLocalEntity();

	ITypeInfoItem getIdMember();

	ITypeInfoItem getIdMemberByIdIndex(byte idIndex);

	byte getIdIndexByMemberName(String memberName);

	ITypeInfoItem getVersionMember();

	ITypeInfoItem[] getAlternateIdMembers();

	int[][] getAlternateIdMemberIndicesInPrimitives();

	int getAlternateIdCount();

	ITypeInfoItem getCreatedOnMember();

	ITypeInfoItem getCreatedByMember();

	ITypeInfoItem getUpdatedOnMember();

	ITypeInfoItem getUpdatedByMember();

	ITypeInfoItem[] getFulltextMembers();

	ITypeInfoItem[] getPrimitiveMembers();

	IRelationInfoItem[] getRelationMembers();

	boolean isFulltextRelevant(ITypeInfoItem primitiveMember);

	boolean isMergeRelevant(ITypeInfoItem primitiveMember);

	boolean isAlternateId(ITypeInfoItem primitiveMember);

	ITypeInfoItem getMemberByName(String memberName);

	int getIndexByRelationName(String relationMemberName);

	int getIndexByRelation(IRelationInfoItem relationMember);

	int getIndexByPrimitiveName(String primitiveMemberName);

	int getIndexByPrimitive(ITypeInfoItem primitiveMember);

	boolean isRelationMember(String relationMemberName);

	Class<?>[] getTypesRelatingToThis();

	boolean isRelatingToThis(Class<?> childType);

	boolean isCascadeDelete(Class<?> other);

	boolean hasInterningBehavior(ITypeInfoItem primitiveMember);

	void changeInterningBehavior(ITypeInfoItem primitiveMember, boolean state);

	void postLoad(Object entity);

	void prePersist(Object entity);

	Object newInstance();
}
