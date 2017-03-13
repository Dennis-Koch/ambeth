package com.koch.ambeth.merge.metadata;

import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;

public interface IMemberTypeProvider
{
	PrimitiveMember getPrimitiveMember(Class<?> type, String propertyName);

	RelationMember getRelationMember(Class<?> type, String propertyName);

	Member getMember(Class<?> type, String propertyName);
}
