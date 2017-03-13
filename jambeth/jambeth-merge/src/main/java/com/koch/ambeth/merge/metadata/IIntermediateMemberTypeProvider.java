package com.koch.ambeth.merge.metadata;

import com.koch.ambeth.service.metadata.IntermediatePrimitiveMember;
import com.koch.ambeth.service.metadata.IntermediateRelationMember;

public interface IIntermediateMemberTypeProvider
{
	IntermediatePrimitiveMember getIntermediatePrimitiveMember(Class<?> entityType, String propertyName);

	IntermediateRelationMember getIntermediateRelationMember(Class<?> entityType, String propertyName);
}
