package com.koch.ambeth.cache;

import com.koch.ambeth.service.metadata.Member;

public interface IParentEntityAware
{
	void setParentEntity(Object parentEntity, Member member);
}
