package de.osthus.ambeth.metadata;

public interface IMemberTypeProvider
{
	PrimitiveMember getPrimitiveMember(Class<?> type, String propertyName);

	RelationMember getRelationMember(Class<?> type, String propertyName);

	Member getMember(Class<?> type, String propertyName);
}
