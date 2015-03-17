package de.osthus.ambeth.metadata;

public interface IIntermediateMemberTypeProvider
{
	IntermediatePrimitiveMember getIntermediatePrimitiveMember(Class<?> entityType, String propertyName);

	IntermediateRelationMember getIntermediateRelationMember(Class<?> entityType, String propertyName);
}
